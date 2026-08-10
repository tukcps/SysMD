package models.kerml

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureTests {
    /**
     * Serialization and de-serialization of type constraint and unit constraint.
     */
    @Test
    fun testFeature() = testSession("ISQ") {
        loadKerML("feature f: ISQ::LengthValue { :>> range = 1..2 [m] }", Runlevel.MODEL)
        assertNoIssues()

        val f = global.resolve("f")!!.member<Feature>()!!
        assertEquals("f", f.declaredName)
        val range = f.getOwnedElementOfType<Feature>()!!
        assertEquals("range", range.name)
        assertEquals(1, f.typeConstraint.size)

        assertEquals("Declared name saved incorrectly", "f", f.declaredName)
        assertTrue(f.isFeatureWithValue() )
        assertEquals("Unit saved incorrectly", "m", f.unitConstraint)
    }
}