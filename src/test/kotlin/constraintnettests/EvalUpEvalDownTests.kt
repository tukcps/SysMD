package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
    fun considerSubtypeConstraintTest() = testSession {
        loadSysMD(
            """
            feature x: ScalarValues::Real; 
            feature y: ScalarValues::Real(1.0 .. 2.0) = x;"""
        )
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        // now, both x and y must be 1..2
        assertEquals(1.0, global.resolve<Feature>("y")!!.variable!!.min(), 0.00001)
        assertEquals(2.0, global.resolve<Feature>("y")!!.variable!!.max(), 0.00001)
        assertEquals(1.0, global.resolve<Feature>("x")!!.variable!!.min(), 0.00001)
        assertEquals(2.0, global.resolve<Feature>("x")!!.variable!!.max(), 0.00001)
    }

    /**
     * The subtype constraint is considered when computing the resulting interval.
     * y i constrained to 1.0 .. 2.0, x to 1.5 .. 2.5; result must be 1.5 .. 2.5, which
     * is the intersection of 1.00 .. 2.0 and 1.5 .. 2.5
     */
    @Test
    fun considerSubtypeConstraintTestIntersection()  = testSession {
        loadSysMD(
            """
            feature x: ScalarValues::Real(1.5 .. 2.5).
            feature y: ScalarValues::Real(1.0 .. 2.0) = x."""
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(1.5, global.resolve<Feature>("y")!!.variable!!.aadd().getRange().min, 0.00001)
        assertEquals(2.0, global.resolve<Feature>("y")!!.variable!!.aadd().getRange().max, 0.00001)
        assertEquals(1.5, global.resolve<Feature>("x")!!.variable!!.aadd().getRange().min, 0.00001)
        assertEquals(2.0, global.resolve<Feature>("x")!!.variable!!.aadd().getRange().max, 0.00001)
    }

    /**
     * The subtype constraint is considered when computing the resulting interval also for integers.
     */
    @Test
    fun considerSubtypeConstraintTestIntersectionInt()  = testSession("ScalarValues") {
        loadSysMD(
            """
            feature x: ScalarValues::Integer(1 .. 3).
            feature y: ScalarValues::Integer(2 .. 4) = x."""
        )
        propagate()
        assertEquals(2, global.resolve<Feature>("y")!!.variable!!.idd().getRange().min)
        assertEquals(3, global.resolve<Feature>("y")!!.variable!!.idd().getRange().max)
        assertEquals(2, global.resolve<Feature>("x")!!.variable!!.idd().getRange().min)
        assertEquals(3, global.resolve<Feature>("x")!!.variable!!.idd().getRange().max)
    }
}