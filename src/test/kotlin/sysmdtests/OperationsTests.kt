package sysmdtests

import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.aadd.values.XBool.Companion.False
import com.github.tukcps.aadd.values.XBool.Companion.True
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class OperationsTests {
    @Test
    fun notTest1() = testSession {
        loadSysMD("""
            feature a: ScalarValues::Boolean = not true; 
        """.trimIndent())
        val a = global.resolve<Feature>("a")!!.variable
        assertEquals(False, a?.vectorQuantity?.value as XBool)
    }

    @Test
    fun notTest2() = testSession {
        loadSysMD("""
            feature a: ScalarValues::Boolean = not false or false; 
        """.trimIndent())
        val a = global.resolve<Feature>("a")!!.variable
        assertTrue(a?.ast?.dependency is com.github.tukcps.sysmd.model.expression.AstBinOp)
        assertEquals(True, a?.vectorQuantity?.value as XBool)
    }

    @Test
    fun minusTest1() = testSession {
        loadSysMD("""
            feature a: ScalarValues::Real = - 1.0 -- 1.0; 
        """.trimIndent())
        val a = global.resolve<Feature>("a")!!.variable
        assertEquals(0.0, a!!.vectorQuantity.getMaxAsDouble(), 0.00000001)
    }
}