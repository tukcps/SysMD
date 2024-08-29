package models.kerml

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.compiler.loadProject
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmlv2.entities.ElementDAO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@Suppress("UNUSED_VARIABLE")
class ExportProjectTests {

    /**
     * Test: Create export record with Class
     */
    @Test
    fun exportClassTest() = testSession(loadKerML = false) {
        val classifier = create(ClassImplementation(declaredName="c"), global)
        val specialization = create(SpecializationImplementation(classifier, any), classifier)
        assertNotNull(classifier.getOwnedElementOfType<Specialization>())
        assertNotNull(classifier.getOwnedElementOfType<Specialization>()?.elementId)
        initialize()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val record = export()
        assertNotNull(record)

        // c is in record
        val c=record.getElements().first { it.name=="c" }
        assertNotNull(c)
        assertEquals(null, c.owner?.id)         // Global represented by null

        // Specialization is in record
        val spec=record.getElements().first { it.type == "Specialization"}
        assertNotNull(spec)
        assertEquals(any.elementId, spec.target?.first()?.id)
        assertEquals(c.elementId, spec.owner?.id)
    }


    /**
     * Test: Create export and import record with Class
     */
    @Test
    fun importClassTest() {
        var export: List<ElementDAO> = listOf()
        testSession {
            val classifier = create(TypeImplementation(declaredName = "c"), global)
            create(SpecializationImplementation(classifier, any), classifier)
            assertNotNull(classifier.getOwnedElementOfType<Specialization>())
            assertNotNull(classifier.getOwnedElementOfType<Specialization>()?.elementId)
            initialize()
            assertEquals(0, status.exceptions.size, status.exceptions.toString())
            export = export().getElements()
        }
        testSession {
            import(export)
            initialize()
            val c = global.resolve<TypeImplementation>("c")
            assertNotNull(c)
            assertTrue(c!!.ownedSpecialization.isNotEmpty())
            assertEquals(c.ownedSpecialization.first().owner.id, c.elementId)
            assertNotNull(c.owner.ref)
            assertNotNull(c.owner.id)
        }
    }

    /**
     * Test: Create export record with Package
     */
    @Test
    fun exportPackageTest() = testSession( loadKerML = false ) {
        val p = create(PackageImplementation(declaredName="p"), global)
        val f = create(FeatureImplementation(declaredName ="f"), p)
        create(MultiplicityImplementation(multiplicity = IntegerRange(1,3).toString()), f)
        initialize(2)
        assertTrue(status.exceptions.none { it is SysMDError }, status.exceptions.toString())
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
        testSession(loadKerML = false) {
            loadSysMD("package ScalarValues { datatype Integer; }")
            val p = create(PackageImplementation(declaredName="p"), global)
            val f = create(FeatureImplementation(declaredName ="f"), p)
            create(MultiplicityImplementation(multiplicity = IntegerRange(1,3).toString()), f)
            create(SpecializationImplementation(general = Resolved("Base::Anything"), specific = Resolved(ref = f)), f)
            initialize()
            assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
            export = export().getElements()
        }
        testSession(loadKerML = false) {
            import(export)
            initialize()
            propagate()
            val p = global.resolve<PackageImplementation>("p")
            val f = p?.resolve<FeatureImplementation>("f")
            val m = f?.getOwnedElementOfType<Multiplicity>()
            assertNotNull(p)
            assertNotNull(f)
            assertNotNull(m)
            assertTrue(p!!.owner.ref == global)
            assertTrue(f!!.owner.ref == p)
            assertTrue(m!!.owner.ref == f)
        }
    }

    /**
     * Test: Create export and import record with Class
     */
    @Test
    fun importScalarValuesTest() {
        var export: List<ElementDAO> = emptyList()
        testSession {
            initialize()
            assertEquals(0, status.exceptions.size, status.exceptions.toString())
            export = export().getElements()
        }
        testSession {
            import(export)
            initialize()
            assertNotNull(repo.booleanType)
            assertNotNull(repo.integerType)
            assertNotNull(repo.numberType)
            assertNotNull(repo.realType)
            val no = repo.elements.size
            import(export)
            initialize()
            propagate()
            assertEquals(no, repo.elements.size)
        }
    }

    @Test
    fun importViaRepositoryCache() {
        testSession("ScalarValues", "ISO26262") {
            initialize()
            propagate()
            get().forEach { element ->
                assertEquals(builder, element.model?.builder)
                if (element is Relationship) {
                    element.source.forEach { source ->
                        if (source.ref != null)
                            assertEquals(builder, source.ref?.model?.builder)
                    }
                    element.target.forEach { target ->
                        assertEquals(builder, target.ref?.model?.builder)
                    }
                }
            }
        }

        testSession("ScalarValues", "ISO26262") {
            get().forEach { element ->
                assertEquals(builder, element.model?.builder)
                if (element is Relationship) {
                    element.source.forEach { source ->
                        if (source.ref != null)
                            assertEquals(builder, source.ref?.model?.builder)
                    }
                    element.target.forEach { target ->
                        if (builder != target.ref?.model?.builder)
                            println()
                        assertEquals(builder, target.ref?.model?.builder)
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
            loadProject("Math")
            val pi = global.resolve<Feature>("Math::pi")!!
            loadProject("Math")
            loadProject("Math")
            val pi2 = global.resolve<Feature>("Math::pi")!!
            assertEquals(pi.elementId, pi2.elementId)
        }
    }
}
