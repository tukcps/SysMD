package compiler.kerml

import com.github.tukcps.sysmd.model.kerml.Conjugation
import com.github.tukcps.sysmd.model.kerml.Disjoining
import com.github.tukcps.sysmd.model.kerml.Specialization
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.services.resolve.resolve
import util.assertIssue
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TypeTests {

    @Test
    fun testType() = testSession {
        loadKerML("""
           type a :> Base::Anything;  
        """)
        assertNoIssues()
        val a = global.resolve<Type>("a")
        assertNotNull(a)
        assertTrue(a.getOwnedElementOfType<Specialization>()?.general == anything)
    }

    @Test
    fun testTwoSupertypes() = testSession {
        loadKerML("""
            type A :> Base::Anything { namespace a; }
            type B :> Base::Anything { namespace b; }
            type AB :> A, B; 
        """)
        assertNoIssues()
        val ab = global.resolve<TypeImplementation>("AB")
        assertNotNull(ab)
        assertEquals(2, ab.allSupertypes().size)
    }

    @Test
    fun testVisibility() = testSession {
        loadKerML("""
            package p {
                private type t :> Base::Anything; 
            }
            
            type A :> p::t; 
        """)
        val pt = global.resolve<Type>("p::t")
        assertNotNull(pt)
        assertNoIssues()
    }

    @Test
    fun typeWithMultiplicityTest() = testSession {
        loadKerML("""
            type A [1] :> Base::Anything;
        """)
        assertNoIssues()
    }

    /**
     * Type definitions must not be cyclic if their multiplicity is larger than 0.
     */
    @Test
    fun testSuperclassCannotBeItselfCyclic() = testSession {
        loadKerML("""
            type A [1] :> B ;
            type B [1] :> A ;
        """)
        assertIssue("Cyclic definition")
    }

    /**
     * Disjoining as in 7.3.2.5
     */
    @Test
    fun ownedDisjoiningTest() = testSession {
        loadKerML("""
            type A :> Base::Anything;
            type B :> Base::Anything;
            type C :> A disjoint from B;  
        """)
        assertNoIssues()
        val c = global.resolve<TypeImplementation>("C")
        assertNotNull(c)
        val disjoining = c.getOwnedElementOfType<Disjoining>()
        assertNotNull(disjoining)
    }

    @Test
    fun ownedConjugationTest() = testSession {
        loadKerML("""
            type B conjugates Base::Anything;
        """)
        assertNoIssues()
        val b = global.resolve<TypeImplementation>("B")
        assertNotNull(b)
        val conjugation = b.getOwnedElementOfType<Conjugation>()
        assertNotNull(conjugation)
        assertEquals(b, conjugation.conjugatedType)
        assertEquals(anything, conjugation.originalType)
    }
}