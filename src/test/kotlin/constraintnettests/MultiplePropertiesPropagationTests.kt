package constraintnettests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class MultiplePropertiesPropagationTests {

    @Test
    fun update()  = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1.3 .. 1.3;}
            feature b: ScalarValues::Real = a;
        """, Runlevel.VARIABLES)
        assertNoIssues()
        solver.variable("b").updated = false
        solver.propagate()
        assertEquals(true, solver.variable("b").stable)
        assertEquals(true, solver.variable("b").updated)
    }

    @Test
    fun up1() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = 1 .. 10;}
            feature y: Ranges::RealInRange = x {:>> range = 1 .. 100;}
            feature z: Ranges::RealInRange = y;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val y = solver.getVariable("y")
        assertBounds(1.0..10.0, y!!)
    }

    @Test
    fun up2() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature z: Ranges::RealInRange = y;
            feature y: Ranges::RealInRange = x {:>> range = 1 .. 100;}
            feature x: Ranges::RealInRange = c {:>> range = 1 .. 10;}
            feature c: Ranges::RealInRange = 2.0 {:>> range = 1 .. 10;}
            // y should be 1 .. 10 via y = x.
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(2.0 .. 2.0, solver.variable("y"))
    }

    @Test
    fun down1() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = 1 .. 100;}
            feature y: Ranges::RealInRange = x {:>> range = 1 .. 10;}
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0..10.0, solver.variable("x").vectorQuantity.aadd())
    }

    @Test
    fun up1Negative() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = -10 .. -1;}
            feature y: Ranges::RealInRange = x {:>> range = -100 .. -1;}
            feature z: Ranges::RealInRange = y;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val y = solver.getVariable("y")
        assertBounds(-10.0..-1.0, y!!)
    }

    @Test
    fun down1Negative() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = -100 .. -1;}
            feature y: Ranges::RealInRange = x {:>> range = -10 .. -1;}
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(-10.0..-1.0, solver.variable("x").vectorQuantity.aadd())
    }
}
