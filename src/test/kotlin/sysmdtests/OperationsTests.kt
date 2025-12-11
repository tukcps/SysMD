package sysmdtests

import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.XBool.Companion.False
import io.github.tukcps.aadd.values.XBool.Companion.True
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
        """)
        val a = global.resolveVar("a")
        assertEquals(False, a?.bool())
    }

    @Test
    fun notTest2() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean = not false or false; 
        """)
        val a = global.resolveVar("a")
        assertTrue(a?.ast?.dependency is com.github.tukcps.sysmd.model.expression.AstBinOp)
        assertEquals(True, a.bool())
    }

    @Test
    fun minusTest1() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = - 1.0 -- 1.0; 
        """)
        val a = global.resolveVar("a")
        assertEquals(0.0, a!!.max(), 0.00000001)
    }
}