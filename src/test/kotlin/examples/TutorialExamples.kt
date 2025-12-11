package examples


import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals


class TutorialExamples {
    @Test
    fun specializationExample() = testSession("Occurrences") {
        loadKerML(catchExceptions = false, input = """
            package p {
                class c1 {
                   feature p: ScalarValues::Real(1 .. 2);
                }
                class c2 :> c1 {
                   :>> p: ScalarValues::Real(1.5 .. 1.8); 
                }
            }
        """)
        val pc2p = global.resolve("p::c2::p")?.member<Feature>()
        val p = pc2p?.resolve("p")?.member<Feature>()   // Was an issue: p search inside p does not resolve to p.
        assertEquals(p, pc2p)
        assertNoIssues()
    }


    @Test
    fun reasonExample() = testSession("Occurrences") {
        loadKerML("""
           package Reason {
               class General {
                   feature p: ScalarValues::Real = bySpecializations(p);
               }
               class Variant1 :> General {
                   :>> p: ScalarValues::Real = 2.0;
               }

               class Variant2 :> General {
                   :>> p: ScalarValues::Real = 3.0; 
               }
           }
        """)
        assertNoIssues()
        solver.propagate()
        val p = global.resolveVar("Reason::General::p")!!
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 3.0, 0.000001)
        assertEquals(p.vectorQuantity.getMinAsDouble(), 2.0, 0.000001)
    }


    @Test
    fun deCompositionExample() = testSession("Occurrences", "ISQ", "Ranges") {
        loadKerML(catchExceptions = false, input = """
            package Example {
                class Engine { 
                    feature mass: ISQ::MassValue(10..500);  
                }

                class Wheel {
                    feature mass: ISQ::MassValue(20..50);  
                }

                class Car { 
                    feature engine: Engine [1..1];
                    feature wheels: Wheel [2..6]; 
                    feature totalMass: ISQ::MassValue = sumOverParts(mass); 
                }
            }
        """)
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
        val mass = global.resolveVar("Example::Car::totalMass")!!
        assertEquals(800.0, mass.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(50.0, mass.vectorQuantity.getMinAsDouble(), 0.00001)
    }


    @Test fun requirementTrueExample() = testSession("Attributes") {
        loadSysMLv2("""
            attribute x: ScalarValues::Boolean(true) = 1.0 < 2.0 + 1.0;
        """)
        assertNoIssues()
        solver.propagate()
        val x = global.resolveVar("x")!!
        assertEquals(builder.True, x.vectorQuantity.value)
    }

    /**
     * First Example from the SysMD Kickstart.
     */
    @Test fun volumeExample() = testSession("ISQ", "Ranges")  {
        loadKerML(""" 
            feature partWithVolume {
                feature height:  ISQ::LengthValue {:>> unit = "cm"; :>> range = "10 .. 100";}
                feature width:   ISQ::LengthValue{:>> range = "1 .. 1.1";}
                feature length:  ISQ::LengthValue {:>> range = "1 .. 1.1";}
                feature volume:  ISQ::VolumeValue = height * width * length {:>> unit = "l"; :>> range = "1000 .. 2000";}
            }
        """)

        solver.propagate()
        assertNoIssues()

        val volume = global.resolveVar("partWithVolume::volume")!!
        val height = global.resolveVar("partWithVolume::height")!!
        assertEquals(82.6, height.min(), 0.1)
        assertEquals(1210.0, volume.max(), 1.0)
    }

    @Test
    fun issueExample() = testSession("ISQ", "Occurrences", "Ranges") {
        loadKerML("""
            // A general class 
            class Wheel {
                feature tire: Tire; 
                feature rim: Rim; 
                feature totalMass: ISQ::MassValue = sumOverParts(mass);
            }
            
            class Rim {
                feature mass: ISQ::MassValue {:>> range = "20 .. 30";}
            }
            
            class Tire {
                feature mass: ISQ::MassValue {:>> range = "10 .. 20";}
            }
            
            class SummerTire {
                feature mass: ISQ::MassValue {:>> range = "10 .. 10";}
            }
            
            class WinterTire { 
                feature mass: ISQ::MassValue {:>> range = "20 .. 20";}
            }

            // We calculate the sum inside the specific elements
            class SummerWheel :> Wheel {
                feature tire: SummerTire;
            }
            
            class WinterWheel :> Wheel {
                feature tire: WinterTire;
            }
        """)
        solver.propagate()
        assertNoIssues()
        val summerWheel = global.resolveVar("SummerWheel::totalMass") !!
        val winterWheel = global.resolveVar("WinterWheel::totalMass") !!
        assertEquals(30.0, summerWheel.min(), 0.0000001)
        assertEquals(40.0, summerWheel.max(), 0.0000001)
        assertEquals(40.0, winterWheel.min(), 0.0000001)
        assertEquals(50.0, winterWheel.max(), 0.0000001)
    }
}