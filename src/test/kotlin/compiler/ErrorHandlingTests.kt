package compiler

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.MembershipImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.services.Runlevel
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
        var p = FeatureImplementation(this, declaredName = "XXX")
        p.expression = "asdf +++ asdf" // nonsense
        p = addOwnedMember(p, global)
        addOwnedRelationship(SpecializationImplementation(this, specific = p, general = repo.anything!!), p)
        solver.addVariable(p.path(),
            VariableImplementation(
                MembershipImplementation(this, memberElement = p),
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
        loadKerML("Engine :> ; ", Runlevel.MODEL)
        assertIssue("Expected")
    }

    @Test
    fun wringSuperClassError() = testSession("ScalarValues") {
        loadKerML("""
            package SportsCar;
            type Porsche911 :> SportsCar;
        """, Runlevel.MODEL)
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