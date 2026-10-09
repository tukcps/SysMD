package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.dd.IDD
import util.*
import util.mockup.loadKerML
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class Power2Tests {
    /** ConstNet shall compute bottom-up with pow2 in real and model.builder.range */
    @Test
    fun evalUpWithPow2_real_range() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1.0 .. 5.0;}
            feature b: ScalarValues::Real = power2(a);
        """, Runlevel.ALL)
        val b = solver.variable("b")
        solver.propagate()
        assertNoIssues()
        assertBounds(2.0 .. 32.0, solver.variable("b"))
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with pow2 in real and value */
    @Test
    fun evalUpWithPow2_real_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = 3.0 {:>> range = 1.0 .. 5.0;}
            feature b: ScalarValues::Real = power2(a); 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(8.0 .. 8.0, solver.variable("b"))
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }

    @Test
    fun evalDownWithPow2_real_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1.0 .. 5.0;}
            feature b: Ranges::RealInRange = power2(a) {:>> range = 8.0 .. 8.0;} """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(3.0 .. 3.0, solver.variable("a"))
        assertEquals("1", solver.variable("a").vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 and negative values*/
    @Test
    fun evalUpWithPow2_real_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -2.0 .. -1.0;}
            feature b: ScalarValues::Real = power2(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.25 .. 0.5, solver.variable("b"))
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 with zero*/
    @Test
    fun evalUpWithPow2_real_zero() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = 0.0 .. 0.0; }
            feature b: ScalarValues::Real = power2(a); 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0 .. 1.0, solver.variable("b"))
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in int and model.builder.range*/
    @Test
    fun evalUpWithPow2_int_range() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = 1 .. 5;}
            feature b: ScalarValues::Integer = power2(a); 
        """, Runlevel.ALL)
        val b = solver.variable("b")
        solver.propagate()
        assertNoIssues()
        assertBounds(2L .. 32L, b)
        assertIs<IDD.Leaf>(b.idd())
        assertEquals("1", b.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in int and model.builder.range*/
    @Test
    fun evalUpWithPow2_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange = 3 {:>> range = 1 .. 5;}
            feature b: ScalarValues::Integer = power2(a); """
        )
        assertNoIssues()
        solver.propagate()

        val b = solver.variable("b")
        assertBounds(8L .. 8L, b)
        assertIs<IDD.Leaf>(b.idd())
        assertEquals("1", b.vectorQuantity.unit.toString())
    }

    @Test
    fun evalUpWithPow2_int_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = -3 .. -1;}
            feature b: ScalarValues::Integer = power2(a); 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(floor(0.125).toLong() .. ceil(0.5).toLong(), solver.variable("b"))
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }

    @Test
    fun evalUpWithPow2_int_negative2() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = -4 .. -2;}
            feature b: ScalarValues::Integer = power2(a); """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(floor(0.0625).toLong() .. ceil(0.25).toLong(), solver.variable("b"))
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun evalUpWithPow2_real_mixed() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = -2.0 .. 2.0; }
            feature b: ScalarValues::Real = power2(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.25 .. 4.0, solver.variable("b"))
    }

    @Test
    fun evalUpWithPow2_int_mixed() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange { :>> range = -2 .. 2; }
            feature b: ScalarValues::Integer = power2(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0L .. 4L, solver.variable("b"))
    }

    @Test
    fun evalDownWithPow2_negative_real() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -10.0 .. 10.0;}
            feature b: Ranges::RealInRange = power2(a) {:>> range = -5.0 .. -1.0;}
        """)
        solver.propagate()
        val b = solver.variable("b")
        assertEmpty(b)
    }

    @Test
    fun evalDownWithPow2_negative_int() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = -10 .. 10;}
            feature b: Ranges::IntegerInRange = power2(a) {:>> range = -5 .. -1;}
        """)
        solver.propagate()
        val a = solver.variable("a")
        assertEmpty(a)
    }
}
