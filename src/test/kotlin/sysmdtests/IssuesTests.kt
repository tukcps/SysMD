package sysmdtests

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class IssuesTests {

    // Todo: Check Requirements and only report subclasses iff all requirements are satisfied.
    @Test @Disabled
    fun kpiAvailabilityTest() = testSession {
        +"kpiAvailabilityTest isA Package."
        +"kpiAvailabilityTest defines A isA Component."
        +"kpiAvailabilityTest::A hasA quality: Real(0..100)"
        +"kpiAvailabilityTest defines B isA kpiAvailabilityTest::A."
        +"kpiAvailabilityTest::B hasA quality: Real(2..2)"
        +"kpiAvailabilityTest defines C isA kpiAvailabilityTest::A."
        +"kpiAvailabilityTest::C hasA quality: Real(5..5)"
        +"kpiAvailabilityTest::C hasA R1: Requirement = false"
        val aId = global.resolve<Element>("kpiAvailabilityTest::")
        assertNotNull(aId)
        // println(aId)
    }


    /**
     * TODO: Implement concept of KPI as discussed with AUDI.
     * Semantics: if a KPI exists in a component A, only subclasses are considered as
     * OK that satisfy the KPI.
     */
    @Test @Disabled
    fun kpiSelectionTest() = testSession {
        +"kpiSelectionTest isA Package."
        +"kpiSelectionTest defines A isA Component."
        +"kpiSelectionTest::A hasA quality: Real(0..100)"
        +"kpiSelectionTest defines B isA kpiSelectionTest::A."
        +"kpiSelectionTest::B hasA quality: Real(2..2)"
        +"kpiSelectionTest defines C isA kpiSelectionTest::A."
        +"kpiSelectionTest::C hasA quality: Real(5..5)"
        val a = global.resolve<Element>("kpiSelectionTest::A")!!

        val newProperty = create(FeatureImplementation(declaredName = "KPI1", expression = "quality"), a)
        create(SpecializationImplementation(newProperty, repo.realType!!), newProperty)

        assertEquals("5..5", newProperty.variable?.valueStr)
    }
}