package solver

import util.assertBounds
import com.github.tukcps.sysmd.model.expression.AstBinOp
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.util.Assertions.assertEquals
import io.github.tukcps.aadd.values.bool.XBool
import io.github.tukcps.aadd.values.bounds.DoubleBound
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class OperationsTests {
    @Test
    fun notTest1() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean = not true; 
        """, Runlevel.VARIABLES)
        val a = solver.getVariable("a")
        assertEquals(XBool.False, a?.bool()?.value)
    }

    @Test
    fun notTest2() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean = not false or false; 
        """, Runlevel.VARIABLES)
        val a = solver.getVariable("a")
        assertTrue(a?.ast?.dependency is AstBinOp)
        assertEquals(XBool.True, a.bool().value)
    }

    @Test
    fun minusTest1() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = - 1.0 -- 1.0; 
        """, Runlevel.VARIABLES)
        val a = solver.getVariable("a")
        assertBounds(0.0, a!!)
    }
}