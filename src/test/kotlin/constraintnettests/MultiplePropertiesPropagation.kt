package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.Range
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals


class MultiplePropertiesPropagation {

    /**
     * Updates of properties can be observed by two flags:
     * - updated must be re-set explicitly to false; any change of it will set it to true.
     * - stable is set to false if in the last computation of it there was no change in its value.
     */
    @Test
    fun updateTest()  = testSession( "Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "1.3 .. 1.3";}
            feature b: ScalarValues::Real = a;
        """, Runlevel.VARIABLES)
        assertNoIssues()
        solver.getVariable("b")!!.updated = false
        // 1st call of evalUp is done instantly after compiler run; might be 2.
        // assertEquals(false, resolveName<Expression>("b").stable)
        solver.propagate()
        assertEquals(true, global.resolveVar("b")!!.stable)
        assertEquals(true, global.resolveVar("b")!!.updated)
    }

    /**
     * Dependencies across properties: direct dependency, forward.
     */
    @Test
    fun upTest1() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = "1 .. 10";}
            feature y: Ranges::RealInRange = x {:>> range = "1 .. 100";}
            feature z: Ranges::RealInRange = y;
        """, Runlevel.ALL)
        // y should be 1 .. 10 via y = x.
        assertNoIssues()
        val y = solver.getVariable("y")
        assertEquals(10.0, y!!.max(), 0.000001)
    }

    /**
     * Propagation in upwards direction, from right side to left over several variables.
     */
    @Test
    fun upTest2() = testSession("ScalarValues") {
        loadKerML("""
            feature z: ScalarValues::Real = y;
            feature y: ScalarValues::Real = x {:>> range = "1 .. 100";}
            feature x: ScalarValues::Real = c {:>> range = "1 .. 10";}
            feature c: ScalarValues::Real = 2.0 {:>> range = "1 .. 10";}
            // y should be 1 .. 10 via y = x.
        """)
        solver.propagate()
        assertEquals(Range(2.0..2.0), global.resolveVar("y")!!.vectorQuantity.aadd().getRange())
    }

    /**
     * Dependencies across properties: reverse dependency.
     * reversed dependency
     */
    @Test
    fun downTest1() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = "1 .. 100";}
            feature y: Ranges::RealInRange = x {:>> range = "1 .. 10";}
        """)
        // y should be 1 .. 10 via y = x
        solver.propagate()
        assertEquals(10.0, global.resolveVar("x")!!.vectorQuantity.aadd().getRange().max, 0.000001)
    }
}