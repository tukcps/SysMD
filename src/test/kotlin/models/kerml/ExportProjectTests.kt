package models.kerml

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.model.kerml.Specialization
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.loadLibrary
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import io.github.tukcps.sysmlv2.api.entities.getElements
import util.assertNoIssues
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
        val classifier = addOwnedMember(ClassImplementation(declaredName="c"), global)
        addOwnedRelationship(SpecializationImplementation(classifier, anything), classifier)
        assertNotNull(classifier.getOwnedElementOfType<Specialization>())
        assertNotNull(classifier.getOwnedElementOfType<Specialization>()?.elementId)
        initialize()
        assertNoIssues()
        val record = export()
        assertNotNull(record)
        // c is in record
        val c=record.getElements().first { it.name=="c" }
        assertNotNull(c)

        // Specialization is in record
        val spec=record.getElements().first { it.type == "Specialization"}
        assertNotNull(spec)
        assertEquals(anything.elementId, spec.target?.first()?.id)
    }


    /**
     * Test: Create export and import record with Class
     */
    @Test
    fun importClassTest() {
        var export: List<ElementDAO> = listOf()
        testSession {
            val classifier = addOwnedMember(TypeImplementation(declaredName = "c"), global)
            addOwnedRelationship(SpecializationImplementation(classifier, anything))
            assertNotNull(classifier.getOwnedElementOfType<Specialization>())
            assertNotNull(classifier.getOwnedElementOfType<Specialization>()?.elementId)
            // initialize()
            assertNoIssues()
            export = export().getElements()
        }
        testSession {
            import(export)
            checkOwnership()
            initialize()
            val c = global.resolve("c")?.member<Type>()
            assertNotNull(c)
            assertTrue(c.ownedSpecialization.isNotEmpty())
            assertEquals(c.ownedSpecialization.first().owningRelatedElement.elementId, c.elementId)
            assertNotNull(c.owner)
            assertNotNull(c.owningRelationship?.elementId)
        }
    }

    /**
     * Test: Create export record with Package
     */
    @Test
    fun exportPackageTest() = testSession {
        val p = addOwnedMember(PackageImplementation(declaredName="p"), global)
        val f = addOwnedMember(FeatureImplementation(declaredName ="f"), p)
        addOwnedMember(MultiplicityImplementation(multiplicity = IntegerRange(1,3).toString()), f)
        initialize(2)
        assertTrue(status.issues.none { it.kind.ordinal >= Issue.Kind.ERROR.ordinal }, status.issues.toString())
        val record = export()
        assertNotNull(record)

        // p is in record
        val pkg=record.getElements().first { it.name=="p" }
        assertNotNull(pkg)
        assertEquals(null, pkg.owner?.id)         // Global represented by null

        // f is in record
        val feat=record.getElements().first { it.name=="f" }
        assertNotNull(feat)
        assertEquals(pkg.elementId, feat.owner?.id)         // Global represented by null

        // Multiplicity is in record
        val multiplicity=record.getElements().first { it.type == "Multiplicity" }
        assertNotNull(multiplicity)
        assertEquals(f.elementId, multiplicity.owner?.id)
    }

    @Test
    fun importPackageTest() {
        var export: List<ElementDAO> = emptyList()
        testSession {
            addOwnedMember(PackageImplementation(declaredName="p"), global)
            assertTrue(status.issues.isEmpty(), status.issues.toString())
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
        var export: List<ElementDAO> = emptyList()
        testSession("ScalarValues") {
            assertNoIssues()
            export = export().getElements()
        }
        testSession("ScalarValues") {
            import(export)
            assertNotNull(repo.booleanType)
            assertNotNull(repo.integerType)
            assertNotNull(repo.numberType)
            assertNotNull(repo.realType)
            val no = repo.elements.size
            import(export)
            assertEquals(no, repo.elements.size)
        }
    }

    @Test
    fun importViaRepositoryCache() {
        testSession("ScalarValues") {
            initialize()
            solver.propagate()
            get().forEach { element ->
                assertEquals(builder, element.model?.builder)
                if (element is Relationship) {
                    element.source.forEach { source ->
                        assertEquals(builder, source.model?.builder)
                    }
                    element.target.forEach { target ->
                        assertEquals(builder, target.model?.builder)
                    }
                }
            }
        }

        testSession("ScalarValues") {
            get().forEach { element ->
                assertEquals(builder, element.model?.builder)
                if (element is Relationship) {
                    element.source.forEach { source ->
                        assertEquals(builder, source.model?.builder)
                    }
                    element.target.forEach { target ->
                        assertEquals(builder, target.model?.builder)
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
            loadLibrary("Math")
            val pi = global.resolve("Math::pi")
            loadLibrary("Math")
            loadLibrary("Math")
            val pi2 = global.resolve("Math::pi")
            assertEquals(pi?.memberElement?.elementId, pi2?.memberElement?.elementId)
        }
    }
}
