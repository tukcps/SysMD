package sysmdtests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.compiler.loadLibrary
import com.github.tukcps.sysmd.services.check.checkLibraryElementIds
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.createKerMLIntrospectionLibrary
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionImplementation
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions
import kotlin.test.*

class ConsistencyAfterLoadingTests {

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
     * Check that all children point to its parents.
     */
    private fun checkForParent( session: Session) {
        with(session) {
            val allElements = get()
            allElements.forEach {  element ->
                if (element.owner.ref != null) {
                    val ownedByOwner = element.owner.ref?.ownedElement?.associateBy { it.id }?.keys
                    require(ownedByOwner?.contains(element.elementId) != false)
                }
            }
        }
    }

    /**
     * Check basic invariants of bare, fresh session
     * - The root namespace owns all elements
     * - All elements are subclasses of Any(thing).
     */
    @Test
    fun checkBasicConsistency() = testSession(loadKerML = false) {
        checkForOneGlobal(this)
        checkOwnership()
        assertSame(global, get(global.elementId))
        assertSame(any, get(any.elementId))
    }

    /** Loading the Base library from resources */
    @Test
    fun readBaseFromResources() = testSession(loadKerML = false) {
        settings.catchExceptions=false
        createKerMLIntrospectionLibrary()
        loadLibrary("Base.md", false)
        // There are 5 Elements:
        // - Any, Package, the loaded package + 2 Imports iff pre-defined after initialize
        val elements = global.getOwnedElementsOfType<Element>()
        checkForOneGlobal(this)
        checkLibraryElementIds()
        checkForParent(this)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(2, elements.size)
    }


    /** Loading the Links library from resources */
    @Test
    fun readLinksFromResources() = testSession(loadKerML = false) {
        settings.catchExceptions=false
        createKerMLIntrospectionLibrary()
        loadLibrary("Base.md", false)
        loadLibrary("Links.md", false)
        // There are 5 Elements:
        // - Any, Package, the loaded package + 2 Imports iff pre-defined after initialize
        val elements = global.getOwnedElementsOfType<Element>()
        checkForOneGlobal(this)
        checkLibraryElementIds()
        checkForParent(this)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(3, elements.size)
    }

    /** Loading ScalarValues from resources */
    @Test
    fun readOccurrencesFromResources() = testSession(loadKerML = false) {
        settings.catchExceptions=false
        createKerMLIntrospectionLibrary()
        loadLibrary("Base.md", false)
        loadLibrary("Links.md", false)
        loadLibrary("Occurrences.md", false)
        // There are 5 Elements:
        // - Any, Package, the loaded package + 2 Imports iff pre-defined after initialize
        val elements = global.getOwnedElementsOfType<Element>()
        checkForOneGlobal(this)
        checkLibraryElementIds()
        checkForParent(this)
        Assertions.assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(4, elements.size)
    }

    /** Loading ScalarValues from resources */
    @Test
    fun readScalarValuesFromResources() = testSession(loadKerML = false) {
        settings.catchExceptions=false
        createKerMLIntrospectionLibrary()
        loadLibrary("Base.md", false)
        loadLibrary("Links.md", false)
        loadLibrary("Occurrences.md", false)
        loadLibrary("ScalarValues.md", false)
        // There are 5 Elements:
        // - Any, Package, the loaded package + 2 Imports iff pre-defined after initialize
        val elements = global.getOwnedElementsOfType<Element>()
        checkForOneGlobal(this)
        checkForParent(this)
        Assertions.assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(5, elements.size)
    }



    @Test
    fun readUseAssociation() {
        val session = SessionImplementation()
        session.propagate()
        val elements = session.export().data.map { it.payloadElementSnapshot!! }
        // val sources = elements.filter { it.declaredName in setOf("source", "target") }
        assertTrue(session.status.exceptions.isEmpty(), "Exceptions messages: ${session.status.exceptions}")
        checkForOneGlobal(session)
        session.checkOwnership()
        assertTrue(session.status.exceptions.isEmpty(), "Error messages: ${session.status.exceptions}")

        val session2 = SessionImplementation()
        session2.import(elements)
        // val elements2 = session.export().data.map { it.payloadElementSnapshot!! }
        // val sources2 = elements2.filter { it.declaredName in setOf("source", "target") }
        session2.checkOwnership()
        assertTrue(session2.status.exceptions.isEmpty(), session2.status.exceptions.toString())
    }



    /** ISO26262 in particular uses Links */
    @Test
    fun readISO26262FromRepository() = testSession(  "ISO26262", initialize = false) {
        val implements = global.resolve<Association>("ISO26262::implements")
        assertNotNull(implements)
        assertTrue(status.exceptions.isEmpty(), "Exceptions messages: ${status.exceptions}")
        assertEquals(8, global.getOwnedElementsOfType<Element>().size)
        checkForOneGlobal(this)
        settings.initialize = true
        initialize()
        checkOwnership()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
    }
}
