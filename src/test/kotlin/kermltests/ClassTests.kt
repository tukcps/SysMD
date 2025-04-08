package kermltests

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class ClassTests {

    /**
     * Default specializes Occurrence.
     */
    @Test
    fun classBasic() = testSession {
        loadKerML("""
            namespace Occurrences { type Occurrence :> Base::Anything; }
            class a; 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val a = global.resolve<Class>("a")
        val occ = global.resolve<Type>("Occurrences::Occurrence")
        assertNotNull(a)
        assertNotNull(occ)
        val specialization = a.getOwnedElementOfType<Specialization>()
        assertNotNull(specialization)
        assertEquals(occ, specialization.general.ref)
        assertEquals(global.resolve<Type>("Occurrences::Occurrence"), a.allSupertypes().first())
    }

    /**
     * Specialization other than Base::Anything
     */
    @Test
    fun classTestViaSysMD() = testSession {
        loadKerML("""
            namespace Occurrences { type Occurrence :> Base::Anything; }
            class a; 
            class b :> a.
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val b = global.resolve<Class>("b")
        assertEquals("a", b?.allSupertypes()?.first()?.declaredName)
    }

    /**
     * A class' specialization is an occurrence. Else, an error is reported.
     */
    @Test fun isATestParser() = testSession("Occurrences") {
        loadKerML("""
            class a :> Base::Anything   ;
        """)
        val a = global.resolve<Class>("a")
        assertNotNull(a)
        assertTrue(anything in a.allSupertypes(true))
        a.checkConstraints()
        assertFalse(status.issues.isEmpty(), status.issues.toString())
    }


    @Test fun tessSupertypeCreation() = testSession ("Occurrences") {
        loadKerML("""
            class x :> Base::Anything; 
            class y :> x; 
        """)
        val y = global.resolve<Class>("y")
        assertEquals("x", y?.allSupertypes()?.first()?.qualifiedName)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test fun testSuperclassCannotBeItself() = testSession("Occurrences") {
        loadKerML("class A :> A;")
        val a = global.resolve<Type>("A")
        assertNotNull(a)
        assertTrue(status.issues.isNotEmpty(), status.issues.toString())
    }

    @Test fun testSuperclassCannotBeItselfUpdate() = testSession("Occurrences") {
        loadKerML("""
            class B :> Base::Anything; 
            class A :> A; 
        """)
        initialize()
        assertTrue(status.issues.isNotEmpty(), status.issues.toString())
    }

    /**
     * Type definitions must not be cyclic if their multiplicity is larger than 0.
     */
    @Test fun testSuperclassCannotBeItselfCyclic() = testSession {
        loadKerML("""
            type A :> B [1];
            type B :> A [1];
        """.trimIndent())
        assertTrue(status.issues.isNotEmpty(), status.issues.toString())
    }

    /**
     * A Feature shall not be considered as a Class for Classification.
     */
    @Test fun testSuperclassMustBeClassifiable() = testSession("Base") {
        loadKerML("""
            type c :> Base::things; // Superclass must be Classifier, but not a Feature. 
        """)
        val a = global.resolve<Type>("c")!!
        assertNotNull(a.allSupertypes().firstOrNull())
        assertTrue(status.issues.isNotEmpty(), status.issues.toString())
    }

    @Test fun testOrderOfIsAIsIrrelevant() = testSession("Occurrences") {
        loadKerML("""
            class A :> B; 
            class B; 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val a = global.resolve<TypeImplementation>("A")!!
        val b = global.resolve<TypeImplementation>("B")!!
        assertTrue(b in a.allSupertypes())
        assertTrue(anything in b.allSupertypes(transitive = true))
    }


    @Test fun testTwoSuperclasses() = testSession("Occurrences") {
        loadKerML("""
            class A { feature a; }
            class B { feature b; }
            class AB :> A, B; 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val ab = global.resolve<Class>("AB")
        assertNotNull(ab)
        assertEquals(2, ab.allSupertypes().size)
        val ABa = global.resolve<Feature>("AB::a")
        val ABb = global.resolve<Feature>("AB::b")
        assertNotNull(ABa)
        assertNotNull(ABb)
    }
}