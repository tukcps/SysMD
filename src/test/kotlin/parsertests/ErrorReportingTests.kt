package parsertests

import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ErrorReportingTests {


    /**
     * Repeated execution of same code does not add new errors.
     */
    @Test
    fun reportNotTwice() = testSession {
        loadSysMD("""Global a b c.""".trimIndent())
        val nr = status.exceptions.size
        loadSysMD("""Global a b c.""".trimIndent())
        assertEquals(nr, status.exceptions.size)
    }

    @Test
    fun reportConstraintWrong() = testSession {
        loadSysMD("feature x: ScalarValues::Real(1.0 .. 0.0);")
        assertTrue(status.exceptions.isNotEmpty() )
    }

    @Test
    fun reportTypeIncompatible() = testSession {
        loadSysMD("feature x: Real(2.0 .. 3.0) = 1 + 2.0.")
        assertTrue(status.exceptions.isNotEmpty())
    }

    @Test
    fun reportUnknownOwner() = testSession {
        loadSysMD("""
            xx::yyy hasA feature p: Base::Anything. 
        """.trimIndent())
        assertTrue(status.exceptions.isNotEmpty())
    }
}