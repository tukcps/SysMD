package compiler.kerml.root

import com.github.tukcps.sysmd.compiler.KerML
import util.assertNoIssues
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ElementTests {

    /** Short name is given in <> */
    @Test
    fun namespaceParseTest() {
        val compiler = KerML()
        val elements = compiler.parse("namespace < abc >;")
        val nr = if (compiler.settings.includeOwningRelationshipsToRoot) 2 else 1
        assertEquals(nr, elements.size) // namespace + membership
        val namespace = elements.single { it.type.name == "Namespace" }
        if (compiler.settings.includeOwningRelationshipsToRoot)
            elements.single { it.type.name == "OwningMembership" }
        assertEquals("abc", namespace.declaredShortName)
    }

    /** Short name is given in <> */
    @Test
    fun namespaceParseTest2() = testSession {
        val compiler = KerML(status = this.status)
        val elements = compiler.parse("namespace < shortName > longName;")
        val nr = if (compiler.settings.includeOwningRelationshipsToRoot) 2 else 1
        assertEquals(nr, elements.size)      // Only the namespace abc. Ready for import into session.
        val namespace = elements.single { it.type.name == "Namespace" }
        if (settings.includeOwningRelationshipsToRoot)
            elements.single { it.type.name == "OwningMembership" }
        assertEquals("Namespace", namespace.type.name)
        assertEquals("shortName", namespace.declaredShortName)
        assertNoIssues()
    }

    @Test
    fun namspace2MemberTest() = testSession {
        val elements = KerML(status = this.status).parse("namespace a; namespace b;")
        assertNoIssues()
        assertNotNull(elements.singleOrNull { it.declaredName == "a" })
        assertNotNull(elements.singleOrNull { it.declaredName == "b" })

        // Check that import is correctly importable ..
        import(elements)
        val a = global.resolve("a")?.memberElement
        val b = global.resolve("b")?.memberElement
        assertNotNull(a)
        assertNotNull(b)
    }

    @Test
    fun namspaceNestedTest() = testSession {
        val elements = KerML(status = this.status).parse("namespace a { namespace b; }")
        assertNoIssues()
        assertNotNull(elements.singleOrNull { it.declaredName == "a" })
        assertNotNull(elements.singleOrNull { it.declaredName == "b" })

        // Check that import is correctly importable ..
        import(elements)
        val a = global.resolve("a")?.memberElement
        val b = global.resolve("a::b")?.memberElement
        assertNotNull(a)
        assertNotNull(b)
    }
}