package kermltests

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.compiler.loadProject
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.check.checkLibraryElementIds
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class LibrariesTests {

    /** Check that UUID5 are generated for the fully qualified names in standard library packages */
    @Test
    fun uuid5isGeneratedTest1() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValue { datatype Integer; }
            standard library package x {
                class c; 
                feature f; 
                datatype d; 
                package p {
                    class c; 
                }
                
            }; 
        """.trimIndent())
        // assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val x = global.resolve<Package>("x::p")
        assertNotNull(x)
        assertTrue(x.isLibraryElement)
        assertEquals(5, x.elementId.version())
        val uuid5 = Generators.nameBasedGenerator().generate(x.qualifiedName)
        assertEquals(uuid5, x.elementId)

        get().forEach { element ->
            if(element.isLibraryElement) {
                val uuid5 = Generators.nameBasedGenerator().generate(element.qualifiedName)
                assertEquals(uuid5, element.elementId)
            }
        }
    }


    /** Check that UUID5 are generated for the fully qualified names in standard library packages */
    @Test
    fun uuid5isGeneratedTest2() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValues { datatype Integer; }
            standard library package x {
                feature f1;
                feature f2 redefines f1;
            }; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val x = global.resolve<Package>("x")
        assertNotNull(x)
        assertTrue(x.isLibraryElement)
        assertEquals(5, x.elementId.version())
        val uuid5 = Generators.nameBasedGenerator().generate(x.qualifiedName)
        assertEquals(uuid5, x.elementId)

        get().forEach { element ->
            if(element.isLibraryElement) {
                val uuid5 = Generators.nameBasedGenerator().generate(element.qualifiedName)
                assertEquals(uuid5, element.elementId)
            }
        }
    }

    /**
     * Loading from repository via cloning all elements from a repository; eventually,
     * there might be double-copies that shall be avoided, e.g., Any, Global, Package, ...
     */
    @Test
    fun basicSessionTest() = testSession {
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(6, global.getOwnedElementsOfType<Element>().size)
        checkLibraryElementIds()
        checkOwnership()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }


    /**
     * Loading from repository via cloning all elements from a repository; eventually,
     * there might be double-copies that shall be avoided, e.g. Any, Global, Package, ...
     */
    @Test
    fun basicSessionWithReload() = testSession {
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
        assertEquals(6, global.getOwnedElementsOfType<Element>().size)
        checkLibraryElementIds()
        checkOwnership()
        loadProject("ScalarValues", false)
        checkOwnership()
        checkLibraryElementIds()
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
    }

}