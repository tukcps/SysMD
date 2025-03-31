package constraintnettests

import io.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import util.testSession
import kotlin.test.assertTrue

class UpdateNotificationsTests {

    /**
     * The map 'status.updates' holds key/value pairs of all changes.
     * It must be reset explicitly.
     */
    @Test
    fun updateNotificationTest() = testSession("ScalarValues") {
        loadKerML(""" 
                feature p1: ScalarValues::Real {:>> range = "1.0 ..6.0";}
                feature p2: ScalarValues::Real(7.0);
                feature p3: ScalarValues::Real = p1+p2;
                """)
            // Just collect the "updates" without calling the method propagate.
         get().filterIsInstance<Feature>().forEach {
            if (it.variable?.updated == true)
                status.updates[it.elementId!!] = it.variable?.vectorQuantity.toString()
        }
        status.updates.clear()
        // println(status.updates)
        // 8 updates might be there from the packages; p1, p2 eventually. Min. p3.
        // Number might vary depending on extensions of packages.
        //  assertEquals(3, status.updates.size)
        propagate() // No additional updates.
        assertEquals(1, status.updates.size) // No additional updates, all stable
        propagate()
        status.updates.clear()
        get().forEach { it.updated = false }
        // Change a variable
        loadKerML("feature p1: ScalarValues::Real {:>> range = \"2.0 ..3.0\";}")
        assertEquals(0, status.updates.size)
        propagate()
        assertEquals(1, status.updates.size)
    }

    @Test
    fun updateNotificationTest2() = testSession("ScalarValues", "Math") {
        loadKerML("""
            feature p: ScalarValues::Real = 1.0 + Math::pi + Math::e;
            feature x: ScalarValues::Real; 
            feature y: ScalarValues::Real(2.0) = x + p;  
        """)

        assertEquals(true, global.resolve<Feature>("x")!!.variable!!.updated)
        assertEquals(true, global.resolve<Feature>("p")!!.variable!!.updated)
        assertEquals(true, global.resolve<Feature>("y")!!.variable!!.updated)

        propagate()
        assertEquals(true, global.resolve<Feature>("x")!!.variable!!.updated)
        assertTrue(global.resolve<Feature>("x")!!.variable!!.vectorQuantity.values.first().asAadd().getRange() in Range(-4.87..-4.85))
    }
}