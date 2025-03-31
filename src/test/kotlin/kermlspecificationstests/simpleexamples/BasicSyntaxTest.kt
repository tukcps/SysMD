package kermlspecificationstests.simpleexamples

import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class BasicSyntaxTest {

    /**
     * Kernel Modeling Language: https://modeldriven.com/wp-content/uploads/2024/06/KERML-Building-Modeling-Languages.pdf
     */
    @Test
    fun testBasicSyntax() =
        testSession("ScalarValues", "Objects", "Occurrences", "Links") {
            loadKerML("""
                package KerML_Base_Example {
                    classifier TorqueValue;
                    classifier Person;
                    
                    classifier Engine {
                        feature engineTorque: TorqueValue[1];
                    }
                    
                    classifier Wheel;
                    
                    classifier Car {
                        feature driver: Person[0..1];
                        feature engine: Engine[1];
                        feature wheels: Wheel[4];
                    }
                classifier Sedan specializes Car;
                
                }
        """.trimIndent()
            )
            assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        }
}