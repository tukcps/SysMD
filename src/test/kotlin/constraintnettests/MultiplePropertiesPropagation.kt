package constraintnettests

import com.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.resolve.resolveVar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class MultiplePropertiesPropagation {

    /**
     * Updates of properties can be observed by two flags:
     * - updated must be re-set explicitly to false; any change of it will set it to true.
     * - stable is set to false if in the last computation of it there was no change in its value.
     */
    @Test
    fun updateTest()  = testSession("ScalarValues") {
        loadSysMD("""
            Global hasA Value a: ScalarValues::Real(1.3).
            Global hasA Value b: ScalarValues::Real = a.""")
        global.resolveVar("b")!!.updated = false
        // 1st call of evalUp is done instantly after compiler run; might be 2 ..
        // assertEquals(false, resolveName<Expression>("b").stable)
        propagate()
        assertEquals(true, global.resolveVar("b")!!.stable)
        assertEquals(true, global.resolveVar("b")!!.updated)
    }

    /**
     * Dependencies across properties: direct dependency, forward.
     */
    @Test
    fun upTest1() = testSession {
        loadSysMD("""
            feature x: ScalarValues::Real(1 .. 10).
            feature y: ScalarValues::Real(1 .. 100) = x.
            feature z: ScalarValues::Real = y.""")
        // y should be 1 .. 10 via y = x.
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        assertEquals(10.0, global.resolveVar("y")!!.max(), 0.000001)
    }

    /**
     * Propagation in upwards direction, from right side to left over several variables.
     */
    @Test
    fun upTest2() = testSession {
        loadSysMD(
            """feature z: ScalarValues::Real = y.
            feature y: ScalarValues::Real(1 .. 100) = x.
            feature x: ScalarValues::Real(1 .. 10) = c.
            feature c: ScalarValues::Real(1 .. 10) = 2.0.
            // y should be 1 .. 10 via y = x."""
        )
        propagate()
        assertEquals(Range(2.0..2.0), global.resolveVar("y")!!.vectorQuantity.aadd().getRange())
    }

    /**
     * Dependencies across properties: reverse dependency.
     * reversed dependency
     */
    @Test
    fun downTest1() = testSession {
        loadSysMD(
            """
            feature x: ScalarValues::Real(1 .. 100).
            feature y: ScalarValues::Real(1 .. 10) = x."""
        )
        // y should be 1 .. 10 via y = x
        propagate()
        assertEquals(10.0, global.resolveVar("x")!!.vectorQuantity.aadd().getRange().max, 0.000001)
    }
}