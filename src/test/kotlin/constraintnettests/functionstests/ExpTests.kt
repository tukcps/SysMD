package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.*
import util.mockup.loadKerML
import kotlin.math.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpTests {
    /** ConstNet shall compute bottom-up with exp in real and model.builder.range */
    @Test
    fun evalUpWithExp_real_range() = testSession("ScalarValues") {
        loadKerML("feature b: ScalarValues::Real = exp([1.0 .. 5.0]);")
        solver.propagate()
        assertNoIssues()
        assertNoIssues()
        assertBounds(Math.E .. Math.E.pow(5), solver.variable("b"))
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in real and value */
    @Test
    fun evalUpWithExp_real_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = 3.0 {:>> range = 1.0 .. 5.0;}
            feature b: ScalarValues::Real = exp(a);""", Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertNoIssues()
        val e3 = Math.E.pow(3)
        assertBounds(e3 .. e3, solver.variable("b"))
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }


    /** ConstNet shall compute bottom-up with pow2 with negative value*/
    @Test
    fun evalUpWithExp_real_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -3.0 .. -1.0;}
            feature b: ScalarValues::Real = exp(a);""", Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertNoIssues()
        val x = Math.E.pow(-3)
        val y = Math.E.pow(-1)
        val b = solver.variable("b")
        assertBounds(x..y, b)
        assertEquals("1", b.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 and zero */
    @Test
    fun evalUpWithExp_real_zero() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 0.0;}
            feature b: ScalarValues::Real = exp(a);""", Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertNoIssues()
        assertBounds(1.0 .. 1.0, solver.variable("b"))
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with exp in int and model.builder.range */
    @Test
    fun evalUpWithExp_int_range() = testSession("Ranges") {
        loadKerML("feature b: ScalarValues::Integer = exp([1 .. 5]).", Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(
            floor(Math.E).toLong() .. ceil(Math.E.pow(5)).toLong(),
            solver.variable("b"),
        )
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in int and value */
    @Test
    fun evalUpWithExp_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange  = 3 {:>> range = 1 .. 5;}
            feature b: ScalarValues::Integer = exp(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertNoIssues()
        val e3 = Math.E.pow(3)
        assertBounds(
            floor(e3).toLong() ..ceil(e3).toLong(),
            solver.variable("b")
        )
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }

    @Test
    fun evalDownWithExp_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange  {:>> range = 0 .. 5;}
            feature b: Ranges::IntegerInRange = exp(a) {:>> range = 1 .. 1;} 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0L .. 0L, solver.variable("a"))
        assertEquals("1", solver.variable("a").vectorQuantity.unit.toString())
    }

    @Test
    fun evalUpWithExp_int_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange  = -2 {:>> range = -5 .. -1;}
            feature b: ScalarValues::Integer = exp(a);
        """, Runlevel.ALL
        )
        solver.propagate()
        assertNoIssues()
        val en2 = Math.E.pow(-2)
        assertBounds(
            floor(en2).toLong() .. ceil(en2).toLong(),
            solver.variable("b")
        )
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }

    @Test
    fun evalUpWithExp_int() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = 3 .. 3;}
            feature b: ScalarValues::Integer = exp(a);
        """, Runlevel.ALL
        )
        solver.propagate()
        assertNoIssues()
        assertNoIssues()
        assertNoIssues()
        val e3 = Math.E.pow(3)
        assertBounds(floor(e3).toLong() .. ceil(e3).toLong(), solver.variable("b"))
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }

    @Test
    fun evalDownWithExp_negative_real() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -10.0 .. 10.0;}
            feature b: Ranges::RealInRange = exp(a) {:>> range = -5.0 .. -1.0;}
        """)
        solver.propagate()
        val b = solver.variable("b")
        assertEmpty(b)
    }

    @Test
    fun evalDownWithExp_negative_int() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = -10 .. 10;}
            feature b: Ranges::IntegerInRange = exp(a) {:>> range = -5 .. -1;}
        """)
        solver.propagate()
        val a = solver.variable("a")
        assertEmpty(a)
    }
}
