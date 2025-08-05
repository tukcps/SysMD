package constraintnettests

import io.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import util.testSession
import kotlin.test.assertTrue

class UpdateNotificationsTests {

    /**
     * The map 'status.updates' holds key/value pairs of all changes.
     * It must be reset explicitly.
     */
    @Test @Disabled //TODO: Fix updated variables
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
                status.updatedValues[it.elementId!!] = it.variable?.vectorQuantity.toString()
        }
        status.updatedValues.clear()
        propagate() // No additional updates.
        assertEquals(1, status.updatedValues.size) // No additional updates, all stable
        propagate()
        status.updatedValues.clear()
        get().forEach { it.updated = false }
        // Change a variable
        loadKerML("feature p1: ScalarValues::Real(2.0 ..3.0);")
        assertEquals(1, status.updatedValues.size)
        propagate()
        assertEquals(2, status.updatedValues.size)
    }

    @Test
    fun updateNotificationTest2() = testSession("ScalarValues", "Math", "Ranges") {
        loadKerML("""
            feature p: ScalarValues::Real = 1.0 + Math::pi + Math::e;
            feature x: ScalarValues::Real; 
            feature y: Ranges::RealInRange = x + p {:>> range = "2.0";}
        """)

        assertEquals(true, global.resolve<Feature>("x")!!.variable!!.updated)
        assertEquals(true, global.resolve<Feature>("p")!!.variable!!.updated)
        assertEquals(true, global.resolve<Feature>("y")!!.variable!!.updated)

        propagate()
        assertEquals(true, global.resolve<Feature>("x")!!.variable!!.updated)
        assertTrue(global.resolve<Feature>("x")!!.variable!!.vectorQuantity.values.first().asAadd().getRange() in Range(-4.87..-4.85))
    }
}