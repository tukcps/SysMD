package models.kerml

import com.github.tukcps.sysmd.model.generated.ElementDataIF
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.model.kerml.Specialization
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.rest.entities.api.entities.getElements
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import util.assertNoIssues
import util.loadLibraryArrangement
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("UNUSED_VARIABLE")
class ExportProjectTests {

    /**
     * Test: Create export record with Class
     */
    @Test
    fun exportClassTest() = testSession {
        val classifier = addOwnedMember(ClassImplementation(this, declaredName="c"), global)
        addOwnedRelationship(SpecializationImplementation(this, specific = classifier, general = repo.anything!!), classifier)
        assertNotNull(classifier.getOwnedElementOfType<Specialization>())
        assertNotNull(classifier.getOwnedElementOfType<Specialization>()?.elementId)
        initialize(Runlevel.MODEL)
        assertNoIssues()
        val record = export()
        assertNotNull(record)
        // c is in record
        val c=record.getElements().first { it.declaredName=="c" }
        assertNotNull(c)

        // Specialization is in record
        val spec=record.getElements().first { it.type.name == "Specialization"}
        assertNotNull(spec)
        assertEquals(repo.anything!!.elementId, spec.target.first().id)
    }


    /**
     * Test: Create export and import record with Class
     */
    @Test
    fun importClassTest() {
        var export: List<ElementDataIF> = listOf()
        testSession {
            val classifier = addOwnedMember(TypeImplementation(this, declaredName = "c"), global)
            addOwnedRelationship(SpecializationImplementation(this, specific = classifier, general = repo.anything!!))
            assertNotNull(classifier.getOwnedElementOfType<Specialization>())
            assertNotNull(classifier.getOwnedElementOfType<Specialization>()?.elementId)
            // initialize()
            assertNoIssues()
            export = export().getElements()
        }
        testSession {
            import(export)
            checkOwnership()
            initialize(Runlevel.MODEL)
            val c = global.resolve("c")?.member<Type>()
            assertNotNull(c)
            assertTrue(c.ownedSpecialization.isNotEmpty())
            assertEquals(c.ownedSpecialization.first().owningRelatedElement.elementId, c.elementId)
            assertNotNull(c.owner)
            assertNotNull(c.owningRelationship?.elementId)
        }
    }


    @Test
    fun importPackageTest() {
        var export: List<ElementDataIF> = emptyList()
        testSession {
            addOwnedMember(PackageImplementation(this, declaredName="p"), global)
            assertNoIssues()
            export = export().getElements()
        }
        testSession {
            import(export)
            val p = global.resolve("p")
            assertNotNull(p?.memberElement as? PackageImplementation)
            assertEquals(p.owner, global)
        }
    }

    /**
     * Test: Create export and import record with Class
     */
    @Test
    fun importScalarValuesTest() {
        var export: List<ElementDataIF> = emptyList()
        testSession("ScalarValues") {
            assertNoIssues()
            assertNotNull(repo.booleanType)
            assertNotNull(repo.integerType)
            assertNotNull(repo.numberType)
            assertNotNull(repo.realType)
            export = export().getElements()
        }
        testSession("ScalarValues") {
            import(export)
            assertNotNull(repo.booleanType)
            assertNotNull(repo.integerType)
            assertNotNull(repo.numberType)
            assertNotNull(repo.realType)
            val no = repo.elements().size
            import(export)
            assertEquals(no, repo.elements().size)
        }
    }

    @Test
    fun importViaRepositoryCache() {
        testSession("ScalarValues") {
            solver.propagate()
            get().forEach { element ->
                assertEquals(builder, element.model.builder)
                if (element is Relationship) {
                    element.source.forEach { source ->
                        assertEquals(builder, source.model.builder)
                    }
                    element.target.forEach { target ->
                        assertEquals(builder, target.model.builder)
                    }
                }
            }
        }

        testSession("ScalarValues") {
            get().forEach { element ->
                assertEquals(builder, element.model.builder)
                if (element is Relationship) {
                    element.source.forEach { source ->
                        assertEquals(builder, source.model.builder)
                    }
                    element.target.forEach { target ->
                        assertEquals(builder, target.model.builder)
                    }
                }
            }
        }
    }


    /**
     * The elementId should be given once and then maintained.
     */
    @Test
    fun repeatedUsage() {
        testSession {
            loadLibraryArrangement("Math")
            val pi = global.resolve("Math::pi")
            loadLibraryArrangement("Math")
            loadLibraryArrangement("Math")
            val pi2 = global.resolve("Math::pi")
            assertEquals(pi?.memberElement?.elementId, pi2?.memberElement?.elementId)
        }
    }
}
