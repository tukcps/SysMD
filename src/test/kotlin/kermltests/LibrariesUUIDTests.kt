package kermltests

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.check.checkLibraryElementIds
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.loadLibrary
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class LibrariesUUIDTests {

    /** Check that UUID5 are generated for the fully qualified names in standard library packages */
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

        // assertTrue(status.reports.isEmpty(), status.reports.toString())
        val x = global.resolve("x::p")?.memberElement
        assertNotNull(x)
        assertTrue(x.isLibraryElement)
        assertEquals(5, x.elementId!!.version())
        var uuid5 = Generators.nameBasedGenerator().generate("x::p")
        assertEquals(uuid5, x.elementId)

        // Specialization of class is generated as UUID v5
        val cSpecialization = global.resolve("x::c")?.memberElement!!.getOwnedElementOfType<Specialization>()
        val cSpecializationPath = cSpecialization?.path()
        assertEquals(cSpecializationPath, "x::c/0", "Path should use index if no name is available")
        assertTrue(cSpecialization != null)
        assertTrue(cSpecialization.isLibraryElement)
        assertEquals(5, cSpecialization.elementId!!.version())
        uuid5 = Generators.nameBasedGenerator().generate(cSpecialization.path())
        assertEquals(uuid5, cSpecialization.elementId)

        // Datatype's id
        val d = global.resolve("x::d")?.memberElement
        assertEquals(5, d?.elementId?.version())
        val dSpecialization = global.resolve("x::d")?.memberElement?.getOwnedElementOfType<Specialization>()
        assertTrue(dSpecialization != null)
        assertTrue(dSpecialization.isLibraryElement)
        assertEquals(5, dSpecialization.elementId!!.version())
        uuid5 = Generators.nameBasedGenerator().generate(dSpecialization.path())
        assertEquals(uuid5, dSpecialization.elementId)

        // Feature's id
        val f = global.resolve("x::f")?.memberElement
        assertEquals(5, f?.elementId?.version())
        val fSpecialization = global.resolve("x::f")?.memberElement!!.getOwnedElementOfType<Specialization>()
        assertTrue(fSpecialization != null)
        assertTrue(fSpecialization.isLibraryElement)
        assertEquals(5, fSpecialization.elementId!!.version())
        uuid5 = Generators.nameBasedGenerator().generate(fSpecialization.path())
        assertEquals(uuid5, fSpecialization.elementId)
        val fMultiplicity = global.resolve("x::f")?.memberElement?.getOwnedElementOfType<Multiplicity>()
        assertTrue(fMultiplicity != null)
        assertTrue(fMultiplicity.isLibraryElement)
        assertEquals(5, fMultiplicity.elementId!!.version())
        // val mPath = fMultiplicity.path()
        uuid5 = Generators.nameBasedGenerator().generate("x::f::cardinality")
        assertEquals(uuid5, fMultiplicity.elementId)

        get().forEach { element ->
            if(element.isLibraryElement && (element.declaredName != null || element.declaredShortName != null) && element !is Multiplicity) {
                val uuid52 = Generators.nameBasedGenerator().generate(element.qualifiedName)
                assertEquals(uuid52, element.elementId, "No correct UUID5 generated for ${element.qualifiedName}")
            }
        }
    }


    /** Check that UUID5 are generated for the fully qualified names in standard library packages */
    @Test
    fun uuid5isGeneratedTest2() = testSession {
        loadKerML("""
                package ScalarValues { datatype Natural :> ScalarValue; datatype ScalarValue :> Base::Anything; }
                standard library package x {
                    feature f1;
                    feature f2 subsets f1;
                }; 
            """)
        initialize()
        assertNoIssues()
        val x = global.resolve("x")?.memberElement
        assertNotNull(x)
        assertTrue(x.isLibraryElement)
        assertEquals(5, x.elementId!!.version())
        val uuid5 = Generators.nameBasedGenerator().generate(x.qualifiedName)
        assertEquals(uuid5, x.elementId)

        get().forEach { element ->
            if(element.isLibraryElement && (element.declaredName != null || element.declaredName != null) && element !is Multiplicity) {
                val uuid52 = Generators.nameBasedGenerator().generate(element.qualifiedName)
                assertEquals(uuid52, element.elementId, "Wrong or no UUID5: $element")
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
        loadLibrary("ScalarValues")
        checkOwnership()
        checkLibraryElementIds()
        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
    }
}