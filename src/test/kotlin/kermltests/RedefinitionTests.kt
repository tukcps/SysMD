package kermltests

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertIssue
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RedefinitionTests {

    /**
     * A redefined feature should add a multiplicity and also refine its value.
     */
    @Test
    fun redefinitionTestBareRedefinition1() = testSession("ScalarValues") {
        loadKerML("""
            type a :> Base::Anything {
                 feature f[1..4]; 
            } 
                       
            type b :> a {
                :>> f: ScalarValues::Real; 
            }
        """, Runlevel.MODEL)
        assertNoIssues()

        val bf: Feature? = global.resolve("b::f")?.member()
        val af: Feature? = global.resolve("a::f")?.member()

        assertNotNull(af)
        assertEquals(1L, af.multiplicityRange.min)
        assertEquals(4L, af.multiplicityRange.max)
        assertEquals(anything, af.generalization.first())

        assertNotNull(bf)
        assertEquals(1L, bf.multiplicityRange.min)
        assertEquals(4L, bf.multiplicityRange.max)
        assertTrue(repo.realType in bf.generalization)
        assertTrue(af in bf.generalization)
    }


    @Test
    fun redefinitionTestBareRedefinition2() = testSession("ScalarValues") {
        loadKerML("""
            type a :> Base::Anything {
                 feature f: ScalarValues::Real[1 .. 4]; 
            } 
                       
            type b :> a {
                :>> f [2..3];   // Must be of type Real 
            }
        """, Runlevel.MODEL)
        val af: Feature? = global.resolve("a::f")?.member()
        assertNotNull(af)
        assertEquals(1L, af.multiplicityRange.min)
        assertEquals(4L, af.multiplicityRange.max)
        assertTrue(repo.realType in af.generalization)

        assertNoIssues()
        val bf = global.resolve("b::f")?.memberElement as Feature
        assertNotNull(bf)
        assertEquals(2L, bf.multiplicityRange.min)
        assertEquals(3L, bf.multiplicityRange.max)
        assertTrue(repo.realType in bf.generalization)
    }

    @Test
    fun redefinitionTestBareRedefinition3() = testSession("ScalarValues", "ISQ") {
        loadSysMLv2("""
            attribute def Position {
                attribute x: ISQ::LengthValue [m]; 
                attribute y: ISQ::LengthValue [m]; 
                attribute z: ISQ::LengthValue [m];     
            }
            
            attribute p: Position { 
              redefines x = 1.0 [m];
              redefines y = 2.0 [m]; 
              redefines z = 1.5 [m]; 
            }
        """)
        solver.propagate()
        assertNoIssues()
    }

    /**
     * Basic test for re-definition.
     * - redefinition changes the multiplicity.
     * - redefinition changes the type
     * - The redefined feature is not changed.
     */
    @Test
    fun redefinitionTest1() = testSession("ScalarValues") {
        loadKerML("""
            type a :> Base::Anything {
                 feature f[1..4]; 
            } 
                       
            type b :> a {
                :>> f: ScalarValues::Real[2..3]; 
            }
        """, Runlevel.VARIABLES)

        assertNoIssues()
        val bf = global.resolve("b::f")?.memberElement as Feature

        assertNotNull(bf)
        assertEquals(2L, bf.ownedElement.filterIsInstance<Multiplicity>().first().variable!!.min())
        assertEquals(3L, bf.ownedElement.filterIsInstance<Multiplicity>().first().variable!!.max())
        assertTrue(repo.realType in bf.typing.map { it.type })

        val af = global.resolve("a::f")?.memberElement as Type?
        assertNotNull(af)
        assertEquals(1L, af.ownedElement.filterIsInstance<Multiplicity>().first().variable!!.min())
        assertEquals(4L, af.ownedElement.filterIsInstance<Multiplicity>().first().variable!!.max())
        assertEquals(anything, af.generalization.first())
    }


    @Test
    fun redefinitionTest()  = testSession("Occurrences") {
        loadKerML("""
            class QuantityPowerFactor {
                feature unit: ScalarValues::String;
                feature exponent: ScalarValues::Integer(-10..10);
            }
            class QuantityDimension {
                feature quantityPowerFactors: QuantityPowerFactor;
            }
            feature lengthPF: QuantityPowerFactor { 
                :>> unit = "m";  
                :>> exponent = 1;
            }
            feature massPF: QuantityPowerFactor { 
                :>> unit = "kg";  
                :>> exponent = 1;  
            }
            feature quantityDimension: QuantityDimension { 
                :>> quantityPowerFactors = lengthPF; 
            }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(1L, global.resolveVar("quantityDimension::quantityPowerFactors::exponent")!!.max() )
        assertEquals("m", global.resolveVar("quantityDimension::quantityPowerFactors::unit")!!.vectorQuantity.value.asStrDD().toString())
        assertEquals("m", global.resolveVar("lengthPF::unit")!!.vectorQuantity.value.asStrDD().toString())
        assertEquals(1L, global.resolveVar("lengthPF::exponent")!!.min())
    }

    // The types are not set as expected in addInheritedFeatures (the function should be right but not correctly called for the elements),
    // so that in "initialize" the values in the last row can be set (like in the last test).
    @Test
    fun nestedRedefinitionTest3()  = testSession("ScalarValues") {
        loadKerML("""
            datatype Old {
                feature a: ScalarValues::String default "old";
            }
            datatype OwnsOld {
                feature ownedOld: Old;
            }
            feature ownsOld: OwnsOld { :>> ownedOld = redefinedOld; } 
            feature redefinedOld: Old { :>> a = "new"; }
        """)
        assertNoIssues()
        solver.propagate()
        // val ownsOld = global.resolve<Feature>("ownsOld")
        // val redefinedOld = global.resolve<Feature>("redefinedOld")
        // val new = global.resolveVar("ownsOld::ownedOld::a")!!.ast!!.evalUpRec()
        assertEquals("new", global.resolveVar("ownsOld::ownedOld::a")!!.vectorQuantity.value.asStrDD().toString())
    }


    @Test
    fun nestedRedefinitionTest4()  = testSession("ScalarValues", "ISQ", "Parts") {
        loadSysMLv2("""
            private import ISQ::*;
            part def Precision{
                attribute value: StorageCapacityValue {:>> range = "1..4"; :>> unit = "B";}
            }
            part def Float32 :> Precision{
                attribute :>> value = 4.0 [B];
            }
            part def Float16 :> Precision{
                attribute :>> value = 2.0 [B];
            }
            part def Int8 :> Precision{
                attribute :>> value = 1.0 [B];
            }
            part def NeuralNetworkLayer {
                part precision: Precision;
            }
            part layer5 : NeuralNetworkLayer {
                :>> precision : Float16;
            }
        """)
        solver.propagate()
        assertNoIssues()

        // Verify that the redefined values are correctly set (in bits, since 1 B = 8 bits)
        assertEquals(32.0, global.resolveVar("Float32::value")!!.vectorQuantity.value.asAadd().min, 0.001)
        assertEquals(32.0, global.resolveVar("Float32::value")!!.vectorQuantity.value.asAadd().max, 0.001)
        assertEquals(16.0, global.resolveVar("Float16::value")!!.vectorQuantity.value.asAadd().min, 0.001)
        assertEquals(16.0, global.resolveVar("Float16::value")!!.vectorQuantity.value.asAadd().max, 0.001)
        assertEquals(8.0, global.resolveVar("Int8::value")!!.vectorQuantity.value.asAadd().min, 0.001)
        assertEquals(8.0, global.resolveVar("Int8::value")!!.vectorQuantity.value.asAadd().max, 0.001)

    }

    @Test
    fun redefinitionRangeOverrideTest() = testSession("ScalarValues", "ISQ") {
        loadSysMLv2("""
            attribute def Weight {
                attribute value: ISQ::MassValue {:>> range = "1..100"; :>> unit = "kg";}
            }
            
            attribute specificWeight: Weight {
                attribute :>> value {:>> range = "3..40"; :>> unit = "kg";}
            }
        """)
        solver.propagate()
        assertNoIssues()
        
        // Verify that the redefined range is used, not the superclass range
        val specificWeightValue = global.resolveVar("specificWeight::value")
        assertNotNull(specificWeightValue)
        // The redefined range should be 3..40, not 1..100
        assertEquals(3.0, specificWeightValue.min(), 0.001)
        assertEquals(40.0, specificWeightValue.max(), 0.001)
    }

    @Test
    fun redefinitionUnitAndValueInInheritedPartTest() = testSession("ScalarValues", "ISQ", "Parts") {
        loadSysMLv2("""
            private import ISQ::*;
            
            part def TemperatureSensor {
                attribute temperature: ISQ::ThermodynamicTemperatureValue {
                    :>> range = "0..100";
                    :>> unit = "°C";
                }
            }
            
            part def NarrowRangeSensor :> TemperatureSensor {
                attribute :>> temperature {
                    :>> range = "20..80";
                    :>> unit = "°C";
                }
            }
            
            part def KelvinSensor :> TemperatureSensor {
                attribute :>> temperature {
                    :>> range = "293..353";
                    :>> unit = "K";
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        
        // Verify base part definition
        val baseTemp = global.resolveVar("TemperatureSensor::temperature")
        assertNotNull(baseTemp)
        assertEquals(0.0, baseTemp.min(), 0.001)
        assertEquals(100.0, baseTemp.max(), 0.001)
        
        // Verify narrow range sensor redefinition (same unit, refined range)
        val narrowRangeTemp = global.resolveVar("NarrowRangeSensor::temperature")
        assertNotNull(narrowRangeTemp)
        assertEquals(20.0, narrowRangeTemp.min(), 0.001)
        assertEquals(80.0, narrowRangeTemp.max(), 0.001)
        
        // Verify kelvin sensor redefinition (different unit, corresponding range)
        val kelvinTemp = global.resolveVar("KelvinSensor::temperature")
        assertNotNull(kelvinTemp)
        assertEquals(293.0, kelvinTemp.min(), 0.001)
        assertEquals(353.0, kelvinTemp.max(), 0.001)
    }

    @Test
    fun redefinitionInvalidRangeSameUnitTest() = testSession("ScalarValues", "ISQ", "Parts") {
        loadSysMLv2("""
            private import ISQ::*;
            
            part def TemperatureSensor {
                attribute temperature: ISQ::ThermodynamicTemperatureValue {
                    :>> range = "0..100";
                    :>> unit = "°C";
                }
            }
            
            part def InvalidSensor :> TemperatureSensor {
                attribute :>> temperature {
                    :>> range = "-50..150";  // Invalid: not a refinement of 0..100
                    :>> unit = "°C";
                }
            }
        """, Runlevel.VARIANCE_CHECKED)
        // Should have inconsistency error - range is not a refinement
        assertIssue("must be refinement")
    }

    @Test
    fun redefinitionInvalidRangeDifferentUnitTest() = testSession("ScalarValues", "ISQ", "Parts") {
        loadSysMLv2("""
            private import ISQ::*;
            
            part def TemperatureSensor {
                attribute temperature: ISQ::ThermodynamicTemperatureValue {
                    :>> range = "0..100";
                    :>> unit = "°C";
                }
            }
            
            part def InvalidKelvinSensor :> TemperatureSensor {
                attribute :>> temperature {
                    :>> range = "200..400";  // Invalid: 200K=-73°C, 400K=127°C, not within 0..100°C
                    :>> unit = "K";
                }
            }
        """, Runlevel.VARIANCE_CHECKED)
        // Should have inconsistency error - converted range is not a refinement
        // Verify it's specifically a range refinement error
        assertIssue("must be refinement")
    }
}



