package compiler

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.util.DataModelWithEdges
import com.github.tukcps.sysmd.compiler.parser.util.DataModelWithEdges.ActualElement
import com.github.tukcps.sysmd.compiler.parser.util.DataModelWithEdges.Path
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies.PathBasedUuids
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies.SYSMD_NOTEBOOK_ROOT_NAMESPACE
import com.github.tukcps.sysmd.model.datamodel.version
import com.github.tukcps.sysmd.model.generated.ElementType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertAll
import util.assertNoIssues
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UuidTests
{
    /** Checks that UUIDs don't change if owning relationships to root are excluded */
    @Test
    fun correctDespiteNoMembershipsFromRoot()
    {
        fun parse(rels : Boolean) = KerML(
            uuidPolicy = UuidPolicies.NewSysMD
        ).settings {
            includeOwningRelationshipsToRoot = rels
        }.apply {
            parse("""
                class Wheel { feature price : Ranges::RealInRange { :>> range = 1.0..1.0;} }
                class Body { feature price : Ranges::RealInRange { :>> range = 1.0..1.0;} }
                class Chassis { feature price : Ranges::RealInRange { :>> range = 1.0..1.0;} }
                class Car {
                    feature wheel: Wheel;
                    feature body: Body;
                    feature chassis: Chassis;
                    feature carPrice: Ranges::RealInRange = sumOverParts(price);
                }
            """.trimIndent())
            assertNoIssues()
        }.semantics.elementsBuilt.let {
            DataModelWithEdges(it)
        }

        val withRels = parse(true)
        val withoutRels = parse(false)

        fun syncBfs(path : String, a : List<Path>, b : List<Path>) {
            assertEquals(a.size, b.size, "$a != $b")

            for((ix,xy) in (a zip b).withIndex())
            {
                val (x,y) = xy
                assertEquals(x.isTopLevel, y.isTopLevel, "Top-level-ness of $path ix $ix don't agree")
                assertEquals(x.qualifiedName, y.qualifiedName, "Qualified names of $path ix $ix don't match")
                assertEquals(x.path, y.path, "Paths of $path ix $ix don't match")
                assertEquals(x.id, y.id, "UUIDs at ${x.path} don't match")

                if(x.id !== null)
                    assertEquals(5, x.id.version, "UUID of ${x.path} is not v5")

                if(x.head is ActualElement)
                {
                    assertIs<ActualElement>(y.head)
                    val xe = x.head.element
                    val ye = y.head.element
                    val msg = "Head elements at ${x.path} don't match"

                    assertEquals(xe.type, ye.type, msg)
                    assertEquals(xe.declaredShortName, ye.declaredShortName, msg)
                    assertEquals(xe.declaredName, ye.declaredName, msg)

                    // doesn't work because Identified is not data class
                    //assertEquals(x.head.element, y.head.element, "Head elements at ${x.path} don't match")
                }

                syncBfs(x.path, x.next, y.next)
            }
        }

        syncBfs(
            path = "$",
            a = withRels.rootPaths.single().next.map { it.next.singleOrNull() ?: throw IllegalStateException("$it -> ${it.next}") },
            b = withoutRels.rootPaths.single().next
        )
    }

    @Test
    fun relativeNamespace()
    {
        val model = KerML(
            uuidPolicy = UuidPolicies.NewSysMD
        ).settings {
            includeOwningRelationshipsToRoot = false // doesn't matter since "Foo::Bar" isn't root
            addDefaultMultiplicity = false
            addImplied = false
        }.apply {
            // fixme: packages can't be empty??
            parse("""
                standard library package p {
                    standard library package q { feature x; }
                }
                package q { feature y; }
            """.trimIndent(), "Foo::Bar")
            assertNoIssues()
        }.semantics.elementsBuilt.let {
            DataModelWithEdges(it)
        }

        //model.print(System.out)

        // 3 packages + 2 features + owning memberships
        assertEquals(5 * 2, model.elements.size)

        val expect = PathBasedUuids(SYSMD_NOTEBOOK_ROOT_NAMESPACE)

        assertAll(model.dfs().map { path -> {
            // println(path)
            assertFalse(path.isRootNamespace) { "$path should not be a root namespace" }
            assertFalse(path.isTopLevel) { "$path should not be top-level" }
            if (path.id !== null)
            {
                assertEquals(expect.mapUuid(path), path.id, "Wrong UUID for $path (${path.head.name})")
                assertEquals(5, path.id.version) // sanity check
            }

            when(path.head.name)
            {
                "p" -> assertEquals("Foo::Bar::p", path.path)
                "x" -> assertEquals("Foo::Bar::p::q::x", path.path)
                "y" -> assertEquals("Foo::Bar::q::y", path.path)
                "q" -> when {
                    path.head is ActualElement && path.head.element.type == ElementType.LibraryPackage -> "Foo::Bar::p::q"
                    else -> "Foo::Bar::q"
                }.let { assertEquals(it, path.path) }
                else -> {}
            }
        } }.toList())
    }
}