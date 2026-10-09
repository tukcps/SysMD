package examples


import util.variable
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals


class TutorialExamples {
    @Test
    fun specializationExample() = testSession("Occurrences") {
        loadKerML("""
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
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val p = solver.variable("Reason::General::p")
        assertBounds(2.0 .. 3.0, p)
    }


    @Test
    fun decompositionExample() = testSession("Occurrences", "ISQ") {
        loadKerML("""
            package Example {
                class Engine { 
                    feature mass: ISQ::MassValue(10..500 [kg]);  
                }

                class Wheel {
                    feature mass: ISQ::MassValue(20..50 [kg]);  
                }

                class Car { 
                    feature engine: Engine [1..1];
                    feature wheels: Wheel [2..6]; 
                    feature totalMass: ISQ::MassValue = sumOverParts(mass); 
                }
            }
        """, Runlevel.ALL)
        assertNoIssues()
        val mass = solver.variable("Example::Car::totalMass")
        assertBounds(50.0..800.0, mass)
    }


    @Test fun requirementTrueExample() = testSession("Attributes") {
        loadSysMLv2("""
            attribute x: ScalarValues::Boolean(true) = 1.0 < 2.0 + 1.0;
        """, Runlevel.ALL)
        assertNoIssues()
        val x = solver.variable("x")
        assertEquals(builder.Bool.True, x.vectorQuantity.value)
    }

    /**
     * First Example from the SysMD Kickstart.
     */
    @Test fun volumeExample() = testSession("ISQ")  {
        loadKerML(""" 
            feature partWithVolume {
                feature height:  ISQ::LengthValue {:>> range = 10 .. 100 [cm];}
                feature width:   ISQ::LengthValue{:>> range = 1 .. 1.1 [m];}
                feature length:  ISQ::LengthValue {:>> range = 1 .. 1.1 [m];}
                feature volume:  ISQ::VolumeValue = height * width * length { :>> range = 1000 .. 2000 [l];}
            }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()

        val volume = solver.variable("partWithVolume::volume")
        val height = solver.variable("partWithVolume::height")
        assertBounds(82.6446280991735..100.0, height, unit = "cm")
        assertBounds(999.9999999999999..1210.000000000001, volume, unit = "l")
    }

    @Test
    fun issueExample() = testSession("ISQ", "Occurrences") {
        loadKerML("""
            // A general class 
            class Wheel {
                feature tire: Tire; 
                feature rim: Rim; 
                feature totalMass: ISQ::MassValue = sumOverParts(mass);
            }
            
            class Rim {
                feature mass: ISQ::MassValue {:>> range = 20 .. 30 [kg];}
            }
            
            class Tire {
                feature mass: ISQ::MassValue {:>> range = 10 .. 20 [kg];}
            }
            
            class SummerTire {
                feature mass: ISQ::MassValue {:>> range = 10 .. 10 [kg];}
            }
            
            class WinterTire { 
                feature mass: ISQ::MassValue {:>> range = 20 .. 20 [kg];}
            }

            // We calculate the sum inside the specific elements
            class SummerWheel :> Wheel {
                feature tire: SummerTire;
            }
            
            class WinterWheel :> Wheel {
                feature tire: WinterTire;
            }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val summerWheel = solver.getVariable("SummerWheel::totalMass") !!
        val winterWheel = solver.getVariable("WinterWheel::totalMass") !!
        assertBounds(30.0 .. 40.0, summerWheel)
        assertBounds(40.0 .. 50.0, winterWheel)
    }
}