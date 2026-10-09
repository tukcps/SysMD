package constraintnettests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals


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
            feature y: Ranges::RealInRange = x { :>> range = 1.0 .. 2.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        // now, both x and y must be 1..2
        assertBounds(1.0 .. 2.0, solver.variable("x"))
        assertBounds(1.0 .. 2.0, solver.variable("y"))
    }

    /**
     * The subtype constraint is considered when computing the resulting interval.
     * y i constrained to 1.0 .. 2.0, x to 1.5 .. 2.5; result must be 1.5 .. 2.5, which
     * is the intersection of 1.00 .. 2.0 and 1.5 .. 2.5
     */
    @Test
    fun considerSubtypeConstraintTestIntersection()  = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange { :>> range = 1.5 .. 2.5;}
            feature y: Ranges::RealInRange = x { :>> range = 1.0 .. 2.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(1.5 .. 2.0, solver.variable("x"))
        assertBounds(1.5 .. 2.0, solver.variable("y"))
    }

    /**
     * The subtype constraint is considered when computing the resulting interval also for integers.
     */
    @Test
    fun considerSubtypeConstraintTestIntersectionInt()  = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::IntegerInRange { :>> range = 1 .. 3;}
            feature y: Ranges::IntegerInRange = x { :>> range = 2 .. 4;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(2L .. 3L, solver.variable("x"))
        assertBounds(2L .. 3L, solver.variable("y"))
    }

    /**
     * Eval-down propagation with ScalarValues::Real and sum constraint.
     * Tests that a + b = sum where sum is in range 9..10 and b is in range 3..5,
     * constrains a to 4..7.
     */
    @Test fun evalDownReal() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature a: ScalarValues::Real; 
            feature b: Ranges::RealInRange {:>> range = 3..5;}
            feature sum: Ranges::RealInRange = a+b {:>> range = 9..10;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(9.0..10.0, solver.variable("sum"))
        assertBounds(4.0..7.0, solver.variable("a"))
        assertBounds(3.0..5.0, solver.variable("b"))
    }

    /**
     * Eval-down propagation with ScalarValues::Integer and integer sum constraint.
     * Integers do not well deal with overflows — one overflow breaks the whole computation chain.
     */
    @Test fun evalDownInt() = testSession("ScalarValues", "Ranges") {
        loadKerML(""" 
           feature a: ScalarValues::Integer;
           feature b: Ranges::IntegerInRange {:>> range = 3..5;}
           feature sum: Ranges::IntegerInRange = a+b {:>> range = 9..10;}
           """)
        solver.propagate()
        assertNoIssues()
        assertBounds(9L..10, solver.variable("sum"))
        assertBounds(4L..7, solver.variable("a"))
        assertBounds(3L..5, solver.variable("b"))
    }

    @Test
    fun considerSubtypeConstraintTestIntersectionNegative()  = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange { :>> range = -2.5 .. -1.5;}
            feature y: Ranges::RealInRange = x { :>> range = -2.0 .. -1.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(-2.0..-1.5, solver.variable("x"))
        assertBounds(-2.0..-1.5, solver.variable("y"))
    }

    @Test
    fun considerSubtypeConstraintTestIntersectionMixed()  = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange { :>> range = -2.0 .. 2.0;}
            feature y: Ranges::RealInRange = x { :>> range = -1.0 .. 3.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(-1.0..2.0, solver.variable("x"))
        assertBounds(-1.0..2.0, solver.variable("y"))
    }

    @Test fun evalDownRealNegative() = testSession("Ranges") {
        loadKerML("""
            feature a: ScalarValues::Real; 
            feature b: Ranges::RealInRange {:>> range = -5..-3;}
            feature sum: Ranges::RealInRange = a+b {:>> range = -10..-9;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(-10.0 .. -9.0, solver.variable("sum"))
        assertBounds(-7.0 .. -4.0, solver.variable("a"))
        assertBounds(-5.0 .. -3.0, solver.variable("b"))
    }

    @Test fun evalDownIntNegative() = testSession("Ranges") {
        loadKerML(""" 
           feature a: ScalarValues::Integer;
           feature b: Ranges::IntegerInRange {:>> range = -5..-3;}
           feature sum: Ranges::IntegerInRange = a+b {:>> range = -10..-9;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(-10L .. -9, solver.variable("sum"))
        assertBounds(-7L .. -4, solver.variable("a"))
        assertBounds(-5L .. -3, solver.variable("b"))
    }
}