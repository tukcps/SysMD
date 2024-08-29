package sysmdtests

import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.compiler.parseDependency
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class ErrorHandlingTests {

    /**
     * Errors are generated and appear in 'status.errors'
     */
    @Test
    fun errorMessageDependencyStringTest() = testSession(loadKerML = false) {
            var p = FeatureImplementation(declaredName="XXX")
            p.expression = "asdf+asdf" // nonsense
            p = create(p, global)
            create(SpecializationImplementation(p, any), p)
            p.variable = VariableImplementation(p)
            p.resolveNames()
            p.variable?.parseDependency()
            assertNotEquals(0, status.exceptions.size)
    }

    /**
     * Errors are saved in the agilaModel.status.exceptions, with
     * the id of the last correctly recognized element as key, and with the line number.
     */
    @Test
    fun errorMessageSysMdError() = testSession {
        loadSysMD("""
            package p;
            p defines isA.
        """.trimIndent()
        )
        assertTrue(status.exceptions.isNotEmpty())
    }


    @Test
    fun missingSuperClassError() = testSession {
        loadSysMD("Engine :>.")
        propagate()
        assertTrue(status.exceptions.isNotEmpty())
    }

    @Test
    fun loadFailed() = testSession {
        loadSysMD("""
            package SportsCar;
            Porsche911 isA SportsCar.
        """.trimIndent()
        )
        propagate()
        assertEquals(1, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    /**
     * if there is no semicolon and then EOF, textual representation shall be set correction.
     */
    @Test
    fun loadFailed3() = testSession {
        loadSysMD("""
            abstract type Vehicle specializes Base::Anything;
            type Bicycle specializes Vehicle;
            type
        """.trimIndent()
        )
        propagate()
        assertEquals(1, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertNotNull(status.exceptions.first().textualRepresentation)
    }
}
