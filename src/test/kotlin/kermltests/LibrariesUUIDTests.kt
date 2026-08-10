package kermltests

import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies.uuid5
import com.github.tukcps.sysmd.model.datamodel.variant
import com.github.tukcps.sysmd.model.datamodel.version
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.check.checkLibraryElementIds
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import util.assertNoIssues
import util.loadLibraryArrangement
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*
import kotlin.uuid.Uuid


class LibrariesUuidTests {

    @Test
    fun testUuid5Generation() {

        val dns = Uuid.parse("6ba7b810-9dad-11d1-80b4-00c04fd430c8")

        val id1 = uuid5("https://omg.org", dns)
        val id2 = uuid5("https://omg.org", dns)

        // Deterministic?
        assertEquals(id1, id2)

        // Reference value (RFC-conform)
        assertEquals(Uuid.parse("5fd3ffa0-fabe-5183-af5e-9a31f565d8ad"), id1)

        // Uuid-Version
        assertEquals(5, id1.version)

        // RFC-4122/RFC-9562 Variant
        assertEquals(2, id1.variant)
    }

    /** Check that Uuid5 are generated for the fully qualified names in standard library packages */
    @Test
    fun uuid5isGeneratedTest1() = testSession {
        loadKerML("""
            standard library package x {     
                class c; 
                datatype d; 
                package p { class e; }
                feature f [2 .. 3]; 
            }
        """)
        assertNoIssues()
        val x = global.resolve("x")?.member<Package>()
        assertNotNull(x)
        assertTrue(x.isLibraryElement)
        val xp = global.resolve("x::p")?.memberElement
        assertNotNull(xp)
        assertTrue(xp.isLibraryElement)
        assertEquals(5, xp.elementId.version)

//?        assertEquals(uuid5("https://www.omg.org/spec/KerML/x::p", DNS_NAMESPACE), xp.elementId)

        // Specialization of class is generated as Uuid v5
        val cSpecialization = global.resolve("x::c")?.memberElement!!.getOwnedElementOfType<Specialization>()
//        val cSpecializationPath = cSpecialization?.path()
//        assertEquals("x::c/1", cSpecializationPath, "Path should use index if no name is available, starts with 1")
        assertTrue(cSpecialization != null)
        assertTrue(cSpecialization.isLibraryElement)
        assertEquals(5, cSpecialization.elementId.version)
//        var uuid5 = uuid5("https://www.omg.org/spec/KerML/x::c/1", DNS_NAMESPACE)
//        assertEquals(uuid5, cSpecialization.elementId)

        // Datatype's id
        val d = global.resolve("x::d")?.memberElement
        assertEquals(5, d?.elementId?.version)
        val dSpecialization = global.resolve("x::d")?.memberElement?.getOwnedElementOfType<Specialization>()
        assertTrue(dSpecialization != null)
        assertTrue(dSpecialization.isLibraryElement)
        assertEquals(5, dSpecialization.elementId.version)
//        uuid5 = Generators.nameBasedGenerator().generate(dSpecialization.path())
//        assertEquals(uuid5, dSpecialization.elementId)

        // Feature's id
        val f = global.resolve("x::f")?.memberElement
        assertEquals(5, f?.elementId?.version)
        val fSpecialization = global.resolve("x::f")?.memberElement!!.getOwnedElementOfType<Specialization>()
        assertTrue(fSpecialization != null)
        assertTrue(fSpecialization.isLibraryElement)
        assertEquals(5, fSpecialization.elementId.version)
//        uuid5 = Generators.nameBasedGenerator().generate(fSpecialization.path())
//        assertEquals(uuid5, fSpecialization.elementId)
        val fMultiplicity = global.resolve("x::f")?.memberElement?.getOwnedElementOfType<Multiplicity>()
        assertTrue(fMultiplicity != null)
        assertTrue(fMultiplicity.isLibraryElement)
        assertEquals(5, fMultiplicity.elementId.version)
        // val mPath = fMultiplicity.path()
//        uuid5 = Generators.nameBasedGenerator().generate("x::f::cardinality")
//        assertEquals(uuid5, fMultiplicity.elementId)

        get().forEach { element ->
            if(element.isLibraryElement && (element.declaredName != null || element.declaredShortName != null) && element !is Multiplicity) {
//                val uuid52 = Generators.nameBasedGenerator().generate(element.qualifiedName)
//                assertEquals(uuid52, element.elementId, "No correct Uuid5 generated for ${element.qualifiedName}")
            }
        }
    }


    /** Check that Uuid5 are generated for the fully qualified names in standard library packages */
    @Test
    fun uuid5isGeneratedTest2() = testSession {
        loadKerML("""
            package ScalarValues { datatype Natural :> ScalarValue; datatype ScalarValue :> Base::Anything; }
            standard library package x {
                feature f1;
                feature f2 subsets f1;
            }; 
        """)
        initialize(Runlevel.MODEL)
        assertNoIssues()
        val x = global.resolve("x")?.memberElement
        assertNotNull(x)
        assertTrue(x.isLibraryElement)
        fun pkgID(name : String) = uuid5("https://www.omg.org/spec/KerML/$name", UuidPolicies.DNS_NAMESPACE)

        assertEquals(5, x.elementId.version)
        assertEquals(pkgID("x"), x.elementId)

        get().forEach { element ->
            if(element.isLibraryElement && (element.declaredName != null || element.declaredName != null) && element !is Multiplicity) {
                var pkg = element

                while(true)
                {
                    val o = pkg.owningNamespace

                    if(o === null || o === global)
                        break

                    pkg = o
                }

                // fixme: should actually be LibraryPackage
                assertIs<Package>(pkg, "Standard element not inside library package?").also {
                    assertTrue(pkg.isLibraryElement)
                    assertTrue(pkg.isStandard, "Standard element not inside standard package?")
                    assertNotNull(pkg.name) { "Unnamed standard library?" }
                }

                val pid = pkgID(pkg.name!!)

                if(pkg === element)
                    assertEquals(pid, element.elementId, "Wrong or no Uuid5: $element")
                else
                {
                    assertEquals(
                        uuid5(element.path(), pkgID(pkg.name!!)),
                        element.elementId,
                        "Wrong or no Uuid5: $element"
                    )
                }


            }
        }
    }

    @Test
    fun loadScalarValuesTest2() = testSession("ScalarValues") {
        checkOwnership()
        checkLibraryElementIds()
        assertNoIssues()
    }


    @Test
    fun loadLinksTest2() = testSession("Links") {
        val links = global.resolve("Links::Link")?.memberElement
        assertNotNull(links)
        checkOwnership()
        checkLibraryElementIds()
        assertNoIssues()
    }

    @Test
    fun loadOccurrencesTest() = testSession("Occurrences") {
        val occurrence = global.resolve("Occurrences::Occurrence")?.memberElement
        assertNotNull(occurrence)
        checkOwnership()
        checkLibraryElementIds()
        assertNoIssues()
    }

    @Test
    fun loadOccurrencesTest2() = testSession("Occurrences", "ISQ") {
        val occurrence = global.resolve("Occurrences::Occurrence")?.memberElement
        assertNotNull(occurrence)
        assertNotNull(global.resolve("ISQ")).memberElement
        assertNoIssues()
    }

    @Test
    fun loadLinksTest3() = testSession("ScalarValues") {
        loadKerML("""
         package Links {
            assoc BinaryLink :> Base::Anything {
                end feature source: Base::Anything [1];
                end feature target: Base::Anything [1];
            }
            connector binaryLinks : BinaryLink from Base::things to Base::things;
        }   
        """)
        checkOwnership()
        checkLibraryElementIds()
        val bl = global.resolve("Links::BinaryLink")?.memberElement
        assertNotNull(bl)
        assertNoIssues()
    }


    /**
     * Loading from a repository via cloning all elements from a repository; eventually,
     * there might be double-copies that shall be avoided, e.g., Any, Global, Package, ...
     */
    @Test
    fun basicSessionTest() = testSession("KerML") {
        assertNoIssues()
        assertTrue(5 <= global.getOwnedElementsOfType<Element>().size)
        checkLibraryElementIds()
        checkOwnership()
        assertNoIssues()
    }


    /**
     * Loading from the repository via cloning all elements from a repository; eventually,
     * there might be double-copies that shall be avoided, e.g., Any, Global, Package, ...
     */
    @Test
    fun basicSessionWithReload() = testSession("KerMLLibraries") {
        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
        assertTrue(5 <= global.getOwnedElementsOfType<Element>().size)
        checkLibraryElementIds()
        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
        checkOwnership()
        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
        loadLibraryArrangement("ScalarValues")
        checkOwnership()
        checkLibraryElementIds()
        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
    }
}