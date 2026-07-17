package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class EvalUpEvalDownTests {

    /**
     *  The Subtype of a range constrains the value of a variable:
     *  - the variable y is constrained to 1 .. 2,
     *  - the variable x is a free variable.
     *  Expected result:
     *  - y becomes 1..2, x becomes 1..2 if solved.
     */
    @Test
    fun considerSubtypeConstraintTest() = testSession("Ranges") {
        loadKerML("""
            feature x: ScalarValues::Real; 
            feature y: Ranges::RealInRange = x { :>> range = "1.0 .. 2.0";}"""
        )
        assertNoIssues()
        solver.propagate()
        // now, both x and y must be 1..2
        assertEquals(1.0, global.resolveVar("y")!!.min(), 0.00001)
        assertEquals(2.0, global.resolveVar("y")!!.max(), 0.00001)
        assertEquals(1.0, global.resolveVar("x")!!.min(), 0.00001)
        assertEquals(2.0, global.resolveVar("x")!!.max(), 0.00001)
    }

    /**
     * The subtype constraint is considered when computing the resulting interval.
     * y i constrained to 1.0 .. 2.0, x to 1.5 .. 2.5; result must be 1.5 .. 2.5, which
     * is the intersection of 1.00 .. 2.0 and 1.5 .. 2.5
     */
    @Test
    fun considerSubtypeConstraintTestIntersection()  = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange { :>> range = "1.5 .. 2.5";}
            feature y: Ranges::RealInRange = x { :>> range = "1.0 .. 2.0";}"""
        )
        assertNoIssues()
        solver.propagate()
        assertEquals(1.5, global.resolveVar("y")!!.min(), 0.00001)
        assertEquals(2.0, global.resolveVar("y")!!.max(), 0.00001)
        assertEquals(1.5, global.resolveVar("x")!!.min(), 0.00001)
        assertEquals(2.0, global.resolveVar("x")!!.max(), 0.00001)
    }

    /**
     * The subtype constraint is considered when computing the resulting interval also for integers.
     */
    @Test
    fun considerSubtypeConstraintTestIntersectionInt()  = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::IntegerInRange { :>> range = "1 .. 3";}
            feature y: Ranges::IntegerInRange = x { :>> range = "2 .. 4";}"""
        )
        assertNoIssues()
        solver.propagate()
        assertEquals(2L, global.resolveVar("y")!!.min())
        assertEquals(3L, global.resolveVar("y")!!.max())
        assertEquals(2L, global.resolveVar("x")!!.min())
        assertEquals(3L, global.resolveVar("x")!!.max())
    }

    /**
     * Eval-down propagation with ScalarValues::Real and sum constraint.
     * Tests that a + b = sum where sum is in range 9..10 and b is in range 3..5,
     * constrains a to 4..7.
     */
    @Test fun evalDownReal() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature a: ScalarValues::Real; 
            feature b: Ranges::RealInRange {:>> range = "3..5";}
            feature sum: Ranges::RealInRange = a+b {:>> range = "9..10";}
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(9.0, solver.getVariable("sum")!!.aadd().min, 0.00001)
        assertEquals(10.0, solver.getVariable("sum")!!.aadd().max, 0.000001)
        assertEquals(4.0, solver.getVariable("a")!!.aadd().getRange().min,0.0001)
        assertEquals(7.0, solver.getVariable("a")!!.aadd().getRange().max,0.0001)
        assertEquals(3.0, solver.getVariable("b")!!.aadd().getRange().min,0.0001)
        assertEquals(5.0, solver.getVariable("b")!!.aadd().getRange().max,0.0001)
    }

    /**
     * Eval-down propagation with ScalarValues::Integer and integer sum constraint.
     * Integers do not well deal with overflows — one overflow breaks the whole computation chain.
     */
    @Test fun evalDownInt() = testSession("ScalarValues", "Ranges") {
        loadKerML(""" 
           feature a: ScalarValues::Integer;
           feature b: Ranges::IntegerInRange {:>> range = "3..5";}
           feature sum: Ranges::IntegerInRange = a+b {:>> range = "9..10";}
           """)
        solver.propagate()
        assertEquals(9, solver.getVariable("sum")!!.idd().getRange().min)
        assertEquals(10, solver.getVariable("sum")!!.idd().getRange().max)
        assertEquals(4, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(7, solver.getVariable("a")!!.idd().getRange().max)
        assertEquals(3, solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(5, solver.getVariable("b")!!.idd().getRange().max)
    }

    @Test
    fun considerSubtypeConstraintTestIntersectionNegative()  = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange { :>> range = "-2.5 .. -1.5";}
            feature y: Ranges::RealInRange = x { :>> range = "-2.0 .. -1.0";}"""
        )
        assertNoIssues()
        solver.propagate()
        assertEquals(-2.0, global.resolveVar("y")!!.min(), 0.00001)
        assertEquals(-1.5, global.resolveVar("y")!!.max(), 0.00001)
        assertEquals(-2.0, global.resolveVar("x")!!.min(), 0.00001)
        assertEquals(-1.5, global.resolveVar("x")!!.max(), 0.00001)
    }

    @Test
    fun considerSubtypeConstraintTestIntersectionMixed()  = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange { :>> range = "-2.0 .. 2.0";}
            feature y: Ranges::RealInRange = x { :>> range = "-1.0 .. 3.0";}"""
        )
        assertNoIssues()
        solver.propagate()
        assertEquals(-1.0, global.resolveVar("y")!!.min(), 0.00001)
        assertEquals(2.0, global.resolveVar("y")!!.max(), 0.00001)
        assertEquals(-1.0, global.resolveVar("x")!!.min(), 0.00001)
        assertEquals(2.0, global.resolveVar("x")!!.max(), 0.00001)
    }

    @Test fun evalDownRealNegative() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature a: ScalarValues::Real; 
            feature b: Ranges::RealInRange {:>> range = "-5..-3";}
            feature sum: Ranges::RealInRange = a+b {:>> range = "-10..-9";}
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(-10.0, solver.getVariable("sum")!!.aadd().min, 0.00001)
        assertEquals(-9.0, solver.getVariable("sum")!!.aadd().max, 0.000001)
        assertEquals(-7.0, solver.getVariable("a")!!.aadd().getRange().min,0.0001)
        assertEquals(-4.0, solver.getVariable("a")!!.aadd().getRange().max,0.0001)
        assertEquals(-5.0, solver.getVariable("b")!!.aadd().getRange().min,0.0001)
        assertEquals(-3.0, solver.getVariable("b")!!.aadd().getRange().max,0.0001)
    }

    @Test fun evalDownIntNegative() = testSession("ScalarValues", "Ranges") {
        loadKerML(""" 
           feature a: ScalarValues::Integer;
           feature b: Ranges::IntegerInRange {:>> range = "-5..-3";}
           feature sum: Ranges::IntegerInRange = a+b {:>> range = "-10..-9";}
           """)
        solver.propagate()
        assertEquals(-10, solver.getVariable("sum")!!.idd().getRange().min)
        assertEquals(-9, solver.getVariable("sum")!!.idd().getRange().max)
        assertEquals(-7, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(-4, solver.getVariable("a")!!.idd().getRange().max)
        assertEquals(-5, solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(-3, solver.getVariable("b")!!.idd().getRange().max)
    }
}