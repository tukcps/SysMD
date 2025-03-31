package parsertests

import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErrorReportingTests {


    /**
     * Repeated execution of the same code does not add new errors.
     */
    @Test
    fun reportNotTwice() = testSession("ScalarValues") {
        loadKerML("""Global a b c.""".trimIndent())
        val nr = status.exceptions.size
        loadKerML("""Global a b c.""".trimIndent())
        assertEquals(nr, status.exceptions.size)
    }

    // Unsure whether we need this error or if we simply shall return an empty set.
    // @Ignore
    @Test
    fun reportConstraintWrong() = testSession("ScalarValues") {
        loadKerML("feature x: ScalarValues::Real(1.0 .. 0.0);")
        // x is empty
        val x = global.resolveVar("x")
        assertEquals(builder.Empty, x?.aadd())
        // shall we report an error? eventually, a user wants exactly this.
        // assertTrue(status.exceptions.isNotEmpty() )
    }

    @Test
    fun reportTypeIncompatible() = testSession("ScalarValues") {
        loadKerML("feature x: Real(2.0 .. 3.0) = 1 + 2.0.")
        assertTrue(status.exceptions.isNotEmpty())
    }

    @Test
    fun reportUnknownOwner() = testSession("ScalarValues") {
        loadKerML("""
            xx::yyy hasA feature p: Base::Anything. 
        """.trimIndent())
        assertTrue(status.exceptions.isNotEmpty())
    }
}