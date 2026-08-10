package services

import com.github.tukcps.sysmd.model.datamodel.version
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.check.checkLibraryElementIds
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.implementation.SessionImplementation
import org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES
import util.assertNoIssues
import util.loadLibraryArrangement
import util.mockup.loadKerML
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
                if (it.owner == null) {
                    globals++
                    assertEquals(global.elementId, it.elementId)
                }
            }
            assertNoIssues()
            assertEquals(1, globals)
        }
    }

    /**
     * Check that all children point to its correct parents that also own them.
     */
    private fun checkForParent( session: Session) : Boolean {
        with(session) {
            val allElements = get()
            allElements.forEach {  element ->
                if (element is Relationship && element !is Association && element !is Connector && element !is Dependency) {
                    if ( ! element.owningRelatedElement.ownedRelationship.map { it.elementId }.contains(element.elementId) )
                        return false
                } else {
                    if (element != global && element.owningRelationship?.ownedElement?.map { it.elementId }
                            ?.contains(element.elementId) != true)
                        return false
                }
            }
        }
        return true
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
        val f = global.resolve("test::f")!!.member<Type>()!!
        val spec = f.getOwnedElementOfType<Specialization>() !!
        val idVersion = spec.elementId.version
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
        assertSame(global, get(global.elementId))
        assertSame(repo.anything, get(repo.anything?.elementId!!))
    }

    /** Loading the Base library from resources */
    @Test
    fun readBaseFromResources() = testSession {
        loadLibraryArrangement("Base")
        // There are 5 Elements:
        // - Any, Package, the loaded package + 2 Imports iff pre-defined after initialize
        val elements = global.getOwnedElementsOfType<Element>()
        checkForOneGlobal(this)
        checkLibraryElementIds()
        checkForParent(this)
        assertNoIssues()
        assertEquals(1, elements.size)
    }


    /** Loading the Links library from resource works. Needs Base. */
    @Test
    fun readLinksFromResources() = testSession("") {
        loadLibrary("Base")
        loadLibrary("ScalarValues")
        loadLibrary("Links")
        // There are 5 Elements:
        // - Any, Package, the loaded package + 2 Imports iff pre-defined after initialize
        val elements = global.getOwnedElementsOfType<Element>()
        checkForOneGlobal(this)
        checkLibraryElementIds()
        checkForParent(this)
        assertNoIssues()
        assertEquals(3, elements.size, elements.toString()) // Base, ScalarValues, Links
    }

    /** Loading ScalarValues from resources. Needs Base, Links.  */
    @Test
    fun readOccurrencesFromResources() = testSession {
        loadLibraryArrangement("Occurrences")
        val elements = global.getOwnedElementsOfType<Element>()
        checkForOneGlobal(this)
        checkLibraryElementIds()
        checkForParent(this)
        assertNoIssues()
        assertEquals(4, elements.size)
    }

    /** Loading ScalarValues from resources works; needs Base */
    @Test
    fun readScalarValuesAndBaseFromResources() = testSession {
        loadLibraryArrangement("ScalarValues")
        val elements = global.getOwnedElementsOfType<Element>()
        checkForOneGlobal(this)
        checkForParent(this)
        assertNoIssues()
        assertEquals(2, elements.size)
    }

    /** Load and re-load Base and ScalarValues */
    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun readUseLibrary() {
        val session = SessionImplementation()
        session.loadLibraryArrangement("Base")
        session.loadLibraryArrangement("ScalarValues")
        session.initialize(Runlevel.MODEL)
        val elements = session.export().map { it.payloadElementSnapshot!! }
        // val sources = elements.filter { it.declaredName in setOf("source", "target") }
        assertTrue(session.status.issues.isEmpty(), session.status.issues.toString())
        checkForOneGlobal(session)
        session.checkOwnership()
        session.checkLibraryElementIds()
        assertTrue(session.status.issues.isEmpty(), session.status.issues.toString())

        val session2 = SessionImplementation()
        session2.import(elements)
        // val elements2 = session.export().data.map { it.payloadElementSnapshot!! }
        // val sources2 = elements2.filter { it.declaredName in setOf("source", "target") }
        session2.checkOwnership()
        session.checkLibraryElementIds()
        assertTrue(session2.status.issues.isEmpty(), session2.status.issues.toString())
    }


    /**
     * For testing ...
     */
    @Test
    fun readISO26262fromResources() = testSession {
        loadLibraryArrangement("ISO26262")
        initialize(Runlevel.MODEL)
        checkOwnership()
        assertNoIssues()
    }

    /** ISO26262 in particular uses Links */
    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    fun readISO26262FromRepository() = testSession("ISO26262") {
        checkOwnership()
        assertNoIssues()
        // Serialisierung/Deserialisierung klappt glaub ich nicht richtig hier. Hierarchiche Ownership-Struktur inkonsistent!
        val implements = global.resolve("ISO26262::implements")?.member<Association>()
        assertNotNull(implements)
        assertNotNull(implements)
        assertTrue(status.issues.isEmpty(), "${status.issues}")
        assertTrue(4 <= global.getOwnedElementsOfType<Element>().size)
        checkForOneGlobal(this)
        initialize(Runlevel.MODEL)
        checkOwnership()
        assertTrue(status.issues.isEmpty(), "${status.issues}")
    }
}
