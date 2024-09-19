package kermltests

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class ClassTests {

    /**
     * hasA features are inherited.
     */
    @Test fun classTestViaSysMD() = testSession(loadKerML = false) {
        settings.catchExceptions = false
        loadSysMD("""
                class a :> Base::Anything;
                class b :> a.
            """.trimIndent())
        initialize() // Shall set isA.ref.name to 'a'.
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val b = global.resolve<TypeImplementation>("b")
        assertEquals("a", b?.allSupertypes()?.first()?.declaredName)
    }

    /**
     * isA specifies superclass; via parser.
     */
    @Test fun isATestParser() = testSession(loadKerML = false) {
        loadSysMD("class a :> Base::Anything;")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val a = global.resolve<Element>("a") as TypeImplementation
        assertNotNull(a)
        assertTrue(any in a.allSupertypes())
    }


    @Test
    fun debug() = testSession(loadKerML = false) {
        +"""package x {
                class a :> Base::Anything; 
            }
            package y {
                package a; 
            }
         """.trimIndent()
        val a = global.resolve<Classifier>("x::a")
        assertNotNull(a)
        val b = global.resolve<Package>("y::a")
        assertNotNull(b)
    }

    @Test fun tessSupertypeCreation() = testSession (loadKerML = false) {
        loadSysMD(input = """
        class x :> Base::Anything; 
        class y :> x; 
        """.trimIndent())
        val y = global.resolve<Class>("y")
        assertEquals("x", y?.allSupertypes()?.first()?.qualifiedName)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test fun testSuperclassCannotBeItself() = testSession(loadKerML = false) {
        loadSysMD("class A :> A.")
        val a = global.resolve<Type>("A")
        assertNotNull(a)
        assertTrue(status.exceptions.isNotEmpty(), status.exceptions.toString())
    }

    @Test fun testSuperclassCannotBeItselfUpdate() = testSession(loadKerML = false) {
        loadSysMD("""
            class B :> Base::Anything; 
            class A :> A; 
        """.trimIndent())
        initialize()
        assertTrue(status.exceptions.isNotEmpty(), status.exceptions.toString())
    }

    @Test fun testSuperclassCannotBeItselfCyclic() = testSession(loadKerML = false) {
        loadSysMD("""
            class A :> B;
            class B :> A;
        """.trimIndent())
        assertTrue(status.exceptions.isNotEmpty(), status.exceptions.toString())
    }

    /**
     * A Feature shall not be considered as a Class for Classification.
     */
    @Test fun testSuperclassMustBeClassifiable() = testSession {
        loadSysMD("""
            class A :> Base::thing; // Superclass must be Classifier, but not a Feature. 
        """.trimIndent()
        )
        val a = global.resolve<Type>("A")!!
        assertNotNull(a.allSupertypes().first())
        assertTrue(status.exceptions.isNotEmpty(), status.exceptions.toString())
    }

    @Test fun testOrderOfIsAIsIrrelevant() = testSession(loadKerML = false) {
        loadSysMD("""
            class A :> B; 
            class B :> Base::Anything; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val a = global.resolve<TypeImplementation>("A")!!
        val b = global.resolve<TypeImplementation>("B")!!
        assertTrue(b in a.allSupertypes())
        assertTrue(any in b.allSupertypes())
    }


    @Test
    fun testTwoSuperclasses() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; }
            class A { feature a; }
            class B { feature b; }
            class AB :> A, B; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val ab = global.resolve<Class>("AB")
        assertNotNull(ab)
        assertEquals(2, ab.allSupertypes().size)
        val ABa = global.resolve<Feature>("AB::a")
        val ABb = global.resolve<Feature>("AB::b")
        assertNotNull(ABa)
        assertNotNull(ABb)
    }
}
