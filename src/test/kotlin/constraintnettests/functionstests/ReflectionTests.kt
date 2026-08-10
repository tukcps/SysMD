package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class ReflectionTests {

    @Test
    fun isTypeTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = 1.0;
            feature b1: ScalarValues::Boolean = a istype ScalarValues::Real;
            feature b2: ScalarValues::Boolean = a istype ScalarValues::Integer;
        """)
        solver.propagate()
        assertNoIssues()
        val b1 = solver.getVariable("b1")!!
        val b2 = solver.getVariable("b2")!!
        assertEquals(builder.True, b1.vectorQuantity.value.asBdd())
        assertEquals(builder.False, b2.vectorQuantity.value.asBdd())
    }

    @Test
    fun hasTypeTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = 1.0;
            feature b1: ScalarValues::Boolean = a hastype ScalarValues::Real;
            feature b2: ScalarValues::Boolean = a hastype ScalarValues::Integer;
        """)
        solver.propagate()
        assertNoIssues()
        val b1 = solver.getVariable("b1")!!
        val b2 = solver.getVariable("b2")!!
        assertEquals(builder.True, b1.vectorQuantity.value.asBdd())
        assertEquals(builder.False, b2.vectorQuantity.value.asBdd())
    }

    @Test
    fun intersectRealTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1.0..5.0;}
            feature b: Ranges::RealInRange {:>> range = 3.0..7.0;}
            feature c: ScalarValues::Real = intersect(a, b);
        """, Runlevel.ALL)
        assertNoIssues()
        val c = solver.getVariable("c")!!
        assertEquals(3.0, c.min(), 0.0001)
        assertEquals(5.0, c.max(), 0.0001)
    }

    @Test
    fun intersectIntTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = 1..5;}
            feature b: Ranges::IntegerInRange {:>> range = 3..7;}
            feature c: ScalarValues::Integer = intersect(a, b);
        """, Runlevel.ALL)
        assertNoIssues()
        val c = solver.getVariable("c")!!
        assertEquals(3L, c.min())
        assertEquals(5L, c.max())
    }
}
