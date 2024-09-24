package sysmdtests

import com.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Ignore
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

    @Test fun hastypeOperationTest() = testSession {
        loadSysMD(input = """
                feature a: ScalarValues::Real = 2.0;
                feature b: ScalarValues::Boolean = a hastype ScalarValues::Real;
                """)
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val b = global.resolve<Feature>("b") !!
        assertEquals(builder.True, b.variable!!.vectorQuantity.value)
    }

    @Test fun hastypeOperationTest2() = testSession {
        loadSysMD(input = """
                feature a: ScalarValues::Integer = 2;
                feature b: ScalarValues::Boolean = a hastype ScalarValues::Real;
                """)
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val b = global.resolve<Feature>("b") !!
        assertEquals(builder.False, b.variable!!.vectorQuantity.value)
    }

    @Test fun hastypeOperationTest3() = testSession {
        loadSysMD(input = """
                feature a: ScalarValues::Integer = 2;
                feature b: ScalarValues::Boolean = a hastype ScalarValues::ScalarValue;
                """)
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val b = global.resolve<Feature>("b") !!
        assertEquals(builder.True, b.variable!!.vectorQuantity.value)
    }

    @Test
    fun oneOfOperationTest3() = testSession {
        +"feature r: ScalarValues::Real = oneOf(1.0 .. 2.0);"
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val r = global.resolve<Feature>("r") !!
        assertEquals(Range(1.0 .. 2.0), r.variable!!.vectorQuantity.value.asAadd().getRange())
    }


    // TODO: Fix semantics of function all Of !
    @Test @Ignore
    fun allOfOperationTest4() = testSession {
        +"feature r: ScalarValues::Real(1.5) = allOf(1.0 .. 2.0);"
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val r = global.resolve<Feature>("r") !!
        assertEquals(Range(1.0 .. 2.0), r.variable!!.vectorQuantity.value.asAadd().getRange())
    }
}