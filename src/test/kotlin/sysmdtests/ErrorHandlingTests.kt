package sysmdtests

import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*


class ErrorHandlingTests {

    /**
     * Errors are generated and appear in 'status.errors'
     */
    @Test
    fun errorMessageDependencyStringTest() = testSession {
            var p = FeatureImplementation(declaredName="XXX")
            p.expression = "asdf+asdf" // nonsense
            p = create(p, global)
            create(SpecializationImplementation(p, anything), p)
            p.variable = VariableImplementation(p)
            p.resolveNames()
            p.variable?.compileExpression()
            assertNotEquals(0, status.exceptions.size)
    }

    /**
     * Errors are saved in the model.status.exceptions, with
     * the id of the last correctly recognized element as key, and with the line number.
     */
    @Test
    fun errorMessageSysMdError() = testSession {
        loadKerML("""
            package p;
            p defines isA.
        """)
        assertTrue(status.exceptions.isNotEmpty())
    }


    @Test
    fun missingSuperClassError() = testSession {
        loadKerML("Engine :>.")
        propagate()
        assertTrue(status.exceptions.isNotEmpty())
    }

    @Test
    fun loadFailed() = testSession {
        loadKerML("""
            package SportsCar;
            Porsche911 isA SportsCar.
        """)
        propagate()
        assertEquals(1, status.exceptions.size, "Error messages: ${status.exceptions}")
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
        assertTrue(status.exceptions.isNotEmpty(), "Error messages: ${status.exceptions}")
        assertNotNull(status.exceptions.first().textualRepresentation)
    }
}
