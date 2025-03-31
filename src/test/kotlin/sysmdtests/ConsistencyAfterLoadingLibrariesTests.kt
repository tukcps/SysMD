package sysmdtests

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.check.checkLibraryElementIds
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionImplementation
import util.mockup.loadKerML
import com.github.tukcps.sysmd.services.session.loadLibrary
import org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES
import util.testSession
import kotlin.test.*

class ConsistencyAfterLoadingLibrariesTests {
    

    /**
     * Helper to check that there is exactly one global element, and it is the same as
     * in the project announced.
     */
    private fun checkForOneGlobal( session: Session) {
        with(session) {
            // There is exactly one Global ...
            val allElements = get()
            var globals = 0
            allElements.forEach {
                if (it.owner.ref == null) {
                    globals++
                    assertEquals(global.elementId, it.elementId)
                }
            }
            assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
            assertEquals(1, globals)
        }
    }

    /**
     * Check that all children point to its correct parents that also own them.
     */
    private fun checkForParent( session: Session) {
        with(session) {
            val allElements = get()
            allElements.forEach {  element ->
                if (element.owner.ref != null) {
                    val ownedByOwner = element.owner.ref?.ownedElement?.associateBy { it.id }?.keys
                    assertTrue(ownedByOwner?.contains(element.elementId) != false)
                }
            }
        }
    }

    /**
     * The standard currently assumes that elements with no name have a UUID4.
     * This leads to trouble when re-reading standard libraries due to random UUIDs in these elements.
     * SysMD avoids this trouble by giving these elements UUID version 5.
     * (Issue discussed in OMG; standard might follow.)
     */
    @Test
    fun checkMultiplicityInLibraryIsUUID5() = testSession {
        loadKerML("""
            standard library package test {
                type f :> Base::Anything; 
            }
        """)
        val f = global.resolve<Type>("test::f") !!
        val spec = f.getOwnedElementOfType<Specialization>() !!
        val idVersion = spec.elementId!!.version()
        assertEquals(5, idVersion)
    }

    /**
     * Check basic invariants of bare, fresh session
     * - The root namespace owns all elements
     * - All elements are subclasses of Any(thing).
     */
    @Test
    fun checkBasicConsistency() = testSession {
        checkForOneGlobal(this)
        checkOwnership()
        assertSame(global, get(global.elementId!!))
        assertSame(anything, get(anything.elementId!!))
    }

    /** Loading the Base library from resources */
    @Test
    fun readBaseFromResources() = testSession {
        settings.catchExceptions=false
        loadLibrary("Base.md")
        // There are 5 Elements:
        // - Any, Package, the loaded package + 2 Imports iff pre-defined after initialize
        val elements = global.getOwnedElementsOfType<Element>()
        checkForOneGlobal(this)
        checkLibraryElementIds()
        checkForParent(this)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(1, elements.size)
    }


    /** Loading the Links library from resource works. Needs Base. */
    @Test
    fun readLinksFromResources() = testSession {
        settings.catchExceptions=false
        // loadLibrary("Base")
        loadLibrary("Links")
        // There are 5 Elements:
        // - Any, Package, the loaded package + 2 Imports iff pre-defined after initialize
        val elements = global.getOwnedElementsOfType<Element>()
        checkForOneGlobal(this)
        checkLibraryElementIds()
        checkForParent(this)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(3, elements.size, elements.toString())
    }

    /** Loading ScalarValues from resources. Needs Base, Links.  */
    @Test
    fun readOccurrencesFromResources() = testSession {
        loadLibrary("Occurrences")
        val elements = global.getOwnedElementsOfType<Element>()
        checkForOneGlobal(this)
        checkLibraryElementIds()
        checkForParent(this)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(4, elements.size)
    }

    /** Loading ScalarValues from resources works; needs Base */
    @Test
    fun readScalarValuesAndBaseFromResources() = testSession {
        settings.catchExceptions=false
        loadLibrary("ScalarValues")
        val elements = global.getOwnedElementsOfType<Element>()
        checkForOneGlobal(this)
        checkForParent(this)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(2, elements.size)
    }

    /** Load and re-load Base and ScalarValues */
    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun readUseAssociation() {
        val session = SessionImplementation(libraries = mutableListOf())
        session.loadLibrary("Base")
        session.loadLibrary("ScalarValues")
        session.initialize()
        val elements = session.export().map { it.payloadElementSnapshot!! }
        // val sources = elements.filter { it.declaredName in setOf("source", "target") }
        assertTrue(session.status.exceptions.isEmpty(), session.status.exceptions.toString())
        checkForOneGlobal(session)
        session.checkOwnership()
        session.checkLibraryElementIds()
        assertTrue(session.status.exceptions.isEmpty(), session.status.exceptions.toString())

        val session2 = SessionImplementation(libraries = mutableListOf())
        session2.import(elements)
        // val elements2 = session.export().data.map { it.payloadElementSnapshot!! }
        // val sources2 = elements2.filter { it.declaredName in setOf("source", "target") }
        session2.checkOwnership()
        session.checkLibraryElementIds()
        assertTrue(session2.status.exceptions.isEmpty(), session2.status.exceptions.toString())
    }


    /** ISO26262 in particular uses Links */
    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun readISO26262FromRepository() = testSession("ISO26262") {
        val implements = global.resolve<Association>("ISO26262::implements")
        assertNotNull(implements)
        assertTrue(status.exceptions.isEmpty(), "${status.exceptions}")
        assertTrue(4 <= global.getOwnedElementsOfType<Element>().size)
        checkForOneGlobal(this)
        settings.initialize = true
        initialize()
        checkOwnership()
        assertTrue(status.exceptions.isEmpty(), "${status.exceptions}")
    }
}
