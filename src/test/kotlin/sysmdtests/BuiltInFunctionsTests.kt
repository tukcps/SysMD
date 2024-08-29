package sysmdtests

import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuiltInFunctionsTests {

    /**
     *  The function getLeaves returns the leaves of the ast of a dependency expression.
     */
    @Test
    fun getLeavesTest() = testSession {
        loadSysMD("""
            feature b: ScalarValues::Real = 2.0;
            feature c: ScalarValues::Real = 3.0;
            feature d: ScalarValues::Real = 4.0; 
            feature a: ScalarValues::Real = b + c * d;
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val a = global.resolve<Feature>("a")
        assertEquals(3, a!!.variable!!.ast!!.getLeaves().size)
    }

    @Test fun hasAFunctionTest() = testSession {
        loadSysMD(catchExceptions = false, input = """
            feature a: ScalarValues::Real = 2.0;
            feature b: ScalarValues::Boolean = hasA(Global, a);
            """)
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val b = global.resolve<Feature>("b")!!
        assertEquals(builder.True, b.variable!!.vectorQuantity.value)
    }

    @Test fun hasAFunctionTest2() = testSession {
        loadSysMD(catchExceptions = false, input = """
            feature a: ScalarValues::Real = 2.0;
            feature b: ScalarValues::Boolean = hasA(Global, c).
            """)
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val b = global.resolve<Feature>("b")!!
        assertEquals(builder.False, b.variable!!.vectorQuantity.value)
    }

    @Test fun isAFunctionTest() = testSession {
        loadSysMD(catchExceptions = false, input = """
                feature a: ScalarValues::Real = 2.0;
                feature b: ScalarValues::Boolean = isA(a, ScalarValues::Real).
                """)
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val b = global.resolve<Feature>("b") !!
        assertEquals(builder.True, b.variable!!.vectorQuantity.value)
    }
}