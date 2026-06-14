package compiler

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.ClassActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.NamespaceActions
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.implementation.SessionImplementation
import io.github.tukcps.aadd.values.IntegerRange
import util.assertNoIssues
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests of the semantic actions.
 */
class ActionsContextTests {

    /** Helper that sets up a valid semantics object */
    private fun testSemantics(): ActionsContext {
        val model = SessionImplementation(libraries = mutableListOf())
        return KerML(model = model).semantics
    }


    /**
     * Adding a class creates a new class in the KerML model; the explicit ownership name is used.
     */
    @Test
    fun testClassActions() {
        testSemantics().run {
            ClassActions<Class>(this, ::ClassImplementation).parse {
                create(Identification("klass"))
            }
            model.initialize(Runlevel.NAMES_RESOLVED)
            val klass = model.global.resolve("klass")?.memberElement as Class?
            assertNotNull(klass)
            assertTrue(model.status.issues.isEmpty(), model.status.issues.toString())
        }
    }

    /**
     * Adding a class creates a new class in the KerML model; the semantics-internal ownership stack is used.
     */
    @Test
    fun testAddClassInPackage() {
        testSemantics().run {
            NamespaceActions(this, ::PackageImplementation).parse {
                create(Identification("pkg"))
                ClassActions<Class>(this@run, ::ClassImplementation).parse {
                    create(Identification("klass"))
                    addSpecialization("Base::Anything")
                }
            }
            model.initialize(Runlevel.NAMES_RESOLVED)
            assertTrue(model.status.issues.isEmpty(), model.status.issues.toString())
            assertNotNull(model.global.resolve("pkg::klass")?.memberElement)
        }
    }

    /**
     * Adding a feature instance to the KerML model also creates Specialization and Multiplicity.
     */
    @Test
    fun testAddFeature() {
        testSemantics().run {
            FeatureActions<Feature>(this, ::FeatureImplementation).parse {
                create(Identification("feature"))
                addTyping("Base::Anything")
                addMultiplicity(IntegerRange(2, 3))
            }
            model.initialize(Runlevel.NAMES_RESOLVED)
            val feature = model.global.resolve("feature")?.memberElement
            assertNotNull(feature)
            val typing = feature.getOwnedElementOfType<FeatureTyping>()
            assertNotNull(typing)
            val multiplicity = feature.getOwnedElementOfType<Multiplicity>()
            assertNotNull(multiplicity)
            assertEquals(model.anything, typing.type)
            assertEquals(feature, typing.typedFeature)
            assertEquals(IntegerRange(2, 3) , IntegerRange(multiplicity.typeConstraint.firstOrNull()!!) )
        }
    }


    @Test
    fun testAddMultiplicity() = testSession {
        val semantics = KerML(this).semantics
        val feature = FeatureActions<Feature>(semantics, ::FeatureImplementation).parse {
            semantics.create(Identification("test"))
            semantics.addTyping("ScalarValues::Integer")
            semantics.addMultiplicity(IntegerRange(2, 3))
        }
        initialize(Runlevel.NAMES_RESOLVED)
        val multiplicity = feature.getOwnedElementOfType<Multiplicity>()
        assertNotNull(multiplicity)
        assertEquals("2 .. 3", multiplicity.typeConstraint.firstOrNull())
        assertNoIssues()
    }
}