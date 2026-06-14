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
        val a = addOwnedMember(ClassImplementation(declaredName = "a"), global)
        addOwnedRelationship(SpecializationImplementation(a, anything))
        val b = addOwnedMember(ClassImplementation(declaredName = "b"), global)
        addOwnedRelationship(SpecializationImplementation(b, a))
        val c = addOwnedMember(ClassImplementation(declaredName = "c"), global)
        addOwnedRelationship(SpecializationImplementation(c, b))
        initialize(Runlevel.MODEL)
        val aSubtypes = a.subtypes
        assertTrue(b in aSubtypes)
    }
}