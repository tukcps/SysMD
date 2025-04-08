package examples


import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import util.testSession

class TutorialExamples {
    @Test
    fun specializationExample() = testSession("Occurrences") {
        loadKerML(catchExceptions = false, input = """
            package p {
                class c1 {
                   feature p: ScalarValues::Real {:>> range = "1 .. 2";}
                }
                class c2 :> c1 {
                   feature p: ScalarValues::Real {:>> range = "1.5 .. 1.8";}
                }
            }
        """.trimIndent())
        val pc2p = global.resolve<Feature>("p::c2::p")
        val p = pc2p?.resolve<Feature>("p")    // Was an issue: p search inside p does not resolve to p.
        assertEquals(p, pc2p)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }


    @Test
    fun reasonExample() = testSession("Occurrences") {
        loadKerML("""
           package Reason {
               class General {
                   feature p: ScalarValues::Real = bySpecializations(p);
               }
               class Variant1 isA General {
                   feature p: ScalarValues::Real = 2.0;
               }

               class Variant2 isA General {
                   feature p: ScalarValues::Real = 3.0; 
               }
           }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        propagate()
        val p = global.resolveVar("Reason::General::p")!!
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 3.0, 0.000001)
        assertEquals(p.vectorQuantity.getMinAsDouble(), 2.0, 0.000001)
    }


    @Test
    fun deCompositionExample() = testSession("Occurrences", "SI") {
        loadKerML(catchExceptions = false, input = """
            package Example {
                class Engine { 
                    feature mass: SI::Mass {:>> range = "10..500";} 
                }

                class Wheel {
                    feature mass: SI::Mass {:>> range = "20..50";} 
                }

                class Car { 
                    feature engine: Engine [1..1];
                    feature wheels: Wheel [2..6]; 
                    feature totalMass: SI::Mass = sumOverParts(mass); 
                }
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val mass = global.resolveVar("Example::Car::totalMass")!!
        assertEquals(800.0, mass.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(50.0, mass.vectorQuantity.getMinAsDouble(), 0.00001)
    }


    @Test fun requirementTrueExample() = testSession("Attributes") {
        loadSysMLv2("""
            attribute x: ScalarValues::Boolean(true) = 1.0 < 2.0 + 1.0;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        propagate()
        val x = global.resolveVar("x")!!
        assertEquals(builder.True, x.vectorQuantity.value)
    }

    /**
     * First Example from the SysMD Kickstart.
     */
    @Test fun volumeExample() = testSession("SI")  {
        loadKerML(""" 
            feature partWithVolume {
                feature height:  SI::Length {:>> unit = "cm"; :>> range = "10 .. 100";}
                feature width:   SI::Length {:>> range = "1 .. 1.1";}
                feature length:  SI::Length {:>> range = "1 .. 1.1";}
                feature volume:  SI::Volume = height * width * length {:>> unit = "l"; :>> range = "1000 .. 2000";}
            }
        """)

        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val volume = global.resolve<Feature>("partWithVolume::volume")!!.variable!!
        val height = global.resolve<Feature>("partWithVolume::height")!!.variable!!
        assertEquals(82.6, height.min(), 0.1)
        assertEquals(1210.0, volume.max(), 1.0)
    }

    @Test
    fun issueExample() = testSession("SI", "Occurrences") {
        loadKerML("""
            // A general class 
            class Wheel {
                feature tire: Tire; 
                feature rim: Rim; 
                feature totalMass: SI::Mass = sumOverParts(mass);
            }
            
            class Rim {
                feature mass: SI::Mass {:>> range = "20 .. 30";}
            }
            
            class Tire {
                feature mass: SI::Mass {:>> range = "10 .. 20";}
            }
            
            class SummerTire {
                feature mass: SI::Mass {:>> range = "10 .. 10";}
            }
            
            class WinterTire { 
                feature mass: SI::Mass {:>> range = "20 .. 20";}
            }

            // We calculate the sum inside the specific elements
            class SummerWheel isA Wheel {
                feature tire: SummerTire;
            }
            
            class WinterWheel isA Wheel {
                feature tire: WinterTire;
            }
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val summerWheel = global.resolveVar("SummerWheel::totalMass") !!
        val winterWheel = global.resolveVar("WinterWheel::totalMass") !!
        assertEquals(30.0, summerWheel.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(40.0, summerWheel.vectorQuantity.getMaxAsDouble(), 0.0000001)
        assertEquals(40.0, winterWheel.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(50.0, winterWheel.vectorQuantity.getMaxAsDouble(), 0.0000001)
    }
}