package entities

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.*

class EntitiesTests {

    @Test
    fun anythingTest() = testSession {
        val base = global.getOwned<Package>("Base")!!
        val any = base.getOwned<Anything>("Anything")!!
        assertSame(any.owner.ref, base)
        assertTrue(any.ownedElement.isEmpty())
        assertTrue(any.specialization.isEmpty())
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
        create(SpecializationImplementation(claCreated, any), claCreated)
        val specialization = claCreated.getOwnedElementOfType<Specialization>() !!
        initialize()
        assertEquals(claCreated.allSupertypes().first(), specialization.general.ref)
        assertEquals(specialization.specific.ref, claCreated)
        assertEquals(specialization.specific.id, claCreated.elementId)
        assertEquals(specialization.general.ref, any)
        assertEquals(specialization.general.id, any.elementId)
    }
}