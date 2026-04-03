package compiler

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.MembershipImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import util.assertIssue
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ErrorHandlingTests {

    /**
     * Errors are generated and appear in 'status.errors'
     */
    @Test
    fun errorMessageDependencyStringTest() = testSession("ScalarValues")  {
        var p = FeatureImplementation(declaredName = "XXX")
        p.expression = "asdf +++ asdf" // nonsense
        p = addOwnedMember(p, global)
        addOwnedRelationship(SpecializationImplementation(p, anything), p)
        solver.addVariable(p.path(),
            VariableImplementation(
                MembershipImplementation(memberElement = p),
                solver=solver,
                baseType = Variable.BaseType.Real,
                path = p.path(),
                expression = p.expression,
            )
        )
        solver.getVariable("XXX")!!.compileExpression()
        assertIssue("rror in production")
    }

    /**
     * Errors are saved in the model.status.reports, with
     * the id of the last correctly recognized element as key, and with the line number.
     */
    @Test
    fun errorMessageSysMdError() = testSession {
        loadKerML("""
            package p;
            p defines isA.
        """)
        assertIssue("Expected")
    }


    @Test
    fun missingSuperClassError() = testSession {
        loadKerML("Engine :> ; ")
        solver.propagate()
        assertIssue("Expected")
    }

    @Test
    fun loadFailed() = testSession {
        loadKerML("""
            package SportsCar;
            type Porsche911 :> SportsCar;
        """)
        solver.propagate()
        solver.propagate()
        assertIssue("SportsCar", "Expected issue with SportsCar that is inappropriate type")
    }


    /**
     * if there is no semicolon and then EOF, textual representation shall be set correction.
     */
    @Test
    fun loadFailed3() = testSession {
        loadKerML("""
            abstract type Vehicle specializes Base::Anything;
            type Bicycle specializes Vehicle;
            type
        """)
        solver.propagate()
        assertTrue(status.issues.isNotEmpty(), "Error messages: ${status.issues}")
        assertNotNull(status.issues.first().input)
    }
}