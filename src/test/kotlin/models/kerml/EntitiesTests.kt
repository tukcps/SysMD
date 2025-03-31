package models.kerml

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.services.initialize
import util.testSession
import kotlin.test.*

class EntitiesTests {

    @Test
    fun anythingTest() = testSession {
        val base = global.getOwned<Package>("Base")!!
        val anything = base.getOwned<Anything>("Anything")!!
        assertSame(anything.owner.ref, base)
        assertTrue(anything.ownedElement.isEmpty())
        assertTrue(anything.specialization.isEmpty())
    }

    @Test
    fun globalTest() = testSession {
        assertNotNull(global)
        assertNotNull(global.elementId)
        assertNull(global.owner.ref)
        assertEquals("Global", global.qualifiedName)
    }

    /**
     * Classification: The superclass is represented by an owned Specialization
     * relationship from itself to the superclass.
     */
    @Test
    fun classificationSpecializationTest() = testSession {
        val cla = TypeImplementation(declaredName = "x")
        val claCreated = create(cla, global)
        create(SpecializationImplementation(claCreated, anything), claCreated)
        val specialization = claCreated.getOwnedElementOfType<Specialization>() !!
        initialize()
        assertEquals(claCreated.allSupertypes().first(), specialization.general.ref)
        assertEquals(specialization.specific.ref, claCreated)
        assertEquals(specialization.specific.id, claCreated.elementId)
        assertEquals(specialization.general.ref, anything)
        assertEquals(specialization.general.id, anything.elementId)
    }
}