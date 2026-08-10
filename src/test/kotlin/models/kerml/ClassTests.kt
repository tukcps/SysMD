package models.kerml

import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class ClassTests {

    @Test
    fun getSubclassesTest() = testSession {
        val a = addOwnedMember(ClassImplementation(this, declaredName = "a"), global)
        addOwnedRelationship(SpecializationImplementation(this, specific = a, general = repo.anything!!))
        val b = addOwnedMember(ClassImplementation(this, declaredName = "b"), global)
        addOwnedRelationship(SpecializationImplementation(this, specific = b, general = a))
        val c = addOwnedMember(ClassImplementation(this, declaredName = "c"), global)
        addOwnedRelationship(SpecializationImplementation(this, specific = c, general = b))
        initialize(Runlevel.MODEL)
        val aSubtypes = a.subtypes
        assertTrue(b in aSubtypes)
    }
}