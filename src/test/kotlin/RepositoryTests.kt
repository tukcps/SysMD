
import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.Specialization
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.compiler.loadProject
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmlv2.entities.ElementDAO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class RepositoryTests {


    @Test
    fun exportImportRelationship() {
        var export = listOf<ElementDAO>()

        // compute export && check it
        testSession {
            loadSysMD("import Base;")
            export = export().getElements().filter { it.type == "NamespaceImport" }
            val import = export.first()
            assertTrue {
                import.source!!.size == 1 &&
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
     * As Class is a Namespace, the following shall work.
     * Check ensuring that imports and specialization are correctly created.
     * Both ref and id are set, and both sources and targets are set.
     */
    @Test
    fun exportImportRelationships() {
        var export: List<ElementDAO> = mutableListOf()

        testSession {
            loadSysMD("""
                class A;  // Specialization is created in A
                class AA; 
                class B;
                class A { import B; } // Import is created in A """.trimIndent()
            )
            Assertions.assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
            val a = global.resolve<ClassImplementation>("A")!!
            val b = global.resolve<ClassImplementation>("B")!!

            val imp = a.getOwnedElementsOfType<Import>().first()
            Assertions.assertEquals(a, imp.source.first().ref)
            Assertions.assertEquals(a.elementId, imp.source.first().id)
            Assertions.assertEquals(b, imp.target.first().ref)
            Assertions.assertEquals(b.elementId, imp.target.first().id)

            val spec = a.getOwnedElementsOfType<Specialization>().first()
            Assertions.assertEquals(a, spec.source.first().ref)
            Assertions.assertEquals(a.elementId, spec.source.first().id)
            Assertions.assertEquals(any, spec.target.first().ref)
            Assertions.assertEquals(any.elementId, spec.target.first().id)

            // Save it in DB and see if
            export = export().data.filter { it.payloadElementSnapshot != null }.mapNotNull { it.payloadElementSnapshot }
        }

        // Check the correct representation in Backend
        testSession {
            // Restore it from DB ... and check again
            // loadProject("test", initialize = false)
            import(export)
            Assertions.assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
            val a = global.resolve<ClassImplementation>("A")!!
            val b = global.resolve<ClassImplementation>("B")!!
            val imp = a.getOwnedElementsOfType<Import>().first()
            Assertions.assertEquals(a.elementId, imp.source.first().id)
            Assertions.assertEquals(b.elementId, imp.target.first().id)

            val spec = a.getOwnedElementsOfType<Specialization>().first()
            Assertions.assertEquals(a.elementId, spec.source.first().id)
            Assertions.assertEquals(any.elementId, spec.target.first().id)
        }
    }

    @Test
    fun projectCanBeLoaded() = testSession("ScalarValues", "ISO26262", "Math" ) {
        loadProject("Math")
    }
}