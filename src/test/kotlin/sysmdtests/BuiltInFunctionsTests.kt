package sysmdtests

import io.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import org.junit.jupiter.api.Disabled
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuiltInFunctionsTests {

    /**
     *  The function getLeaves returns the leaves of the ast of a dependency expression.
     */
    @Test
    fun getLeavesTest() = testSession("ScalarValues") {
        loadKerML("""
            feature b: ScalarValues::Real = 2.0;
            feature c: ScalarValues::Real = 3.0;
            feature d: ScalarValues::Real = 4.0;
            feature a: ScalarValues::Real = b + c * d;
            """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val a = global.resolve<Feature>("a")
        assertEquals(3, a!!.variable!!.ast!!.getLeaves().size)
    }

    @Test fun hasAFunctionTest() = testSession("ScalarValues") {
        loadKerML(catchExceptions = false, input = """
            feature a: ScalarValues::Real = 2.0;
            feature b: ScalarValues::Boolean = hasA(Global, a);
            """)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val b = global.resolve<Feature>("b")!!
        assertEquals(builder.True, b.variable!!.vectorQuantity.value)
    }

    @Test fun hasAFunctionTest2() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Real = 2.0;
            feature b: ScalarValues::Boolean = hasA(Global, c).
            """)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val b = global.resolve<Feature>("b")!!
        assertEquals(builder.False, b.variable!!.vectorQuantity.value)
    }

    @Test fun hastypeOperationTest() = testSession("ScalarValues") {
        loadKerML(input = """
                feature a: ScalarValues::Real = 2.0;
                feature b: ScalarValues::Boolean = a hastype ScalarValues::Real;
                """)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val b = global.resolve<Feature>("b") !!
        assertEquals(builder.True, b.variable!!.vectorQuantity.value)
    }

    @Test fun hastypeOperationTest2() = testSession("ScalarValues") {
        loadKerML(input = """
                feature a: ScalarValues::Boolean;
                feature b: ScalarValues::Boolean = a hastype ScalarValues::Real;
                """)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val b = global.resolve<Feature>("b") !!
        assertEquals(builder.False, b.variable!!.vectorQuantity.value)
    }

    @Test fun hastypeOperationTest3() = testSession("ScalarValues") {
        loadKerML(input = """
                feature a: ScalarValues::Integer = 2;
                feature b: ScalarValues::Boolean = a hastype ScalarValues::ScalarValue;
                """)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val b = global.resolve<Feature>("b") !!
        assertEquals(builder.True, b.variable!!.vectorQuantity.value)
    }

    @Test
    fun oneOfOperationTest3() = testSession("ScalarValues") {
        loadKerML("feature r: ScalarValues::Real = oneOf(1.0 .. 2.0);")
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val r = global.resolve<Feature>("r") !!
        assertEquals(Range(1.0 .. 2.0), r.variable!!.vectorQuantity.value.asAadd().getRange())
    }


    // TODO: Fix semantics of function all Of !
    @Test @Ignore
    fun allOfOperationTest4() = testSession("ScalarValues") {
        loadKerML("feature r: ScalarValues::Real(1.5) = allOf(1.0 .. 2.0);")
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val r = global.resolve<Feature>("r") !!
        assertEquals(Range(1.0 .. 2.0), r.variable!!.vectorQuantity.value.asAadd().getRange())
    }


    @Test
    fun oneOfOperationTest() = testSession("ScalarValues") {
        loadKerML("feature r: ScalarValues::Real(1.0) = oneOf(1.0 .. 2.0);")
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val r = global.resolve<Feature>("r") !!
        assertEquals(Range(1.0 .. 1.0), r.variable!!.vectorQuantity.value.asAadd().getRange())
    }

    @Test @Disabled //TODO: Fix semantics of anyOf!
    fun anyOfOperationTest() = testSession("ScalarValues") {
        loadKerML("feature r: ScalarValues::Real = anyOf(1.0 .. 2.0);")
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val r = global.resolve<Feature>("r") !!
        assertEquals(Range(1.0 .. 2.0), r.variable!!.vectorQuantity.value.asAadd().getRange())
    }
}
