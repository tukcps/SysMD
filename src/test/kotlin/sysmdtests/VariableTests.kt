package sysmdtests

import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VariableTests {
    @Test
    fun ifElseExpressionTest1() = testSession {
        loadSysMD("""
                feature x: ScalarValues::Boolean;
                feature y: ScalarValues::Boolean = if x ? true else false;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val y = global.resolve<Feature>("y") !!.variable !!
        assertEquals(XBool.X, y.vectorQuantity.value)
     }

    @Test
    fun ifElseExpressionTest2() = testSession {
        loadSysMD("""
                feature x: ScalarValues::Boolean = false;
                feature y: ScalarValues::Boolean = if x ? true else false. 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val y = global.resolve<Feature>("y")!!.variable!!
        assertEquals(XBool.False, y.vectorQuantity.value)
    }

    @Test
    fun ifElseExpressionTest3() = testSession {
        loadSysMD("""
               feature x: ScalarValues::Boolean;
               feature y: ScalarValues::Real = if x ? 1.0 else 2.0;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val y = global.resolve<Feature>("y")!!.variable!!
        assertEquals(1.0, y.vectorQuantity.getMinAsDouble(), 0.00001)
        assertEquals(2.0, y.vectorQuantity.getMaxAsDouble(), 0.00001)
    }

    @Test
    fun ifElseExpressionTest4() = testSession {
        loadSysMD("""
               feature x: ScalarValues::Boolean;
               feature y: ScalarValues::Integer = if x ? 1 else 2;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val y = global.resolve<Feature>("y")!!.variable!!
        assertEquals(1.0, y.vectorQuantity.getMinAsDouble(), 0.00001)
        assertEquals(2.0, y.vectorQuantity.getMaxAsDouble(), 0.00001)
    }

}