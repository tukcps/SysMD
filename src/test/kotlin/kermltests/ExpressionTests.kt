package kermltests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpressionTests {
    @Test
    fun testExpression() = testSession("ScalarValues") {
        loadKerML("""
            feature f: ScalarValues::Real = oneOf(1.0 .. 2.0);    
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val f = solver.getVariable("f")
        assertEquals(2.0, f!!.max())
    }
}