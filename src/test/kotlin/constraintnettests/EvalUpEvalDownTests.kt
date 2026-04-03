package constraintnettests

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
}