package models.kerml

import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class TypeTests {

    @Test fun specializesTest() = testSession {
        val a = addOwnedMember(TypeImplementation(declaredName="a"), global)
        addOwnedRelationship(SpecializationImplementation(a, anything), a)
        val b = addOwnedMember(TypeImplementation(declaredName="b"), global)
        addOwnedRelationship(SpecializationImplementation(b, a), b)
        val c = addOwnedMember(TypeImplementation(declaredName="c"), global)
        addOwnedRelationship(SpecializationImplementation(c, b), c)
        initialize(Runlevel.MODEL)
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
        initialize(Runlevel.MODEL)
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
        initialize(Runlevel.MODEL)
        val aSubtype = a.subtypes
        assertEquals("b", aSubtype.first().declaredName)
    }

    /**
     * features are visible members, iff not private
     */
    @Test
    fun getVisibleMembershipTest() = testSession {
        loadKerML(""" 
            type t :> Base::Anything {
                feature f1; 
                private feature f2; // No - private 
                type t2 :> Base::Anything; // No - type 
            }
        """)
        assertNoIssues()
        val t: Type? = global.resolve("t")?.member()
        assertNotNull(t)
        val visibleMembers = t.visibleMemberships()
        assertEquals(2, visibleMembers.size)
    }
}