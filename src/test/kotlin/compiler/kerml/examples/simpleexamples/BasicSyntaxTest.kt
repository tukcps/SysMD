package compiler.kerml.examples.simpleexamples

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies.SYSMD_NOTEBOOK_ROOT_NAMESPACE
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies.libraryUuid5
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies.uuid5
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicy
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import com.github.tukcps.sysmd.services.session.SessionSettings
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class BasicSyntaxTest {

    /**
     * Kernel Modeling Language: https://modeldriven.com/wp-content/uploads/2024/06/KERML-Building-Modeling-Languages.pdf
     */
    @Test
    fun testBasicSyntax() =
        testSession("ScalarValues", "Objects", "Occurrences", "Links") {
            loadKerML("""
                package KerML_Base_Example {
                    classifier TorqueValue;
                    classifier Person;
                    
                    classifier Engine {
                        feature engineTorque: TorqueValue[1];
                    }
                    
                    classifier Wheel;
                    
                    classifier Car {
                        feature driver: Person[0..1];
                        feature engine: Engine[1];
                        feature wheels: Wheel[4];
                    }
                    classifier Sedan specializes Car;
                }
            """)
            assertNoIssues()
        }

    private data class CompileCtx(
        val elements : List<ElementData>,
        val issues : Set<Issue>,
        val settings: SessionSettings
    ) {
        val model : Map<Uuid, ElementData> = HashMap<Uuid, ElementData>().apply {
            for(e in elements)
            {
                put(e.elementId, e)?.also {
                    throw IllegalStateException("Duplicate use of ${e.elementId} between $e and $it")
                }
            }
        }

        fun name(x : ElementData?) : String = if(x === null) "<null>" else
            x.declaredName ?: x.declaredShortName ?: "<anonymous>"

        fun name(x : Uuid?) : String = if(x === null) "<null>" else {
            val hit = elements.filter { it.elementId == x }.map(::name)
            when {
                hit.isEmpty() -> "<missing ID>"
                hit.size == 1 -> hit[0]
                else -> throw IllegalStateException("Ambiguous ID $x could refer to any of $hit")
            }
        }
        fun name(x : Identified?) : String = if(x === null) "<null>" else x.id?.let { name(it) } ?: "<global or named reference>"

        @JvmName("assertEquals1")
        fun assertEquals(want : List<Uuid?>, got : List<Identified>?) = assertEquals(
            want, assertNotNull(got).map { it.id },
            "(wanted ${want.map(::name)} but got ${got.map(::name)})"
        )
        @JvmName("assertEquals2")
        fun assertEquals(want : List<ElementData>, got : List<Identified>?)
                = assertEquals(want.map { it.elementId }, got)
        @JvmName("assertEquals3")
        fun assertEquals(want : ElementData, got : List<Identified>?)
                = assertEquals(listOf(want), got)
        @JvmName("assertEquals4")
        fun assertEquals(want : ElementData, got : Identified?) = assertEquals(
            want.elementId, got?.id,
            "(wanted ${name(want)} but got ${name(got)})"
        )

        val empty = emptyList<Uuid>()
    }

    private inline fun parseTest(code : String, policy : UuidPolicy = UuidPolicies.LegacySysMD, body : CompileCtx.() -> Unit)
    {
        val parser = KerML(uuidPolicy = policy)
        val elements = parser.parse(code)

        CompileCtx(elements, parser.status.issues, parser.settings).body()
    }

    @Test
    fun testBasicOwnership() = parseTest("package foo { package bar; }") {
        assertEquals(emptySet(), issues)
        if (settings.includeOwningRelationshipsToRoot)
            assertEquals(4, elements.size)
        else
            assertEquals(3, elements.size)

        val foo = elements.single { it.declaredName == "foo" }.also {
            assertNull(it.owner)
            assertNull(it.owningNamespace)
            if (settings.includeOwningRelationshipsToRoot) {
                assertNotNull(it.owningRelationship?.id)
                assertEquals(it.owningRelationship!!.id, it.owningMembership?.id)
            }
            assertEquals("Package", it.type.name)

            assertEquals(empty, it.ownedElement)
        }

        if (settings.includeOwningRelationshipsToRoot)
            elements.single { it.elementId == foo.owningRelationship!!.id }.let {
                assertEquals("OwningMembership", it.type.name)

                assertNull(assertNotNull(it.owner).id)
                if(it.owningNamespace !== null)
                    // these are optional computed fields that are usually only present on exports from complete models
                    assertNull(assertNotNull(it.owningNamespace).id)
                assertNull(it.owningRelationship)
                assertNull(it.owningMembership)

                assertEquals(empty, it.ownedRelationship)
                assertEquals(foo, it.ownedElement)

                assertEquals(listOf<Uuid?>(null), it.source)
                assertEquals(foo, it.target)
            }

        val bar = elements.single { it.declaredName == "bar" }.also {
            assertEquals("Package", it.type.name)

            assertNull(it.owner)
            assertNull(it.owningNamespace)
            assertNotNull(it.owningRelationship?.id)
            assertEquals(it.owningRelationship!!.id, it.owningMembership?.id)

            assertEquals(empty, it.ownedRelationship)
            assertEquals(empty, it.ownedElement)
        }

        // membership owning bar
        elements.single { it.elementId == bar.owningRelationship!!.id }.let {
            assertEquals("OwningMembership", it.type.name)

            assertEquals(foo, it.owner)
            if(it.owningNamespace !== null)
                assertEquals(foo, it.owningNamespace)
            assertEquals(it, foo.ownedRelationship)
            assertNull(it.owningRelationship)
            assertNull(it.owningMembership)

            assertEquals(listOf(bar), it.ownedElement)
            assertEquals(empty, it.ownedRelationship)
            assertEquals(listOf(it), foo.ownedRelationship)
        }
    }

    /** Tests Uuid reassignment, and whether relationships survive it */
    @Test
    fun testStdlibUuid() = parseTest("standard library package p;", policy = UuidPolicies.NewSysMD) {
        assertEquals(emptySet(), issues)
        if (settings.includeOwningRelationshipsToRoot)
            assertEquals(2, elements.size)
        else
            assertEquals(1, elements.size)
        // println(elements)

        val p = elements.single { it.declaredName == "p" }

        if (settings.includeOwningRelationshipsToRoot) {
            val m = elements.single { it !== p }

            assertEquals(libraryUuid5("p"), p.elementId)
            assertEquals(uuid5("p/owningMembership", SYSMD_NOTEBOOK_ROOT_NAMESPACE), m.elementId)

            assertNull(p.owner)
            assertNull(p.owningNamespace)
            assertEquals(m, p.owningMembership)
            assertEquals(m, p.owningRelationship)

            assertEquals(empty, p.ownedElement)
            assertEquals(empty, p.ownedRelationship)
            assertEquals(empty, p.ownedRelatedElement)

            assertNull(assertNotNull(m.owner).id)// todo: why are these different??
            assertNull(m.owningNamespace)
            assertNull(m.owningMembership)
            assertNull(m.owningRelationship)

            assertEquals(p, m.ownedElement)
            assertEquals(p, m.ownedRelatedElement)
            assertEquals(empty, m.ownedRelationship)
        }
    }
}