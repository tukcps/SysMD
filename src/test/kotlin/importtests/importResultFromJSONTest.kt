package importtests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
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
    fun baseTest() = SessionManager.testSession("Parts") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 28.5,
            resultUnit = "dB",
            attrFQN = "test::myAmplifier::gain")


        loadSysMD(
            """
            package test {
                import ScalarValues::*; 
                import SI::*; 
                           
                part def Amplifier isA Base::Anything;
            
                part def LNA isA Amplifier {    
                    attribute gain: Real [dB] = [5.0 .. 20.0] dB; 
                }
            
                part def Stage2 isA Amplifier { 
                    attribute gain: Real [dB] = [11.0 .. 20.0] dB;
                }
            
                part def Driver isA Amplifier {
                    attribute gain: Real [dB] = [10.0 .. 30.0] dB; 
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: Real(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),"${Paths.get("").toAbsolutePath()}/src/test/resources/importResultsTestDir/testFile.json"); 
                }
            }
            """.trimIndent()
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        Assertions.assertEquals(28.5, global.resolveVar("test::myAmplifier::gain")!!.aadd().min,0.00001)
        Assertions.assertEquals(28.5,global.resolveVar("test::myAmplifier::gain")!!.aadd().max,0.00001)
        Assertions.assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    //In this test, the range of the imported Result is larger than the range of the upQuantity of the ASTFunction
    fun baseTestResultNotInBounds() = SessionManager.testSession("Parts") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 24.5,
            resultUnit = "dB",
            attrFQN = "test::myAmplifier::gain")

        loadSysMD(
            """
            package test {
                import ScalarValues::*; 
                import SI::*; 
                           
                part def Amplifier isA Base::Anything;
            
                part def LNA isA Amplifier {    
                    attribute gain: Real [dB] = [5.0 .. 20.0] dB; 
                }
            
                part def Stage2 isA Amplifier { 
                    attribute gain: Real [dB] = [11.0 .. 20.0] dB;
                }
            
                part def Driver isA Amplifier {
                    attribute gain: Real [dB] = [10.0 .. 30.0] dB; 
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: Real(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),"${Paths.get("").toAbsolutePath()}/src/test/resources/importResultsTestDir/testFile.json"); 
                }
            }
            """.trimIndent()
        )
        propagate()

        Assertions.assertTrue(global.resolveVar("test::myAmplifier::gain")!!.aadd().isEmpty())
        Assertions.assertEquals(1, status.exceptions.size, "Error messages: ${status.exceptions}")
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
    fun wrongFileNameTest() = SessionManager.testSession("Parts") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))


        loadSysMD(
            """
            package test {
                import ScalarValues::*; 
                import SI::*; 
                           
                part def Amplifier isA Base::Anything;
            
                part def LNA isA Amplifier {    
                    attribute gain: Real [dB] = [5.0 .. 20.0] dB; 
                }
            
                part def Stage2 isA Amplifier { 
                    attribute gain: Real [dB] = [11.0 .. 20.0] dB;
                }
            
                part def Driver isA Amplifier {
                    attribute gain: Real [dB] = [10.0 .. 30.0] dB; 
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: Real(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),"wrongFileNameThatDoesntWork.json"); 
                }
            }
            """.trimIndent()
        )
        propagate()
        Assertions.assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        Assertions.assertEquals(26.0, global.resolveVar("test::myAmplifier::gain")!!.aadd().min,0.00001)
        Assertions.assertEquals(35.0,global.resolveVar("test::myAmplifier::gain")!!.aadd().max,0.00001)
    }

    @Test
    //This test deliberately declares a wrong unit in the JSON file which causes the function to use a [-Inf,+Inf] range as intersection partner
    //The result should therefore be the actual result of the ASTFunction
    fun wrongUnitTest() = SessionManager.testSession("Parts") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 28.5,
            resultUnit = "WRONG_UNIT",
            attrFQN = "test::myAmplifier::gain")

        loadSysMD(
            """
            package test {
                import ScalarValues::*; 
                import SI::*; 
                           
                part def Amplifier isA Base::Anything;
            
                part def LNA isA Amplifier {    
                    attribute gain: Real [dB] = [5.0 .. 20.0] dB; 
                }
            
                part def Stage2 isA Amplifier { 
                    attribute gain: Real [dB] = [11.0 .. 20.0] dB;
                }
            
                part def Driver isA Amplifier {
                    attribute gain: Real [dB] = [10.0 .. 30.0] dB; 
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: Real(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),"${Paths.get("").toAbsolutePath()}/src/test/resources/importResultsTestDir/testFile.json"); 
                }
            }
            """.trimIndent()
        )
        propagate()

        Assertions.assertEquals(26.0, global.resolveVar("test::myAmplifier::gain")!!.aadd().min,0.00001)
        Assertions.assertEquals(35.0,global.resolveVar("test::myAmplifier::gain")!!.aadd().max,0.00001)
        Assertions.assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    //This test checks the behavior if an illegal argument is passed as second parameter
    fun wrongSecondArgumentTest() = SessionManager.testSession("Parts") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 28.5,
            resultUnit = "dB",
            attrFQN = "test::myAmplifier::gain")

        loadSysMD(
            """
            package test {
                import ScalarValues::*; 
                import SI::*; 
                           
                part def Amplifier isA Base::Anything;
            
                part def LNA isA Amplifier {    
                    attribute gain: Real [dB] = [5.0 .. 20.0] dB; 
                }
            
                part def Stage2 isA Amplifier { 
                    attribute gain: Real [dB] = [11.0 .. 20.0] dB;
                }
            
                part def Driver isA Amplifier {
                    attribute gain: Real [dB] = [10.0 .. 30.0] dB; 
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: Real(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),productOverParts(gain)); 
                }
            }
            """.trimIndent()
        )
        propagate()

        Assertions.assertEquals(26.0, global.resolveVar("test::myAmplifier::gain")!!.rangeSpecs[0].min,0.00001)
        Assertions.assertEquals(35.0,global.resolveVar("test::myAmplifier::gain")!!.rangeSpecs[0].max,0.00001)
        Assertions.assertEquals(1, status.exceptions.size, "Error messages: ${status.exceptions}")
    }





    /*************************************************************************
     ***************************ASTFunction Tests ****************************
     *******                                                        **********
     *******    These tests try a variety of ASTFunctions           **********
     *************************************************************************/
    @Test
    //This test checks if the characterizedResult function can handle an AstMax Node
    //The max function returns [10 .. 30] which is intersected with [25 .. 26]
    fun maxFunctionTest() = SessionManager.testSession("Parts") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 28.5,
            resultUnit = "m",
            attrFQN = "gain")

        loadSysMD(
            """       
                attribute a: ScalarValues::Real [m] = [5.0 .. 30.0] m;
                attribute b: ScalarValues::Real [m] = [10.0 .. 20.0] m;
                attribute gain: ScalarValues::Real = characterizedResult(max(a,b),"${Paths.get("").toAbsolutePath()}/src/test/resources/importResultsTestDir/testFile.json").
            
            """.trimIndent(),
        )
        propagate()

        Assertions.assertEquals(28.5, global.resolveVar("gain")!!.aadd().min,0.00001)
        Assertions.assertEquals(28.5,global.resolveVar("gain")!!.aadd().max,0.00001)
        Assertions.assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
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