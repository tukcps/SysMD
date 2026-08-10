package compiler.kerml.kernel

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import util.assertIssue
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class ClassTests {

    /**
     * Default behavior of Occurrence.
     * - is implicit Occurrences::Occurrence.
     * - owned relationship is a Subclassification.
     */
    @Test
    fun classBasic() = testSession("ScalarValues") {
        loadKerML("""
            namespace Occurrences { type Occurrence :> Base::Anything; }
            class a; 
        """, Runlevel.MODEL)
        assertNoIssues()
        val a = global.resolve("a")?.member<Class>()
        val occ = global.resolve("Occurrences::Occurrence")?.member<Type>()
        assertNotNull(a)
        assertNotNull(occ)
        val subclassifier = a.getOwnedElementOfType<Subclassification>()
        assertNotNull(subclassifier)
        assertEquals(occ, subclassifier.general)
        assertEquals(global.resolve("Occurrences::Occurrence")?.memberElement, a.allSupertypes().first())
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
        assertNoIssues()
        val b = global.resolve("b")?.member<Class>()
        assertEquals("a", b?.allSupertypes()?.first()?.declaredName)
    }

    /**
     * A class' specialization is an occurrence. Else, an error is reported.
     */
    @Test
    fun classTypedByOccurrence() = testSession {
        loadKerML("""
            namespace Occurrences { type Occurrence :> Base::Anything; }
            class a :> Base::Anything;
        """)
        val a = global.resolve("a")?.member<Class>()
        assertNotNull(a)
        assertTrue(repo.anything!! in a.allSupertypes(true))
        a.checkConstraints()
        assertIssue("Occurrence")
    }


    @Test
    fun tessSupertypeCreation() = testSession {
        loadKerML("""
            namespace Occurrences { type Occurrence :> Base::Anything; }
            class x :> Base::Anything; 
            class y :> x; 
        """)
        val y = global.resolve("y")?.member<Class>()
        val x = global.resolve("x")?.member<Class>()!!
        assertEquals(y?.allSupertypes()?.contains(x), true)
        assertNoIssues()
    }

    @Test
    fun testSuperclassCannotBeItself() = testSession {
        loadKerML("namespace Occurrences { type Occurrence :> Base::Anything; }")
        loadKerML("class A :> A;")
        val a = global.resolve("A")?.member<Class>()
        assertNotNull(a)
        assertIssue("cannot be itself")
    }

    @Test
    fun testSuperclassCannotBeItselfUpdate() = testSession {
        loadKerML("""
            package Occurrences { classifier Occurrence; }
            class B :> Base::Anything; 
            class A :> A; 
        """)
        initialize(Runlevel.NAMES_RESOLVED)
        assertIssue("cannot be itself")
    }

    /**
     * A Feature shall not be considered as a Class for Classification.
     */
    @Ignore  // Unclear how the intention of KerML is.
    @Test
    fun testSuperclassMustBeClassifiable() = testSession("Base") {
        loadKerML("""
            type c :> Base::things; // Superclass must be Classifier, but not a Feature. 
        """)
        val a = global.resolve("c")!!.member<Class>()
        assertNotNull(a?.allSupertypes()?.firstOrNull())
        assertTrue(status.issues.isNotEmpty(), status.issues.toString())
    }

    @Test
    fun testOrderIsIrrelevant() = testSession {
        loadKerML("""
            namespace Occurrences { classifier Occurrence; }
            class A :> B; 
            class B; 
        """)
        assertNoIssues()
        val a = global.resolve("A")!!.member<Class>()
        val b = global.resolve("B")!!.member<Class>()!!
        assertTrue(b in a!!.allSupertypes())
        assertTrue(repo.anything!! in b.allSupertypes(transitive = true))
    }


    @Test
    fun testTwoSuperclasses() = testSession {
        loadKerML("""
            namespace Occurrences { classifier Occurrence; }
            class A { feature a; }
            class B { feature b; }
            class AB :> A, B; 
        """)
        assertNoIssues()
        val ab = global.resolve("AB")?.member<Class>()
        assertNotNull(ab)
        assertEquals(2, ab.allSupertypes().size)
        val aba = global.resolve("AB::a")?.member<Feature>()
        val abb = global.resolve("AB::b")?.member<Feature>()
        assertNotNull(aba)
        assertNotNull(abb)
    }
}