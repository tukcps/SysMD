package kermltests

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.Runlevel
import util.*
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import kotlin.test.*
import kotlin.test.assertEquals

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
        solver.propagate()
        assertNoIssues()

        val bf: Feature? = global.resolve("b::f")?.member()
        val af: Feature? = global.resolve("a::f")?.member()

        assertNotNull(af)
        assertBounds(1L..4L, af.multiplicityRange)
        assertEquals(repo.anything!!, af.generalization.first())

        assertNotNull(bf)
        assertBounds(1L..4L, bf.multiplicityRange)
        assertTrue(repo.realType as Type in bf.generalization)
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
        solver.propagate()
        assertNoIssues()
        assertNotNull(af)
        assertBounds(1L..4L, af.multiplicityRange)
        assertTrue(repo.realType as Type in af.generalization)

        val bf = global.resolve("b::f")?.memberElement as Feature
        assertNotNull(bf)
        assertBounds(2L..3L, bf.multiplicityRange)
        assertTrue(repo.realType as Type in bf.generalization)
    }

    @Test
    fun redefinitionTestBareRedefinition3() = testSession("Attributes", "ISQ") {
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
        """, Runlevel.ALL)
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

        solver.propagate()
        assertNoIssues()
        val bf = global.resolve("b::f")?.memberElement as Feature

        assertNotNull(bf)
        assertBounds(2L .. 3L, bf.ownedElement.filterIsInstance<Multiplicity>().first().variable!!)
        assertTrue(repo.realType as Type in bf.typing.map { it.type })

        val af = global.resolve("a::f")?.memberElement as Type?
        assertNotNull(af)
        assertBounds(1L .. 4L, af.ownedElement.filterIsInstance<Multiplicity>().first().variable!!)
        assertEquals(repo.anything!!, af.generalization.first())
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
        solver.propagate()
        assertNoIssues()
        assertBounds(1L, solver.variable("quantityDimension::quantityPowerFactors::exponent"))
        assertEquals("m", solver.variable("quantityDimension::quantityPowerFactors::unit").vectorQuantity.value.asStrDD().toString())
        assertEquals("m", solver.variable("lengthPF::unit").vectorQuantity.value.asStrDD().toString())
        assertBounds(1L, solver.variable("lengthPF::exponent"))
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
        // val new = solver.variable("ownsOld::ownedOld::a").ast!!.evalUpRec()
        assertEquals("new", solver.variable("ownsOld::ownedOld::a").vectorQuantity.value.asStrDD().toString())
    }


    @Test
    fun nestedRedefinitionTest4()  = testSession("ScalarValues", "ISQ", "Parts") {
        loadSysMLv2("""
            private import ISQ::*;
            part def Precision{
                attribute value: StorageCapacityValue {:>> range = 1..4 [B];}
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
        """, Runlevel.ALL)
solver.propagate()
assertNoIssues()

        // Verify that the redefined values are correctly set (in bits, since 1 B = 8 bits)
        assertBounds(32.0 .. 32.0, solver.variable("Float32::value"), unit = "bit")
        assertBounds(16.0 .. 16.0, solver.variable("Float16::value"), unit = "bit")
        assertBounds(8.0 .. 8.0, solver.variable("Int8::value"), unit = "bit")
    }

    @Test
    fun redefinitionRangeOverrideTest() = testSession("ISQ", "Attributes") {
        loadSysMLv2("""
            attribute def Weight {
                attribute value: ISQ::MassValue {:>> range = 1..100 [kg]; }
            }
            
            attribute specificWeight: Weight {
                attribute :>> value {:>> range = 3..40 [kg]; }
            }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        
        // Verify that the redefined range is used, not the superclass range
        val specificWeightValue = solver.variable("specificWeight::value")
        // The redefined range should be 3..40, not 1..100
        assertBounds(3.0 .. 40.0, specificWeightValue)
    }

    @Test
    fun redefinitionUnitAndValueInInheritedPartTest() = testSession("ISQ", "Parts") {
        loadSysMLv2("""
            private import ISQ::*;
            
            part def TemperatureSensor {
                attribute temperature: ISQ::ThermodynamicTemperatureValue {
                    :>> range = 0..100 [°C]; }
            }
            
            part def NarrowRangeSensor :> TemperatureSensor {
                attribute :>> temperature { :>> range = 20..80 [°C]; }
            }
            
            part def KelvinSensor :> TemperatureSensor {
                attribute :>> temperature { :>> range = 293..353 [K]; }
            }
        """)
        solver.propagate()
        assertNoIssues()
        
        // Verify base part definition
        val baseTemp = solver.variable("TemperatureSensor::temperature")
        assertBounds(0.0 .. 100.0, baseTemp, unit = "°C")
        
        // Verify narrow range sensor redefinition (same unit, refined range)
        val narrowRangeTemp = solver.variable("NarrowRangeSensor::temperature")
        assertBounds(20.0 .. 80.0, narrowRangeTemp, unit = "°C")
        
        // Verify kelvin sensor redefinition (different unit, corresponding range)
        val kelvinTemp = solver.variable("KelvinSensor::temperature")
        assertBounds(293.0 .. 353.0, kelvinTemp, unit = "K")
    }

    @Test
    fun redefinitionInvalidRangeSameUnitTest() = testSession("ISQ", "Parts") {
        loadSysMLv2("""
            private import ISQ::*;
            
            part def TemperatureSensor {
                attribute temperature: ISQ::ThermodynamicTemperatureValue {
                    :>> range = 0..100 [°C];
                }
            }
            
            part def InvalidSensor :> TemperatureSensor {
                attribute :>> temperature {
                    :>> range = -50..150 [°C];
                }
            }
        """, Runlevel.VARIANCE_CHECKED)
        // Should have inconsistency error - range is not a refinement
        assertIssue("must be refinement")
    }

    @Test
    fun redefinitionInvalidRangeDifferentUnitTest() = testSession("ISQ", "Parts", "Attributes") {
        loadSysMLv2("""            
            part def TemperatureSensor {
                attribute temperature: ISQ::ThermodynamicTemperatureValue {
                    :>> range = 0..100 [°C];
                }
            }
            
            part def InvalidKelvinSensor :> TemperatureSensor {
                attribute :>> temperature {
                    :>> range = 200..400 [K];  // Invalid: 200K=-73°C, 400K=127°C, not within 0..100°C
                }
            }
        """, Runlevel.VARIANCE_CHECKED)
        // Should have inconsistency error - converted range is not a refinement
        // Verify it's specifically a range refinement error
        assertIssue("must be refinement")
    }
}



