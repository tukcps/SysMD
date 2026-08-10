package solver

import com.github.tukcps.sysmd.model.expression.AstBinOp
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.values.XBool
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OperationsTests {
    @Test
    fun notTest1() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean = not true; 
        """, Runlevel.VARIABLES)
        val a = solver.getVariable("a")
        assertEquals(XBool.False, a?.bool())
    }

    @Test
    fun notTest2() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean = not false or false; 
        """, Runlevel.VARIABLES)
        val a = solver.getVariable("a")
        assertTrue(a?.ast?.dependency is AstBinOp)
        assertEquals(XBool.True, a.bool())
    }

    @Test
    fun minusTest1() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = - 1.0 -- 1.0; 
        """, Runlevel.VARIABLES)
        val a = solver.getVariable("a")
        assertEquals(0.0, a!!.max(), 0.00000001)
    }
}