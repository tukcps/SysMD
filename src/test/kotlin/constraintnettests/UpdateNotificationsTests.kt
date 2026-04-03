package constraintnettests

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.Range
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateNotificationsTests {

    /**
     * The map 'status.updates' holds key/value pairs of all changes.
     * It must be reset explicitly.
     */
    @Test
    @Ignore //TODO: Fix updated variables
    fun updateNotificationTest() = testSession("ScalarValues", "Ranges") {
        loadKerML(""" 
                feature p1: Ranges::RealInRange {:>> range = "1.0 ..6.0";}
                feature p2: Ranges::RealInRange {:>> range = "7.0";}
                feature p3: ScalarValues::Real = p1+p2;
        """)
        initialize()
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
            feature y: Ranges::RealInRange = x + p {:>> range = "2.0";}
        """)

        assertEquals(true, global.resolveVar("x")?.updated)
        assertEquals(true, global.resolveVar("p")?.updated)
        assertEquals(true, global.resolveVar("y")?.updated)

        solver.propagate()
        assertEquals(true, global.resolveVar("x")?.updated)
        assertTrue(global.resolveVar("x")!!.vectorQuantity.values.first().asAadd().getRange() in Range(-4.87..-4.85))
    }
}