package kermltests

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.ElementImplementation
import com.github.tukcps.sysmd.services.check.checkLibraryElementIds
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.LibraryRepository
import com.github.tukcps.sysmd.services.session.SessionImplementation
import util.mockup.loadKerML
import com.github.tukcps.sysmd.services.session.loadLibrary
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class LibrariesUUIDTests {

    @Test
    fun countOwnedTest1() {
        val session = SessionImplementation(libraries = mutableListOf())
        val e1 = ElementImplementation(declaredName = "e1")
        val e2 = ElementImplementation(declaredName = "e2")
        session.addUnownedElement(e1, path="e2")
        session.addUnownedElement(e2, startOfOwnerPath = session.global)
        val nr = session.getNumberOfOwnedElements("e2")
        assertEquals(1, nr)
    }

    @Test
    fun countOwnedTest2() {
        val session = SessionImplementation(libraries = mutableListOf())
        val e = ElementImplementation(declaredName = "e")
        val e1 = ElementImplementation(declaredName = "e1")
        val e2 = ElementImplementation(declaredName = "e2")
        session.addUnownedElement(e1, startOfOwnerPath = session.global)
        session.addUnownedElement(e1, startOfOwnerPath = e)
        session.addUnownedElement(e2, startOfOwnerPath = e)
        val nr = session.getNumberOfOwnedElements("e")
        assertEquals(2, nr)
    }


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
        val x = global.resolve<Package>("x::p")
        assertNotNull(x)
        assertTrue(x.isLibraryElement)
        assertEquals(5, x.elementId!!.version())
        var uuid5 = Generators.nameBasedGenerator().generate("x::p")
        assertEquals(uuid5, x.elementId)

        // Specialization of class is generates as UUID v5
        val cSpecialization = global.resolve<Class>("x::c")!!.getOwnedElementOfType<Specialization>()
        assertTrue(cSpecialization != null)
        assertTrue(cSpecialization.isLibraryElement)
        assertEquals(5, cSpecialization.elementId!!.version())
        uuid5 = Generators.nameBasedGenerator().generate("x::c::0")
        assertEquals(uuid5, cSpecialization.elementId)

        // Datatype's id
        val d = global.resolve<DataType>("x::d")
        assertEquals(5, d?.elementId?.version())
        val dSpecialization = global.resolve<DataType>("x::d")!!.getOwnedElementOfType<Specialization>()
        assertTrue(dSpecialization != null)
        assertTrue(dSpecialization.isLibraryElement)
        assertEquals(5, dSpecialization.elementId!!.version())
        uuid5 = Generators.nameBasedGenerator().generate("x::d::0")
        assertEquals(uuid5, dSpecialization.elementId)

        // Feature's id
        val f = global.resolve<Feature>("x::f")
        assertEquals(5, f?.elementId?.version())
        val fSpecialization = global.resolve<Feature>("x::f")!!.getOwnedElementOfType<Specialization>()
        assertTrue(fSpecialization != null)
        assertTrue(fSpecialization.isLibraryElement)
        assertEquals(5, fSpecialization.elementId!!.version())
        uuid5 = Generators.nameBasedGenerator().generate("x::f::1")
        assertEquals(uuid5, fSpecialization.elementId)
        val fMultiplicity = global.resolve<Feature>("x::f")!!.getOwnedElementOfType<Multiplicity>()
        assertTrue(fMultiplicity != null)
        assertTrue(fMultiplicity.isLibraryElement)
        assertEquals(5, fMultiplicity.elementId!!.version())
        val mPath = fMultiplicity.path()
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
    fun uuid5isGeneratedTest2() = testSession("Base") {
        loadKerML("""
                standard library package ScalarValues { datatype Natural; } // For multiplicity
                standard library package x {
                    feature f1;
                    feature f2 redefines f1;
                }; 
            """)
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val x = global.resolve<Package>("x")
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
    fun loadScalarValuesTest2() = testSession {
        LibraryRepository.loadLibraryFromResources("ScalarValues", listOf("Base", "ScalarValues"))
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun loadLinksTest() = testSession {
        LibraryRepository.get("Links")
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun loadOccurrencesTest() = testSession {
        LibraryRepository.get("Occurrences")
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun debug() = testSession("Base") {
        loadKerML("""
            package ScalarValues { datatype Natural; } // For multiplicity
            feature x { feature xx; }
            feature y: Base::Anything [1] redefines xx;  
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * Loading from a repository via cloning all elements from a repository; eventually,
     * there might be double-copies that shall be avoided, e.g., Any, Global, Package, ...
     */
    @Test
    fun basicSessionTest() = testSession("KerML") {
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertTrue(5 <= global.getOwnedElementsOfType<Element>().size)
        checkLibraryElementIds()
        checkOwnership()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
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