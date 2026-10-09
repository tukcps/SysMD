package constraintnettests.functionstests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
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
        val b1 = solver.variable("b1")
        val b2 = solver.variable("b2")
        assertEquals(builder.Bool.True, b1.vectorQuantity.value.asBdd())
        assertEquals(builder.Bool.False, b2.vectorQuantity.value.asBdd())
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
        val b1 = solver.variable("b1")
        val b2 = solver.variable("b2")
        assertEquals(builder.Bool.True, b1.vectorQuantity.value.asBdd())
        assertEquals(builder.Bool.False, b2.vectorQuantity.value.asBdd())
    }

    @Test
    fun intersectRealTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1.0..5.0;}
            feature b: Ranges::RealInRange {:>> range = 3.0..7.0;}
            feature c: ScalarValues::Real = intersect(a, b);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(3.0 .. 5.0, c)
    }

    @Test
    fun intersectIntTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = 1..5;}
            feature b: Ranges::IntegerInRange {:>> range = 3..7;}
            feature c: ScalarValues::Integer = intersect(a, b);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val c = solver.variable("c")
        assertBounds(3L .. 5L, c)
    }


    /** Regression test: intersect(a, b) = r only says r is within a and b; a and b must not be narrowed to r. */
    @Test
    fun intersectEvalDownDoesNotNarrowRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1.0..5.0;}
            feature b: Ranges::RealInRange {:>> range = 3.0..7.0;}
            feature c: Ranges::RealInRange = intersect(a, b) {:>> range = 3.0..4.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0 .. 5.0, solver.variable("a"))
        assertBounds(3.0 .. 7.0, solver.variable("b"))
    }
}
