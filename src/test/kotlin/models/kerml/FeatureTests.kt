package models.kerml

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureTypingImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.toTextualRepresentation
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test
import kotlin.test.assertTrue

class FeatureTests {
    @Test
    fun testFeature() = testSession {
        val feature = FeatureImplementation()
        feature.declaredName = "f"
        feature.unitConstraint = "m"
        feature.addOwnedElement(FeatureTypingImplementation(typedFeature = Resolved(feature), type = Resolved(repo.realType!!)))
        val sysmlv2 = feature.toTextualRepresentation()
        loadSysMD(sysmlv2!!)
        val f = global.resolve<Feature>("f")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals("Declared name saved incorrectly", "f", f?.declaredName)
        assertTrue(f?.isFeatureWithValue() == true )
        assertEquals("Unit saved incorrectly", "m", f?.unitConstraint)
    }
}