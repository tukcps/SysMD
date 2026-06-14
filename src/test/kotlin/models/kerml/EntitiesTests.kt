package models.kerml

import com.github.tukcps.sysmd.model.kerml.Anything
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.Specialization
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import util.testSession
import kotlin.test.*

class EntitiesTests {

    @Test
    fun anythingTest() = testSession {
        val base = global.getOwned<Package>("Base")!!
        val anything = base.getOwned<Anything>("Anything")!!
        assertSame(anything.owner, base)
        assertTrue(anything.ownedElement.isEmpty())
        assertTrue(anything.specialization.isEmpty())
    }

    @Test
    fun globalTest() = testSession {
        assertNotNull(global)
        assertNotNull(global.elementId)
        assertNull(global.owner)
        assertEquals(null, global.qualifiedName)
    }

    /**
     * Classification: The superclass is represented by an owned Specialization
     * relationship from itself to the superclass.
     */
    @Test
    fun classificationSpecializationTest() = testSession {
        val cla = TypeImplementation(declaredName = "x")
        val claCreated = addOwnedMember(cla, global)
        addOwnedRelationship(SpecializationImplementation(claCreated, anything))
        val specialization = claCreated.getOwnedElementOfType<Specialization>() !!
        initialize(Runlevel.MODEL)
        assertEquals(claCreated.allSupertypes().first(), specialization.general)
        assertEquals(specialization.specific, claCreated)
        assertEquals(specialization.specific.elementId, claCreated.elementId)
        assertEquals(specialization.general, anything)
        assertEquals(specialization.general.elementId, anything.elementId)
    }
}