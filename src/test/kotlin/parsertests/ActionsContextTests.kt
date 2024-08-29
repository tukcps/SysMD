package parsertests

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.compiler.parser.kerml.ClassifierDeclarationInfo
import com.github.tukcps.sysmd.compiler.semantics.kerml.ClassActions
import com.github.tukcps.sysmd.compiler.semantics.SemanticActions
import com.github.tukcps.sysmd.services.session.SessionImplementation
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * Tests of the semantic actions.
 */
class ActionsContextTests {


    /** Helper that sets up a valid semantics object */
    private fun testSemantics(): SemanticActions {
        val model = SessionImplementation(loadKerML = false)
        model.loadSysMD("""
            package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; }
        """.trimIndent())
        val textualRepresentation = model.create(TextualRepresentationImplementation(language = "SysMD", body = ""), model.global)
        return KerML(model = model, textualRepresentation = textualRepresentation).semantics
    }


    /**
     * Adding a class creates a new class in the KerML model; the explicit ownership name is used.
     */
    @Test
    fun testAddClass1() = testSemantics().run {
        ClassActions(this, "Global", ClassifierDeclarationInfo(identification = Identification("klass"))).create()
        model.initialize()
        // assertEquals(0, status.errors.size, status.errors.toString())
        assertNotNull(model.global.resolve<Class>("klass"))
    }

    /**
     * Adding a class creates a new class in the KerML model; the semantics-internal ownership stack is used.
     */
    @Test
    fun testAddClassInPackage() = testSemantics().run {
        val pkg = packageActions()?.also { it.identification=Identification("pkg") }?.create()
        pushOwner(Resolved(null, pkg, null))
        ClassActions(this, "pkg", ClassifierDeclarationInfo(identification = Identification("klass"), superclassingPart = mutableListOf("Base::Anything"))).create()
        model.initialize()
        assertTrue(model.status.exceptions.isEmpty(), model.status.exceptions.toString())
        assertNotNull(model.global.resolve<Class>("pkg::klass"))
    }

    /**
     * Adding a feature instance to the KerML model also creates Specialization and Multiplicity.
     */
    @Test
    fun testAddFeatureWithMultiplicity() = testSemantics().run {
        val feature = FeatureImplementation(declaredName="test")
        addFeature(model.global, feature, "Base::Anything", IntegerRange(2,4))
        model.initialize()
        val test = model.global.resolve<Feature>("test")
        assertNotNull(test)
        val typing = test?.getOwnedElementOfType<FeatureTyping>()!!
        assertEquals(model.any, typing.type.ref)
        assertEquals(test, typing.typedFeature.ref)
        assertEquals(IntegerRange(2, 4) , feature.multiplicity )
    }


    /**
     * Adding a feature instance to the KerML model also creates Specialization and Multiplicity.
     * Multiplicity is added, but may only be used if ScalarValues is loaded.
     */
    @Test
    fun testAddFeatureWithoutMultiplicity() = testSemantics().run {
        val feature = FeatureImplementation(declaredName="test")
        addFeature(model.global, feature, "Base::Anything", IntegerRange(1,1))
        model.initialize()
        val test = model.global.resolve<Feature>("test")
        assertNotNull(test)
        val typing = test?.getOwnedElementOfType<FeatureTyping>()!!
        assertEquals(model.any, typing.type.ref)
        assertEquals(test, typing.typedFeature.ref)
    }


    @Test
    fun testAddMultiplicity() = testSession {
        val semantics = KerML(this, TextualRepresentationImplementation(language = "SysMD", body = "")).semantics
        val feature = FeatureImplementation(declaredName ="test")
        semantics.model.create(feature, global)
        semantics.addFeatureTyping(feature, "ScalarValues::Integer")
        val multiplicity = semantics.addMultiplicity(feature, IntegerRange(2, 3))
        initialize()
        assertEquals(IntegerRange(2, 3) , multiplicity.variable!!.intSpecs.first())
        // assertEquals(IntegerRange(2, 3), feature.multiplicityExpression?.intSpecs?.first())
    }

    /*
    @Test
    fun testAddExpression() = testSession {
        val semantics = SysMdSemantics(this, textualRepresentation = TextualRepresentationImplementation(language = "SysMD", body = ""))
        val expression = ExpressionImplementation(name="expr")
        semantics.addExpression(Identity(global), expression, Identity(any))
        initialize()

        val expr = global.resolveName<Expression>("expr")
        assertNotNull(expr)
        assertEquals(IntegerRange(1,1), expr?.multiplicity)
        assertNull(expr?.expression)
        val typing = expr?.getOwnedElementOfType<FeatureTyping>()!!
        assertEquals(any, typing.type.ref)
        assertEquals(expr, typing.typedFeature.ref)
    } */
}