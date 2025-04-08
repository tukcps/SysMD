package kermltests

import com.github.tukcps.sysmd.compiler.KerML
import io.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.SemanticActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MultiplicitiesTests {

    @Test
    fun testMultiplicitySemanticActions() = testSession {
        loadKerML("""
            package ScalarValues { // Needed for instantiation of Variable & Multiplicity
                datatype Integer; 
                datatype Natural :> Integer;;
            }
        """)
        val context = SemanticActions(this, KerML(this))
        context.initOwners("Global")
        val actions = FeatureActions<Feature>(context, creator = ::FeatureImplementation, mutableListOf("Base::Anything"))
        actions.create(Identification(name="f"))
        actions.addMultiplicity(IntegerRange(1,2))
        assertTrue(actions.created is Feature, "Created feature actions must be an instance of Feature")
        initialize()
        val multiplicity = actions.created!!.multiplicityProperty
        assertTrue(multiplicity!!.variable is Variable, "After initialization a variable must be created for multiplicity")
        assertEquals(IntegerRange(1,2), multiplicity.variable!!.vectorQuantity.value.asIdd().getRange())
    }

    @Test
    fun testMultiplicity() = testSession("Base", "ScalarValues") {
        loadKerML("""
            feature f [1 .. 2];
        """)
        val f = global.resolve<Feature>("f")!!
        val multiplicity = f.multiplicityProperty
        initialize()
        assertTrue(multiplicity!!.variable is Variable, "After initialization a variable must be created for multiplicity")
        assertEquals(IntegerRange(1,2), multiplicity.variable!!.vectorQuantity.value.asIdd().getRange())
    }
}