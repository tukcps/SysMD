package models.kerml

import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.services.initialize
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals


class TypeTests {

    @Test fun specializesTest() = testSession {
        val a = addOwnedMember(TypeImplementation(declaredName="a"), global)
        addOwnedRelationship(SpecializationImplementation(a, anything), a)
        val b = addOwnedMember(TypeImplementation(declaredName="b"), global)
        addOwnedRelationship(SpecializationImplementation(b, a), b)
        val c = addOwnedMember(TypeImplementation(declaredName="c"), global)
        addOwnedRelationship(SpecializationImplementation(c, b), c)
        initialize()
        assertEquals(true, c.specializes(b))
        assertEquals(false, a.specializes(b))
    }

    @Test fun isSubtypeTest() = testSession {
        val a = addOwnedMember(TypeImplementation( declaredName="a"), global)
        addOwnedRelationship(SpecializationImplementation(a, anything), a)
        val b = addOwnedMember(TypeImplementation(declaredName="b"), global)
        addOwnedRelationship(SpecializationImplementation(b, a), b)
        val c = addOwnedMember(TypeImplementation(declaredName="c"), global)
        addOwnedRelationship(SpecializationImplementation(c, b), c)
        initialize()
        assertEquals(false, b.specializes(c))
        assertEquals(true, b.specializes(a))
    }

    @Test fun getSubclassesTest() = testSession {
        val a = addOwnedMember(TypeImplementation(declaredName="a"), global)
        addOwnedRelationship(SpecializationImplementation(a, anything), a)
        val b = addOwnedMember(TypeImplementation(declaredName="b"), global)
        addOwnedRelationship(SpecializationImplementation(b, a), b)
        val c = addOwnedMember(TypeImplementation(declaredName="c"), global)
        addOwnedRelationship(SpecializationImplementation(c, b), c)
        initialize()
        val aSubtype = a.subtypes
        assertEquals("b", aSubtype.first().declaredName)
    }
}