package constraintnettests

import util.variable
import util.assertBounds
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import io.github.tukcps.aadd.util.Assertions.assertEquals
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateNotificationsTests {

    /**
     * The map 'status.updates' holds key/value pairs of all changes.
     * It must be reset explicitly.
     */
    @Test
    @Ignore //TODO: Fix updated variables
    fun updateNotificationTest() = testSession("Ranges") {
        loadKerML(""" 
            feature p1: Ranges::RealInRange {:>> range = "1.0 ..6.0";}
            feature p2: Ranges::RealInRange {:>> range = "7.0";}
            feature p3: ScalarValues::Real = p1+p2;
        """)
        initialize(Runlevel.ALL)
        // Just collect the "updates" without calling the method propagate.
        get().filterIsInstance<Feature>().forEach {
            if (it.variable?.updated == true)
                status.updatedValues[it.variable!!.path] = it.variable?.vectorQuantity.toString()
        }
        status.updatedValues.clear()
        solver.propagate() // No additional updates.
        assertEquals(1, status.updatedValues.size) // No additional updates, all stable
        solver.propagate()
        status.updatedValues.clear()
        get().forEach { it.updated = false }
        // Change a variable
        loadKerML("feature p1: ScalarValues::Real(2.0 ..3.0);")
        assertEquals(1, status.updatedValues.size)
        solver.propagate()
        assertEquals(2, status.updatedValues.size)
    }

    @Test
    fun updateNotificationTest2() = testSession("ScalarValues", "Math", "Ranges") {
        loadKerML("""
            feature p: ScalarValues::Real = 1.0 + Math::pi + Math::e;
            feature x: ScalarValues::Real; 
            feature y: Ranges::RealInRange = x + p { :>> range = 2.0; }
        """, Runlevel.VARIABLES)

        assertEquals(true, solver.getVariable("x")?.updated)
        assertEquals(true, solver.getVariable("p")?.updated)
        assertEquals(true, solver.getVariable("y")?.updated)

        solver.propagate()
        assertEquals(true, solver.getVariable("x")?.updated)
        assertBounds(-4.859874482048839 .. -4.859874482048838, solver.variable("x"))
    }
}