package examples

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TutorialExamples {
    @Test
    fun specializationExample() = testSession {
        loadSysMD(catchExceptions = false, input = """
                package p {
                    class c1 {
                       feature p: ScalarValues::Real(1 .. 2);
                    }
                    class c2 :> c1 {
                       feature p: ScalarValues::Real(1.5 .. 1.8);
                    }
                }
            """.trimIndent())
        val pc2p = global.resolve<Feature>("p::c2::p")
        val p = pc2p?.resolve<Feature>("p")    // Was an issue: p search inside p does not resolve to p.
        assertEquals(p, pc2p)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }


    @Test
    fun reasonExample() = testSession {
        loadSysMD(catchExceptions = false, input = """
               package Reason {
                   class General {
                       attribute p: ScalarValues::Real = bySubclasses(p);
                   }
                   class Variant1 isA General {
                       attribute p: ScalarValues::Real = 2.0;
                   }

                   class Variant2 isA General {
                       attribute p: ScalarValues::Real = 3.0; 
                   }
               }
            """.trimIndent()
        )
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        val p = global.resolveVar("Reason::General::p")!!
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 3.0, 0.000001)
        assertEquals(p.vectorQuantity.getMinAsDouble(), 2.0, 0.000001)
    }


    @Test
    fun deCompositionExample() = testSession {
        loadSysMD(catchExceptions = false, input = """
                package Example {
                 	class Engine; 
                    class Wheel;
                    class Car; 
                }

                Example::Engine hasA
                    feature mass: ScalarValues::Real(10..500) [kg]. 
                   
                Example::Wheel hasA
                    feature mass: ScalarValues::Real(20..50)[kg]. 
                   
                Example::Car hasA
                    feature engine: Engine [1..1]; 
                    feature wheels: Wheel [2..6]; 
                    feature totalMass: ScalarValues::Real [kg] = sumOverParts(mass).                     
            """)
        settings.catchExceptions = false
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val mass = global.resolveVar("Example::Car::totalMass")!!
        assertEquals(800.0, mass.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(50.0, mass.vectorQuantity.getMinAsDouble(), 0.00001)
    }

    @Test fun booleanFunctionExample() = testSession {
        loadSysMD(catchExceptions = false, input = """
                attribute a: ScalarValues::Boolean. 
                attribute b: ScalarValues::Boolean. 
                attribute c: ScalarValues::Real(3.0).
                attribute d: ScalarValues::Real(4.0).
                attribute value3: ScalarValues::Boolean = a and b or ((c*d) < 5.0).
            """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test fun requirementNaBExample() = testSession {
        loadSysMD(catchExceptions = false, input = """
            attribute x: ScalarValues::Boolean(true) = 3 < 2;
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        val x = global.resolveVar("x")!!
        assertEquals(builder.NaB, x.vectorQuantity.value)
    }

    @Test fun requirementTrueExample() = testSession {
        loadSysMD(catchExceptions = false, input = """
            attribute x: ScalarValues::Boolean(true) = 1.0 < 2.0 + 1.0;
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        val x = global.resolveVar("x")!!
        assertEquals(builder.True, x.vectorQuantity.value)
    }

    /**
     * First Example from the SysMD Kickstart.
     */
    @Test fun volumeExample() = testSession("SI")  {
        loadSysMD(""" 
            feature partWithVolume {
                feature height:  SI::Length (10 .. 100) [cm];
                feature width:   SI::Length (1 .. 1.1) [m];
                feature length:  SI::Length (1 .. 1.1) [m];
                feature volume:  SI::Volume (1000 .. 2000) [l] = height * width * length; 
            }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

        val volume = global.resolve<Feature>("partWithVolume::volume")!!.variable!!
        val height = global.resolve<Feature>("partWithVolume::height")!!.variable!!
        assertEquals(82.6, height.min(), 0.1)
        assertEquals(1210.0, volume.max(), 1.0)
    }

    @Test
    fun issueExample() = testSession("SI") {
        loadSysMD("""
            // A general class 
            class Wheel {
                feature tire: Tire; 
                feature rim: Rim; 
                feature totalMass: SI::Mass [kg] = sumOverParts(mass). 
            }
            
            class Rim {
                feature mass: SI::Mass(20 .. 30) [kg];
            }
            
            class Tire {
                feature mass: SI::Mass(10 .. 20) [kg];
            }
            
            class SummerTire {
                feature mass: SI::Mass(10 .. 10) [kg];
            }
            
            class WinterTire { 
                feature mass: SI::Mass(20 .. 20) [kg];
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
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val summerWheel = global.resolveVar("SummerWheel::totalMass") !!
        val winterWheel = global.resolveVar("WinterWheel::totalMass") !!
        assertEquals(30.0, summerWheel.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(40.0, summerWheel.vectorQuantity.getMaxAsDouble(), 0.0000001)
        assertEquals(40.0, winterWheel.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(50.0, winterWheel.vectorQuantity.getMaxAsDouble(), 0.0000001)
    }
}