package compiler

import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ErrorHandlingTests {

    /**
     * Errors are generated and appear in 'status.errors'
     */
    @Test
    fun errorMessageDependencyStringTest() = testSession {
        var p = FeatureImplementation(declaredName = "XXX")
        p.expression = "asdf+asdf" // nonsense
        p = addOwnedMember(p, global)
        addOwnedRelationship(SpecializationImplementation(p, anything), p)
        p.variable = VariableImplementation(p)
        p.resolveNames()
        p.variable?.compileExpression()
        assertNotEquals(0, status.issues.size)
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
        assertTrue(status.issues.isNotEmpty())
    }


    @Test
    fun missingSuperClassError() = testSession {
        loadKerML("Engine :>.")
        propagate()
        assertTrue(status.issues.isNotEmpty())
    }

    @Test
    fun loadFailed() = testSession {
        loadKerML("""
            package SportsCar;
            type Porsche911 :> SportsCar;
        """)
        propagate()
        propagate()
        assertTrue(status.issues.size > 0, "Expected issue with SportsCar that is inappropriate type")
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
        propagate()
        assertTrue(status.issues.isNotEmpty(), "Error messages: ${status.issues}")
        assertNotNull(status.issues.first().input)
    }
}