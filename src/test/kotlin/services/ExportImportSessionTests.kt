package services

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.check.checkConsistency
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import io.github.tukcps.sysmlv2.api.entities.getElements
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExportImportSessionTests {

    /**
     * Check the import/export of relationships.
     * Source, target are preserved?
     */
    @Test
    fun exportImportRelationship() {
        var export = listOf<ElementDAO>()

        // compute export && check it
        testSession("ScalarValues") {
            checkConsistency(repo.elements.values, checkForNoTransients = false)
            loadKerML("private import Base::*;")
            export = export().getElements().filter { it.type == "NamespaceImport" }
            val import = export.first()
            assertEquals(1, import.source!!.size)
            assertEquals(1, import.target!!.size)
            assertNull(import.source!!.first().id) // Importing namespace is Global.
            assertNotNull(import.target!!.first().id)
        }

        // Import export && check relationships
        testSession {
            import(export)
            val import = global.getOwnedElementOfType<Import>() !!
            assertTrue(import.source.size == 1 && import.source.first().elementId != null)
            assertTrue(import.target.size == 1 &&
                        import.target.first().elementId != null)
        }
    }


    /**
     * Imports are only allowed to Namespace.
     * As Type is a Namespace, the following shall work.
     * Check ensuring that imports and specialization are correctly created.
     * Both ref and id are set, and both sources and targets are set.
     */
    @Test
    fun exportImportRelationships() {
        var export: List<ElementDAO> = mutableListOf()

        testSession {
            loadKerML("""
                type AA :> Base::Anything; 
                type B :> Base::Anything;
                type A :> Base::Anything { private import B; } // Import is created in A 
            """)
            assertNoIssues()
            val a = global.resolve("A")!!.member<Type>()
            val b = global.resolve("B")

            val imp = a!!.getOwnedElementsOfType<Import>().first()
            assertEquals(a, imp.source.first())
            assertEquals(a.elementId, imp.source.first().elementId)
            assertEquals(b!!, imp.target.first())
            assertEquals(b.elementId, imp.target.first().elementId)

            val spec = a.getOwnedElementsOfType<Specialization>().first()
            assertEquals(a, spec.source.first())
            assertEquals(a.elementId, spec.source.first().elementId)
            assertEquals(anything, spec.target.first())
            assertEquals(anything.elementId, spec.target.first().elementId)

            // Save it in DB and see if
            export = export().filter { it.payloadElementSnapshot != null }.mapNotNull { it.payloadElementSnapshot }
        }

        // Check the correct representation in Backend
        testSession {
            // Restore it from DB ... and check again
            // loadProject("test", initialize = false)
            import(export)
            assertNoIssues()
            val a = global.resolve("A")!!.member<Type>()!!
            val b = global.resolve("B")!!.member<Type>()!!
            val imp = a.getOwnedElementsOfType<MembershipImport>().first()
            assertEquals(a.elementId, imp.source.first().elementId)
            assertEquals(b.owningRelationship?.elementId, imp.target.first().elementId)

            val spec = a.getOwnedElementsOfType<Specialization>().first()
            assertEquals(a.elementId, spec.source.first().elementId)
            assertEquals(anything.elementId, spec.target.first().elementId)
        }
    }
}