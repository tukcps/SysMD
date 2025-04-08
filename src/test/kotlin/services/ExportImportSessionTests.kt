package services

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.check.checkConsistency
import com.github.tukcps.sysmd.services.resolve.resolve
import io.github.tukcps.sysmlv2.api.entities.getElements
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportImportSessionTests {

    /**
     * Check the import/export of relationships.
     * Source, target are preserved?
     */
    @Test
    fun exportImportRelationship() {
        var export = listOf<io.github.tukcps.sysmlv2.api.entities.ElementDAO>()

        // compute export && check it
        testSession("ScalarValues") {
            checkConsistency(repo.elements.values, checkForNoTransients = false)
            loadKerML("private import Base::*;")
            export = export().getElements().filter { it.type == "NamespaceImport" }
            val import = export.first()
            assertTrue {
                import.source!!.size == 1 && // <<---- 0 Bug
                import.target!!.size == 1 &&
                import.source!!.first().id != null &&
                import.target!!.first().id != null
            }
        }

        // Import export && check relationships
        testSession {
            import(export)
            val import = global.getOwnedElementOfType<Import>() !!
            assertTrue(import.source.size == 1 && import.source.first().id != null)
            assertTrue(import.target.size == 1 &&
                        import.target.first().id != null)
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
        var export: List<io.github.tukcps.sysmlv2.api.entities.ElementDAO> = mutableListOf()

        testSession {
            loadKerML("""
                type AA :> Base::Anything; 
                type B :> Base::Anything;
                type A :> Base::Anything { private import B; } // Import is created in A 
            """)
            assertTrue(status.issues.isEmpty(), status.issues.toString())
            val a = global.resolve<Type>("A")!!
            val b = global.resolve<Type>("B")!!

            val imp = a.getOwnedElementsOfType<Import>().first()
            assertEquals(a, imp.source.first().ref)
            assertEquals(a.elementId, imp.source.first().id)
            assertEquals(b, imp.target.first().ref)
            assertEquals(b.elementId, imp.target.first().id)

            val spec = a.getOwnedElementsOfType<Specialization>().first()
            assertEquals(a, spec.source.first().ref)
            assertEquals(a.elementId, spec.source.first().id)
            assertEquals(anything, spec.target.first().ref)
            assertEquals(anything.elementId, spec.target.first().id)

            // Save it in DB and see if
            export = export().filter { it.payloadElementSnapshot != null }.mapNotNull { it.payloadElementSnapshot }
        }

        // Check the correct representation in Backend
        testSession {
            // Restore it from DB ... and check again
            // loadProject("test", initialize = false)
            import(export)
            assertTrue(status.issues.isEmpty(), status.issues.toString())
            val a = global.resolve<Type>("A")!!
            val b = global.resolve<Type>("B")!!
            val imp = a.getOwnedElementsOfType<Import>().first()
            assertEquals(a.elementId, imp.source.first().id)
            assertEquals(b.elementId, imp.target.first().id)

            val spec = a.getOwnedElementsOfType<Specialization>().first()
            assertEquals(a.elementId, spec.source.first().id)
            assertEquals(anything.elementId, spec.target.first().id)
        }
    }
}