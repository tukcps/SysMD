package importtests

import util.testSession
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadSysMLv2
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ImportTest {

    private val dir = "src/test/resources/importResultsTestDir/testFile.json"

    /*************************************************************************
     ***************************** Base Tests ********************************
     *******                                                        **********
     *******    These tests try a variety of constellations for     **********
     *******                    possible inputs                     **********
     *************************************************************************/
    @Test
    // This test checks if a proper .json can be loaded and its values intersect with the result of the AstFunction
    // ASTFunction           JSON result   Final result
    // [26.0,35.0] intersect [28.0,32.0] = [28.0,32.0]
    fun baseTest() = testSession("Parts", "SI") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 28.5,
            resultUnit = "dB",
            attrFQN = "test::myAmplifier::gain")

        loadSysMLv2("""
            package test {
                private import ScalarValues::*; 
                private import SI::*; 
                           
                part def Amplifier isA Base::Anything;
            
                part def LNA isA Amplifier {    
                    attribute gain: SI::Quantity = [5.0 .. 20.0] dB {:>> unit = "dB";}
                }
            
                part def Stage2 isA Amplifier { 
                    attribute gain: SI::Quantity = [11.0 .. 20.0] dB {:>> unit = "dB";}
                }
            
                part def Driver isA Amplifier {
                    attribute gain: SI::Quantity = [10.0 .. 30.0] dB {:>> unit = "dB";}
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: SI::Quantity(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),"${Paths.get("").toAbsolutePath()}/src/test/resources/importResultsTestDir/testFile.json"); 
                }
            }
            """.trimIndent()
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(28.5, global.resolveVar("test::myAmplifier::gain")!!.aadd().min,0.00001)
        assertEquals(28.5,global.resolveVar("test::myAmplifier::gain")!!.aadd().max,0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    //In this test, the range of the imported Result is larger than the range of the upQuantity of the ASTFunction
    fun baseTestResultNotInBounds() = testSession("Parts", "SI") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 24.5,
            resultUnit = "dB",
            attrFQN = "test::myAmplifier::gain")

        loadSysMLv2("""
            package test {
                private import ScalarValues::*; 
                private import SI::*; 
                           
                part def Amplifier isA Base::Anything;
            
                part def LNA isA Amplifier {    
                    attribute gain: Quantity = [5.0 .. 20.0] dB {:>> unit = "dB";}
                }
            
                part def Stage2 isA Amplifier { 
                    attribute gain: Quantity = [11.0 .. 20.0] dB {:>> unit = "dB";}
                }
            
                part def Driver isA Amplifier {
                    attribute gain: Quantity = [10.0 .. 30.0] dB {:>> unit = "dB";} 
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: Quantity(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),"${Paths.get("").toAbsolutePath()}/src/test/resources/importResultsTestDir/testFile.json"); 
                }
            }
            """.trimIndent()
        )
        propagate()

        assertTrue(global.resolveVar("test::myAmplifier::gain")!!.aadd().isEmpty())
        assertEquals(1, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /*************************************************************************
     ************************* Robustness Tests ******************************
     *******                                                        **********
     ******* These tests deliberately feed wrong inputs and check   **********
     ******* if the constraintSolver still outputs a valid result    *********
     *************************************************************************/
    @Test
    //This test deliberately uses a non-existing JSON file which causes the function to use a [-Inf,+Inf] range as intersection partner
    //The result should therefore be the actual result of the ASTFunction
    fun wrongFileNameTest() = testSession("Parts", "SI") {
        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))
        loadSysMLv2("""
            package test {
                private import ScalarValues::*; 
                private import SI::*; 
                           
                part def Amplifier :> Base::Anything;
            
                part def LNA :> Amplifier {    
                    attribute gain: Quantity = [5.0 .. 20.0] dB {:>> unit = "dB";}
                }
            
                part def Stage2 :> Amplifier { 
                    attribute gain: Quantity = [11.0 .. 20.0] dB {:>> unit = "dB";}
                }
            
                part def Driver :> Amplifier {
                    attribute gain: Quantity = [10.0 .. 30.0] dB {:>> unit = "dB";}
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: Quantity(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),"wrongFileNameThatDoesntWork.json"); 
                }
            }
        """)
        propagate()
        assertEquals(1, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(26.0, global.resolveVar("test::myAmplifier::gain")!!.aadd().min,0.00001)
        assertEquals(35.0,global.resolveVar("test::myAmplifier::gain")!!.aadd().max,0.00001)
    }

    @Test
    //This test deliberately declares a wrong unit in the JSON file which causes the function to use a [-Inf,+Inf] range as intersection partner
    //The result should therefore be the actual result of the ASTFunction
    fun wrongUnitTest() = testSession("Parts", "SI") {
        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))
        writeJson(
            resultValue = 28.5,
            resultUnit = "WRONG_UNIT",
            attrFQN = "test::myAmplifier::gain")

        loadSysMLv2("""
            package test {
                private import ScalarValues::*; 
                private import SI::*; 
                           
                part def Amplifier;
            
                part def LNA :> Amplifier {    
                    attribute gain: Quantity = oneOf(5.0 .. 20.0 dB) {:>> unit = "dB";} 
                }
            
                part def Stage2 isA Amplifier { 
                    attribute gain: Quantity = oneOf(11.0 .. 20.0 dB) {:>> unit = "dB";}
                }
            
                part def Driver isA Amplifier {
                    attribute gain: Quantity = oneOf(10.0 .. 30.0 dB) {:>> unit = "dB";}
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: Quantity(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),"${Paths.get("").toAbsolutePath()}/src/test/resources/importResultsTestDir/testFile.json"); 
                }
            }
        """)
        propagate()
        assertEquals(26.0, global.resolveVar("test::myAmplifier::gain")!!.aadd().min, 0.00001)
        assertEquals(35.0,global.resolveVar("test::myAmplifier::gain")!!.aadd().max, 0.00001)
        assertEquals(1, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    //This test checks the behavior if an illegal argument is passed as second parameter
    fun wrongSecondArgumentTest() = testSession("Parts", "SI") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 28.5,
            resultUnit = "dB",
            attrFQN = "test::myAmplifier::gain")

        loadSysMLv2("""
            package test {
                private import ScalarValues::*; 
                private import SI::*; 
                           
                part def Amplifier isA Base::Anything;
            
                part def LNA isA Amplifier {    
                    attribute gain: Quantity = [5.0 .. 20.0] dB {:>> unit = "dB";} 
                }
            
                part def Stage2 isA Amplifier { 
                    attribute gain: Quantity = [11.0 .. 20.0] dB {:>> unit = "dB";}
                }
            
                part def Driver isA Amplifier {
                    attribute gain: Quantity = [10.0 .. 30.0] dB {:>> unit = "dB";} 
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: Quantity(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),productOverParts(gain)); 
                }
            }
        """)
        propagate()

        assertEquals(26.0, global.resolveVar("test::myAmplifier::gain")!!.rangeSpecs[0].min,0.00001)
        assertEquals(35.0,global.resolveVar("test::myAmplifier::gain")!!.rangeSpecs[0].max,0.00001)
        assertEquals(1, status.exceptions.size, "Error messages: ${status.exceptions}")
    }





    /*************************************************************************
     ***************************ASTFunction Tests ****************************
     *******                                                        **********
     *******    These tests try a variety of ASTFunctions           **********
     *************************************************************************/
    @Test
    //This test checks if the characterizedResult function can handle an AstMax Node
    //The max function returns [10 … 30] which is intersected with [25 … 26]
    fun maxFunctionTest() = testSession("Parts", "SI") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 28.5,
            resultUnit = "m",
            attrFQN = "gain")

        loadSysMLv2("""       
            attribute a: SI::Length = [5.0 .. 30.0] m;
            attribute b: SI::Length = [10.0 .. 20.0] m;
            attribute gain: ScalarValues::Real = characterizedResult(max(a,b),"${Paths.get("").toAbsolutePath()}/src/test/resources/importResultsTestDir/testFile.json"); 
        """)
        propagate()

        assertEquals(28.5, global.resolveVar("gain")!!.aadd().min,0.00001)
        assertEquals(28.5,global.resolveVar("gain")!!.aadd().max,0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    private fun writeJson(attrFQN: Any, resultUnit: String, resultValue: Double) {
        File(dir).printWriter().use { out ->
            out.print("[{\n"+
                    "\t\"constraintName\": \"xyz\",\n" +
                    "\t\"resultValue\": \"$resultValue\",\n" +
                    "\t\"resultUnit\": \"$resultUnit\",\n" +
                    "\t\"referenceValue\": 0.0,\n" +
                    "\t\"referenceUnit\": \"dB\",\n" +
                    "\t\"successful\": 1,\n" +
                    "\t\"attributeQualifiedName\": \"$attrFQN\"\n}]")
        }
    }
}