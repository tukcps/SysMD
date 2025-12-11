package sysmdtests

import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.XBool
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class VariableTests {
    @Test
    fun ifElseExpressionTest1() = testSession("ScalarValues") {
        loadKerML("""
                feature x: ScalarValues::Boolean;
                feature y: ScalarValues::Boolean = if x ? true else false;
        """.trimIndent())
        assertNoIssues()
        val y = global.resolveVar("y")!!
        assertEquals(XBool.X, y.bool())
     }

    @Test
    fun ifElseExpressionTest2() = testSession("ScalarValues") {
        loadKerML("""
                feature x: ScalarValues::Boolean = false;
                feature y: ScalarValues::Boolean = if x ? true else false. 
        """.trimIndent())
        assertNoIssues()
        val y = global.resolveVar("y")!!
        assertEquals(XBool.False, y.bool())
    }

    @Test
    fun ifElseExpressionTest3() = testSession("ScalarValues") {
        loadKerML("""
               feature x: ScalarValues::Boolean;
               feature y: ScalarValues::Real = if x ? 1.0 else 2.0;
        """.trimIndent())
        assertNoIssues()
        val y = global.resolveVar("y")!!
        assertEquals(1.0, y.min(), 0.00001)
        assertEquals(2.0, y.max(), 0.00001)
    }

    @Test
    fun ifElseExpressionTest4() = testSession("ScalarValues") {
        loadKerML("""
               feature x: ScalarValues::Boolean;
               feature y: ScalarValues::Integer = if x ? 1 else 2;
        """.trimIndent())
        assertNoIssues()
        val y = global.resolveVar("y")!!
        assertEquals(1.0, y.min(), 0.00001)
        assertEquals(2.0, y.max(), 0.00001)
    }
}