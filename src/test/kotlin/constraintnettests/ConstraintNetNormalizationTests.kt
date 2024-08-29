package test.constraintnettests

import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.cspsolver.normalizer.CNNormalizer
import com.github.tukcps.sysmd.cspsolver.normalizer.NormalizedProperties
import com.github.tukcps.sysmd.cspsolver.normalizer.SimpleProperty
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
                if (model.builder.conds.indexes.containsKey(prop.name)) {
                    println(prop.name + " has index " + model.builder.conds.indexes[prop.name])
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
    fun booleanNormalizationWithoutPropertiesToBeNormalized() = testSession("ScalarValues") {
        +"Value x: ScalarValues::Boolean; Value y: ScalarValues::Boolean; Value z: ScalarValues::Boolean = x and y."
        //ParserSysMD {
        //semantics.defVar("x", model.builder.variable("x"), "")
        //semantics.defVar("y", model.builder.variable("y"), "")
        //+"Value z: ScalarValues::Boolean  = x and y"

        // val z = global.resolveName<ValueFeature>("z")!!.ast!!.solve(1)
        //this.run {
        //    CspSolver(this). solve("z", 1)
        //}

        val normalizedProperties = normalizer.normalizeBooleanConstraints(this)

        // printDebugOutputs(this, normalizedProperties)

        assertTrue(normalizedProperties.properties.size == 3)
    }

    @Test
    fun booleanNormalizationTwoPropertiesToBeNormalized() = testSession("ScalarValues") {
        loadSysMD("""
                Value x: ScalarValues::Boolean;
                Value y: ScalarValues::Boolean;
                Value z1: ScalarValues::Boolean = x and y;
                Value z2: ScalarValues::Boolean = x or y.
         """)
        val normalizedProperties = normalizer.normalizeBooleanConstraints(this)
        // printDebugOutputs(this, normalizedProperties)
        assert(normalizedProperties.properties.size == 3)
    }

    @Test
    fun booleanNormalizationThreePropertiesToBeNormalized() = testSession("ScalarValues") {
        loadSysMD("""
                Value x: ScalarValues::Boolean;
                Value y: ScalarValues::Boolean;
                Value z: ScalarValues::Boolean;
                Value z1: ScalarValues::Boolean  = x and y and z;
                Value z2: ScalarValues::Boolean  = x or y or z;
                Value z3: ScalarValues::Boolean  = x and y or z; 
        """.trimIndent())
        val normalizedProperties = normalizer.normalizeBooleanConstraints(this)
        // printDebugOutputs(this, normalizedProperties)
        assertEquals(4, normalizedProperties.properties.size)
    }

    @Test
    fun booleanNormalizationTwoSetsOfPropertiesToBeNormalized() {
        testSession("ScalarValues") {
            loadSysMD("""
                Value x: ScalarValues::Boolean; 
                Value y: ScalarValues::Boolean; 
                Value z: ScalarValues::Boolean; 
                Value a1: ScalarValues::Boolean  = x and y.
                Value a2: ScalarValues::Boolean  = x or y.
                Value b1: ScalarValues::Boolean  = y or z.
                Value b2: ScalarValues::Boolean  = y and z.
            """.trimIndent())
            val normalizedProperties = normalizer.normalizeBooleanConstraints(this)

            // printDebugOutputs(this, normalizedProperties)

            assert(normalizedProperties.properties.size == 5)
        }
    }

    @Test
    fun booleanNormalizationNegationTest() {
        testSession("ScalarValues") {
            +"Value x: ScalarValues::Boolean"
            +"Value y: ScalarValues::Boolean"
            +"Value a1: ScalarValues::Boolean(false)  = not (x and y)"
            +"Value a2: ScalarValues::Boolean(false)  = not (x or y)"

            val normalizedProperties = normalizer.normalizeBooleanConstraints(this)

            // printDebugOutputs(this, normalizedProperties)

            assertEquals(3, normalizedProperties.properties.size)
        }
    }

    @Test
    fun booleanNormalizationTwoSetsOfPropertiesToBeNormalizedAndNegated() {
        testSession("ScalarValues") {
            +"Value x: ScalarValues::Boolean"
            +"Value y: ScalarValues::Boolean"
            +"Value z: ScalarValues::Boolean"
            +"Value a1: ScalarValues::Boolean(false)  = not (x and y)"
            +"Value a2: ScalarValues::Boolean(false)  = not (x or y)"
            +"Value b1: ScalarValues::Boolean(false)  = not (y or z)"
            +"Value b2: ScalarValues::Boolean(false)  = not (y and z)"


            val normalizedProperties = normalizer.normalizeBooleanConstraints(this)

            // printDebugOutputs(this, normalizedProperties)

            assert(normalizedProperties.properties.size == 5)
        }
    }

}
