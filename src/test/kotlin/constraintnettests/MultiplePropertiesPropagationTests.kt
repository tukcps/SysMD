package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.Range
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class MultiplePropertiesPropagationTests {

    @Test
    fun update()  = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "1.3 .. 1.3";}
            feature b: ScalarValues::Real = a;
        """, Runlevel.VARIABLES)
        assertNoIssues()
        solver.getVariable("b")!!.updated = false
        solver.propagate()
        assertEquals(true, global.resolveVar("b")!!.stable)
        assertEquals(true, global.resolveVar("b")!!.updated)
    }

    @Test
    fun up1() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = "1 .. 10";}
            feature y: Ranges::RealInRange = x {:>> range = "1 .. 100";}
            feature z: Ranges::RealInRange = y;
        """, Runlevel.ALL)
        assertNoIssues()
        val y = solver.getVariable("y")
        assertEquals(10.0, y!!.max(), 0.000001)
    }

    @Test
    fun up2() = testSession("ScalarValues") {
        loadKerML("""
            feature z: ScalarValues::Real = y;
            feature y: ScalarValues::Real = x {:>> range = "1 .. 100";}
            feature x: ScalarValues::Real = c {:>> range = "1 .. 10";}
            feature c: ScalarValues::Real = 2.0 {:>> range = "1 .. 10";}
        """)
        solver.propagate()
        assertEquals(Range(2.0..2.0), global.resolveVar("y")!!.vectorQuantity.aadd().getRange())
    }

    @Test
    fun down1() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = "1 .. 100";}
            feature y: Ranges::RealInRange = x {:>> range = "1 .. 10";}
        """)
        solver.propagate()
        assertEquals(10.0, global.resolveVar("x")!!.vectorQuantity.aadd().getRange().max, 0.000001)
    }

    @Test
    fun up1Negative() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = "-10 .. -1";}
            feature y: Ranges::RealInRange = x {:>> range = "-100 .. -1";}
            feature z: Ranges::RealInRange = y;
        """, Runlevel.ALL)
        assertNoIssues()
        val y = solver.getVariable("y")
        assertEquals(-1.0, y!!.max(), 0.000001)
    }

    @Test
    fun down1Negative() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = "-100 .. -1";}
            feature y: Ranges::RealInRange = x {:>> range = "-10 .. -1";}
        """)
        solver.propagate()
        assertEquals(-1.0, global.resolveVar("x")!!.vectorQuantity.aadd().getRange().max, 0.000001)
    }
}
