package kermltests

import util.assertBounds
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.util.Assertions.assertEquals
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test

class ExpressionTests {
    @Test
    fun testExpression() = testSession("ScalarValues") {
        loadKerML("""
            feature f: ScalarValues::Real = oneOf(1.0 .. 2.0);    
        """, Runlevel.VARIABLES)
        solver.propagate()
        assertNoIssues()
        val f = solver.getVariable("f")
        assertBounds(1.0..2.0, f!!)
    }
}