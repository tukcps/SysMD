package models.kerml

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureTypingImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.toTextualRepresentation
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test
import kotlin.test.assertTrue

class FeatureTests {
    /**
     * Serialization and de-serialization of type constraint and unit constraint.
     * Goes in 3.0 into the body of expression.
     */
    @Test
    fun testFeature() = testSession("ISQ") {
        val feature = FeatureImplementation(declaredName = "f")
        addOwnedMember(feature, global)
        addOwnedMember(FeatureImplementation("unit").also { it.expression="\"m\"" }, feature)
        val type = FeatureTypingImplementation(typedFeature = feature, type = global.resolve("ISQ::LengthValue")?.member()!!)
        addOwnedRelationship(type, feature)
        val kerml = feature.toTextualRepresentation()
        loadKerML(kerml)
        val f = global.resolve("f")?.memberElement as Feature?
        assertNoIssues()
        assertEquals("Declared name saved incorrectly", "f", f?.declaredName)
        assertTrue(f?.isFeatureWithValue() !! )
        assertEquals("Unit saved incorrectly", "m", f.unitConstraint)
    }
}