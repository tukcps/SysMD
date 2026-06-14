package constraintnettests

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.cspsolver.normalizer.CNNormalizer
import com.github.tukcps.sysmd.cspsolver.normalizer.NormalizedProperties
import com.github.tukcps.sysmd.cspsolver.normalizer.SimpleProperty
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.values.XBool
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the CNNormalizer, which normalizes a constraint net.
 */
class ConstraintNetNormalizationTests {

    private val normalizer : CNNormalizer = CNNormalizer()

    private var printOriginalProperties : Boolean = true//false//true
    private var printOriginalConditions : Boolean = true//false
    private var printIndexedVariables : Boolean = true//false
    private var printNormalizedProperties : Boolean = true//false//true

    /**
     * Print the debug outputs, which can individually be activated/deactivated
     * by [printOriginalProperties], [printOriginalConditions], [printIndexedVariables], [printNormalizedProperties]
     */
    @Suppress("unused")
    private fun printDebugOutputs(model : Session, normalizedProperties: NormalizedProperties) {
        if(printOriginalProperties) {
            val originalProperties : List<Variable> = model.get().filterIsInstance<Variable>()
            println("Original set of properties:")
            for(property in originalProperties) {
                println(property.toString())
            }
        }
        if(printOriginalConditions) {
            val originalConditions = model.builder.conds.conditions
            println("Original set of conditions:")
            originalConditions.toString()
            for(cond in originalConditions) {
                var condName = ""
                for(index in model.builder.conds.indexes) {
                    if(index.value == cond.key) {
                        condName = index.key
                        break
                    }
                }
                println("condition $condName: $cond")
            }
        }
        if(printIndexedVariables) {
            val originalProperties : List<Variable> = model.get().filterIsInstance<Variable>()
            println("indexed variables:")
            for(prop in originalProperties) {
                if (model.builder.conds.indexes.containsKey(prop.path)) {
                    println(prop.path + " has index " + model.builder.conds.indexes[prop.path])
                }
            }
        }
        if(printNormalizedProperties) {
            val normalizedProps : MutableList<SimpleProperty<XBool>> = normalizedProperties.properties
            println("Set of properties after normalization:")
            for(property in normalizedProps) {
                println(property.toString())
            }
        }
    }

    @Test
    fun booleanNormalizationWithoutPropertiesToBeNormalized() = testSession {
        loadKerML("""package ScalarValues { datatype Boolean :> Base::DataValue; };
                package Base { type DataValue :> Base::Anything; }
                feature x: ScalarValues::Boolean; 
                feature y: ScalarValues::Boolean; 
                feature z: ScalarValues::Boolean = x and y; 
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val normalizedProperties = normalizer.normalizeBooleanConstraints(this)
        assertEquals(3, normalizedProperties.properties.size)
    }

    @Test
    fun booleanNormalizationTwoPropertiesToBeNormalized() = testSession("ScalarValues") {
        loadKerML("""
                feature x: ScalarValues::Boolean;
                feature y: ScalarValues::Boolean;
                feature z1: ScalarValues::Boolean = x and y;
                feature z2: ScalarValues::Boolean = x or y; 
         """, Runlevel.VARIABLES)
        assertNoIssues()
        val normalizedProperties = normalizer.normalizeBooleanConstraints(this)
        // printDebugOutputs(this, normalizedProperties)
        assertEquals(3, normalizedProperties.properties.size)
    }

    @Test
    fun booleanNormalizationThreePropertiesToBeNormalized() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Boolean;
            feature y: ScalarValues::Boolean;
            feature z: ScalarValues::Boolean;
            feature z1: ScalarValues::Boolean  = x and y and z;
            feature z2: ScalarValues::Boolean  = x or y or z;
            feature z3: ScalarValues::Boolean  = x and y or z; 
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val normalizedProperties = normalizer.normalizeBooleanConstraints(this)
        assertEquals(4, normalizedProperties.properties.size)
    }

    @Test
    fun booleanNormalizationTwoSetsOfPropertiesToBeNormalized() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Boolean; 
            feature y: ScalarValues::Boolean; 
            feature z: ScalarValues::Boolean; 
            feature a1: ScalarValues::Boolean  = x and y; 
            feature a2: ScalarValues::Boolean  = x or y; 
            feature b1: ScalarValues::Boolean  = y or z; 
            feature b2: ScalarValues::Boolean  = y and z; 
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val normalizedProperties = normalizer.normalizeBooleanConstraints(this)
        assertEquals(5, normalizedProperties.properties.size)
    }

    @Test
    fun booleanNormalizationNegationTest() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Boolean; 
            feature y: ScalarValues::Boolean; 
            inv a1 false { not (x and y) }
            inv a2 false { not (x or y) } 
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val normalizedProperties = normalizer.normalizeBooleanConstraints(this)
        assertEquals(3, normalizedProperties.properties.size)
    }

    @Test
    fun booleanNormalizationTwoSetsOfPropertiesToBeNormalizedAndNegated() = testSession("ScalarValues") {
        loadKerML("""
            package ScalarValues { datatype Boolean; }
            feature x: ScalarValues::Boolean;
            feature y: ScalarValues::Boolean;
            feature z: ScalarValues::Boolean;
            inv a1 false { not (x and y) };
            inv a2 false { not (x or y) };
            inv b1 false { not (y or z) };
            inv b2 false { not (y and z) };
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val normalizedProperties = normalizer.normalizeBooleanConstraints(this)
        assertEquals(5, normalizedProperties.properties.size)
    }
}
