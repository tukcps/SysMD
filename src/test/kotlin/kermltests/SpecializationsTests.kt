package kermltests

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ReferenceSubsetting
import com.github.tukcps.sysmd.model.kerml.getOwned
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class SpecializationsTests {
    @Test
    fun referenceSubsettingTest() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValues { datatype Integer; }
            feature referencedFeature; 
            feature referencingFeature references referencedFeature; 
        """.trimIndent())
        val f2 = global.getOwned<Feature>("referencingFeature")
        val reference = f2!!.getOwnedElementsOfType<ReferenceSubsetting>()
        assertTrue(reference.isNotEmpty())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}