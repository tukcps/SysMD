package compiler.kerml.kernel

import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies.libraryUuid5
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.Relationship
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class PackageTests {

    @Test
    fun basicPackage() = testSession {
        loadKerML("""
            package p; 
        """)
        assertNoIssues()
        val p = global.resolve("p")?.member<Package>()
        assertNotNull(p)
    }

    @Test
    fun libraryPackage() = testSession {
        loadKerML("standard library package p;")
        assertNoIssues()
        val p = global.resolve("p")?.member<Package>()
        assertNotNull(p)
        assertTrue(p.isStandard)
        assertEquals(global, p.owner)
        assertEquals("p", p.path())
        assertEquals(libraryUuid5("p"), p.elementId)
    }

    @Test
    fun ownershipExample() = testSession {
        settings.includeOwningRelationshipsToRoot = true
        loadKerML("package foo { package bar; }")
        assertNoIssues()
        // println(toIndentedString(global))
        val foo = get().single { it.name == "foo" }.also {
            assertSame(global, it.owner)
            assertSame(global, it.owningNamespace)
            assertTrue(it in global.ownedElement)
        }
        assertIs<Package>(foo)

        assertNotNull(foo.owningRelationship).also {
            assertSame(global, it.owner)
            assertSame(global, it.owningNamespace)
            assertSame(global, it.owningRelatedElement)
            assertSame(global, it.membershipOwningNamespace)
            assertNull(it.owningRelationship)

            assertSame(foo, it.ownedMemberElement)
            assertEquals(listOf(foo as Element), it.ownedRelatedElement)
            assertEquals(listOf(foo as Element), it.ownedElement)
            assertEquals(emptyList(), it.ownedRelationship)
            assertSame(foo, it.member())

            assertEquals(listOf(foo as Element), it.target)
            assertEquals(listOf(global as Element), it.source)

            assertTrue(it in global.ownedRelationship)
        }

        val bar = get().single { it.name == "bar" }.also {
            assertSame(foo, it.owner)
            assertSame(foo, it.owningNamespace)

            assertEquals(emptyList(), it.ownedElement)
            assertEquals(emptyList(), it.ownedRelationship)

            assertEquals(listOf(it), foo.ownedElement)
        }

        assertNotNull(bar.owningRelationship).also {
            assertSame(foo, it.owner)
            assertSame(foo, it.owningNamespace)
            assertSame(foo, it.owningRelatedElement)
            assertSame(foo, it.membershipOwningNamespace)
            assertNull(it.owningRelationship)

            assertSame(bar, it.ownedMemberElement)
            assertEquals(listOf(bar), it.ownedElement) // TODO fails
            assertEquals(listOf(bar), it.ownedRelatedElement) // TODO fails
            assertEquals(emptyList(), it.ownedRelationship)
            assertSame(bar, it.member())

            assertEquals(listOf(bar), it.target)
            assertEquals(listOf(foo as Element), it.source)
            assertEquals(listOf(it as Relationship), foo.ownedRelationship)
        }
    }
}