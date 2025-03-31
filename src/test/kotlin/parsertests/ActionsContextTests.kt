package parsertests

import io.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.SemanticActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.ClassActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionImplementation
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

/**
 * Tests of the semantic actions.
 */
class ActionsContextTests {

    /** Helper that sets up a valid semantics object */
    private fun testSemantics(): SemanticActions {
        val model = SessionImplementation()
        val textualRepresentation = model.create(TextualRepresentationImplementation(language = "SysMD", body = ""), model.global)
        return KerML(model = model).semantics.also { it.textualRepresentation = textualRepresentation }
    }


    /**
     * Adding a class creates a new class in the KerML model; the explicit ownership name is used.
     */
    @Test
    fun testClassActions() {
        testSemantics().run {
            ClassActions<Class>(this, ::ClassImplementation).create(Identification("klass"))
            model.initialize(1)
            val klass = model.global.resolve<Class>("klass")
            assertNotNull(klass)
            assertTrue(model.status.exceptions.isEmpty(), model.status.exceptions.toString())
        }
    }

    /**
     * Adding a class creates a new class in the KerML model; the semantics-internal ownership stack is used.
     */
    @Test
    fun testAddClassInPackage() {
        testSemantics().run {
            val semantics = packageActions()
            semantics.create(Identification("pkg"))
            pushOwner(Resolved(semantics.created!!))
            val actions = ClassActions<Class>(this, ::ClassImplementation)
            actions.create(Identification("klass"))
            actions.addSpecialization(mutableListOf("Base::Anything"))
            model.initialize(1)
            assertTrue(model.status.exceptions.isEmpty(), model.status.exceptions.toString())
            assertNotNull(model.global.resolve<Class>("pkg::klass"))
        }
    }

    /**
     * Adding a feature instance to the KerML model also creates Specialization and Multiplicity.
     */
    @Test
    fun testAddFeature() {
        testSemantics().run {
            val action = FeatureActions<Feature>(this, ::FeatureImplementation, mutableListOf("Base::Anything"))
            action.create(Identification("feature"))
            action.addTyping(mutableListOf())
            action.addMultiplicity(IntegerRange(2, 3))
            model.initialize(1)
            val feature = model.global.resolve<Feature>("feature")
            assertNotNull(feature)
            val typing = feature.getOwnedElementOfType<FeatureTyping>()
            assertNotNull(typing)
            val multiplicity = feature.getOwnedElementOfType<Multiplicity>()
            assertNotNull(multiplicity)
            assertEquals(model.anything, typing.type.ref)
            assertEquals(feature, typing.typedFeature.ref)
            assertEquals(IntegerRange(2, 3) , IntegerRange(multiplicity.typeConstraint.firstOrNull()!!) )
        }
    }


    @Test
    fun testAddMultiplicity() = testSession {
        val semantics = KerML(this).semantics
        val feature = FeatureImplementation(declaredName ="test")
        create(feature, global)
        semantics.addFeatureTyping(feature, "ScalarValues::Integer")
        semantics.addMultiplicity(feature, IntegerRange(2, 3))
        initialize(1)
        val multiplicity = feature.getOwnedElementOfType<Multiplicity>()
        assertNotNull(multiplicity)
        assertEquals("[2 .. 3]", multiplicity.typeConstraint.firstOrNull())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }


}