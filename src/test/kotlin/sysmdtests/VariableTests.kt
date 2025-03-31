package sysmdtests

import io.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import kotlin.test.*
import util.testSession

class VariableTests {
    @Test
    fun ifElseExpressionTest1() = testSession("ScalarValues") {
        loadKerML("""
                feature x: ScalarValues::Boolean;
                feature y: ScalarValues::Boolean = if x ? true else false;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val y = global.resolve<Feature>("y") !!.variable !!
        assertEquals(XBool.X, y.vectorQuantity.value as XBool)
     }

    @Test
    fun ifElseExpressionTest2() = testSession("ScalarValues") {
        loadKerML("""
                feature x: ScalarValues::Boolean = false;
                feature y: ScalarValues::Boolean = if x ? true else false. 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val y = global.resolve<Feature>("y")!!.variable!!
        assertEquals(XBool.False, y.vectorQuantity.value as XBool)
    }

    @Test
    fun ifElseExpressionTest3() = testSession("ScalarValues") {
        loadKerML("""
               feature x: ScalarValues::Boolean;
               feature y: ScalarValues::Real = if x ? 1.0 else 2.0;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val y = global.resolve<Feature>("y")!!.variable!!
        assertEquals(1.0, y.vectorQuantity.getMinAsDouble(), 0.00001)
        assertEquals(2.0, y.vectorQuantity.getMaxAsDouble(), 0.00001)
    }

    @Test
    fun ifElseExpressionTest4() = testSession("ScalarValues") {
        loadKerML("""
               feature x: ScalarValues::Boolean;
               feature y: ScalarValues::Integer = if x ? 1 else 2;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val y = global.resolve<Feature>("y")!!.variable!!
        assertEquals(1.0, y.vectorQuantity.getMinAsDouble(), 0.00001)
        assertEquals(2.0, y.vectorQuantity.getMaxAsDouble(), 0.00001)
    }
}