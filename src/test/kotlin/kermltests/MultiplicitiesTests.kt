package kermltests

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.SemanticActionsImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MultiplicitiesTests {

    @Test
    fun testMultiplicitySemanticActions() = testSession(loadKerML = false) {
        +"package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; }"
        val context = SemanticActionsImplementation(this, TextualRepresentationImplementation())
        val actions = FeatureActions(
            context,
            identification = Identification(name="f"),
            multiplicity = IntegerRange(1,2)
        )
        actions.create()
        assertTrue(actions.created is Feature, "Created feature actions must be an instance of Feature")
        initialize()
        val multiplicity = actions.created!!.multiplicityProperty
        assertTrue(multiplicity!!.variable is Variable, "After initialization a variable must be created for multiplicity")
        assertEquals(IntegerRange(1,2), multiplicity.variable!!.vectorQuantity.value.asIdd().getRange())
    }

    @Test
    fun testMultiplicity() = testSession(loadKerML = false) {
        +"""
            package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; }
            feature f [1 .. 2];
        """.trimIndent()
        val f = global.resolve<Feature>("f")!!
        val multiplicity = f.multiplicityProperty
        initialize()
        assertTrue(multiplicity!!.variable is Variable, "After initialization a variable must be created for multiplicity")
        assertEquals(IntegerRange(1,2), multiplicity.variable!!.vectorQuantity.value.asIdd().getRange())
    }
}