package models.kerml

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureTypingImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.toTextualRepresentation
import com.github.tukcps.sysmd.services.resolve.resolve
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
    fun testFeature() = testSession("SI") {
        val feature = FeatureImplementation(declaredName = "f")
        addOwnedMember(feature, global)
        feature.unitConstraint = "m"
        val type = FeatureTypingImplementation(typedFeature = feature, type = global.resolve<Type>("SI::Length")!!)
        addOwnedRelationship(type, feature)
        val kerml = feature.toTextualRepresentation()
        loadKerML(kerml!!)
        val f = global.resolve<Feature>("f")
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals("Declared name saved incorrectly", "f", f?.declaredName)
        assertTrue(f?.isFeatureWithValue() == true )
        assertEquals("Unit saved incorrectly", "m", f.unitConstraint)
    }
}