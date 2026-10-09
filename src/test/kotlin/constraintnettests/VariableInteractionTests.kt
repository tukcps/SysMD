package constraintnettests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class VariableInteractionTests {

    /**
     * A property consists of the left-hand data (target variable, type and subtype of it),
     * and the dependency that is in the property field dependency and that can be
     * analyzed separately.
     */

    @Test
    fun evalUpPropertyDirectTest() = testSession("Ranges") {
        loadKerML("feature speed: Ranges::RealInRange = 5.0+6.0 {:>> range = 2.0 .. 22.0;}", Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val speed = solver.getVariable("speed")
        assertBounds(11.0 .. 11.0, speed!!)
    }

    @Test
    fun evalDownPropertyTest() = testSession("Ranges") {
         loadKerML("""
             feature speed2: Ranges::RealInRange {:>> range = 10.0 .. 10000.0;}
             feature speed:  Ranges::RealInRange = speed2 {:>> range = -100.0 ..200.0;}""")
        solver.propagate()
        assertNoIssues()
        val speed = solver.variable("speed")
        assertBounds(10.0 .. 200.0, speed)
    }
}
