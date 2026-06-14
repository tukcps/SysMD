package exports

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertIssue
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
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
    fun baseTest() = testSession("Parts", "ISQ") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 28.5,
            resultUnit = "dB",
            attrFQN = "test::myAmplifier::gain")

        loadSysMLv2("""
            package test {
                private import ScalarValues::*; 
                private import ISQ::*; 
                           
                part def Amplifier :> Base::Anything;
            
                part def LNA :> Amplifier {    
                    attribute gain: Quantities::ScalarQuantityValue = [5.0 .. 20.0] dB {:>> unit = "dB";}
                }
            
                part def Stage2 :> Amplifier { 
                    attribute gain: Quantities::ScalarQuantityValue = [11.0 .. 20.0] dB {:>> unit = "dB";}
                }
            
                part def Driver :> Amplifier {
                    attribute gain: Quantities::ScalarQuantityValue = [10.0 .. 30.0] dB {:>> unit = "dB";}
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: Quantities::ScalarQuantityValue(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),"${Paths.get("").toAbsolutePath()}/src/test/resources/importResultsTestDir/testFile.json"); 
                }
            }
            """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(28.5, global.resolveVar("test::myAmplifier::gain")!!.aadd().min,0.00001)
        assertEquals(28.5,global.resolveVar("test::myAmplifier::gain")!!.aadd().max,0.00001)
        assertNoIssues()
    }

    @Test
    //In this test, the range of the imported Result is larger than the range of the upQuantity of the ASTFunction
    fun baseTestResultNotInBounds() = testSession("ISQ", "Parts") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 24.5,
            resultUnit = "dB",
            attrFQN = "test::myAmplifier::gain")

        loadSysMLv2("""
            package test {
                private import ScalarValues::*; 
                private import Quantities::*; 
                           
                part def Amplifier :> Base::Anything;
            
                part def LNA :> Amplifier {    
                    attribute gain: ScalarQuantityValue(5..20) [dB];
                }
            
                part def Stage2 :> Amplifier { 
                    attribute gain: ScalarQuantityValue(11..20)[dB];
                }
            
                part def Driver :> Amplifier {
                    attribute gain: ScalarQuantityValue(20..30)[dB]; 
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: ScalarQuantityValue(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),"${Paths.get("").toAbsolutePath()}/src/test/resources/importResultsTestDir/testFile.json"); 
                }
            }
        """, Runlevel.ALL)
        assertTrue(solver.getVariable("test::myAmplifier::gain")!!.aadd().isEmpty())
        assertIssue("not satisfiable")
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
    fun wrongFileNameTest() = testSession("Parts", "ISQ") {
        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))
        loadSysMLv2("""
            package test {
                private import ScalarValues::*; 
                private import Quantities::*; 
                           
                part def Amplifier :> Base::Anything;
            
                part def LNA :> Amplifier {    
                    attribute gain: ScalarQuantityValue(5.0 .. 20.0) [dB];
                }
            
                part def Stage2 :> Amplifier { 
                    attribute gain: ScalarQuantityValue(11.0 .. 20.0) [dB];
                }
            
                part def Driver :> Amplifier {
                    attribute gain: ScalarQuantityValue(10.0 .. 30.0) [dB];
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: ScalarQuantityValue(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),"wrongFileNameThatDoesntWork.json"); 
                }
            }
        """, Runlevel.ALL)
        assertEquals(1, status.issues.size, "Expected an error message that reports missing file with JSON")
        assertEquals(26.0, global.resolveVar("test::myAmplifier::gain")!!.aadd().min,0.00001)
        assertEquals(35.0,global.resolveVar("test::myAmplifier::gain")!!.aadd().max,0.00001)
    }

    @Test
    //This test deliberately declares a wrong unit in the JSON file which causes the function to use a [-Inf,+Inf] range as intersection partner
    //The result should therefore be the actual result of the ASTFunction
    fun wrongUnitTest() = testSession("Parts", "ISQ", "Ranges") {
        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))
        writeJson(
            resultValue = 28.5,
            resultUnit = "WRONG_UNIT",
            attrFQN = "test::myAmplifier::gain")

        loadSysMLv2("""
            package test {
                private import ScalarValues::*; 
                private import Quantities::*; 
                           
                part def Amplifier;
            
                part def LNA :> Amplifier {    
                    attribute gain: ScalarQuantityValue(5.0 .. 20.0) [dB]; 
                }
            
                part def Stage2 :> Amplifier { 
                    attribute gain: ScalarQuantityValue(11.0 .. 20.0) [dB];
                }
            
                part def Driver :> Amplifier {
                    attribute gain: ScalarQuantityValue(10.0 .. 30.0) [dB];
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: ScalarQuantityValue(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain),"${Paths.get("").toAbsolutePath()}/src/test/resources/importResultsTestDir/testFile.json"); 
                }
            }
        """, Runlevel.ALL)
        //assertTrue(status.issues.isEmpty(), "${status.issues}")
        assertEquals(26.0, global.resolveVar("test::myAmplifier::gain")!!.aadd().min, 0.00001)
        assertEquals(35.0,global.resolveVar("test::myAmplifier::gain")!!.aadd().max, 0.00001)
        assertEquals(1, status.issues.filter { it.kind == Issue.Kind.ERROR }.size)
    }

    @Test
    //This test checks the behavior if an illegal argument is passed as second parameter
    fun wrongSecondArgumentTest() = testSession("Parts", "ISQ", "Ranges") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 28.5,
            resultUnit = "dB",
            attrFQN = "test::myAmplifier::gain")

        loadSysMLv2("""
            package test {
                private import ScalarValues::*; 
                private import ISQ::*; 
                           
                part def Amplifier;
            
                part def LNA :> Amplifier {    
                    attribute gain: Quantities::ScalarQuantityValue(5.0 .. 20.0) [dB]; 
                }
            
                part def Stage2 :> Amplifier { 
                    attribute gain: Quantities::ScalarQuantityValue(11.0 .. 20.0) [dB];
                }
            
                part def Driver :> Amplifier {
                    attribute gain: Quantities::ScalarQuantityValue(10.0 .. 30.0) [dB]; 
                }
                
                part myAmplifier {
                    part lna:    LNA;  
                    part stage2: Stage2;  
                    part driver: Driver;  
                    attribute gain: Quantities::ScalarQuantityValue(26.0 .. 35.0) [dB] = characterizedResult(productOverParts(gain), productOverParts(gain)); 
                }
            }
        """, Runlevel.ALL)
       // assertTrue(status.issues.isEmpty(), "${status.issues}")
        assertEquals(26.0, global.resolveVar("test::myAmplifier::gain")!!.rangeSpecs[0].min,0.00001)
        assertEquals(35.0,global.resolveVar("test::myAmplifier::gain")!!.rangeSpecs[0].max,0.00001)
        assertEquals(1, status.issues.size, "Error messages: ${status.issues}")
    }





    /*************************************************************************
     ***************************ASTFunction Tests ****************************
     *******                                                        **********
     *******    These tests try a variety of ASTFunctions           **********
     *************************************************************************/
    @Test
    //This test checks if the characterizedResult function can handle an AstMax Node
    //The max function returns [10 … 30] which is intersected with [25 … 26]
    fun maxFunctionTest() = testSession("Parts", "ISQ") {

        Files.createDirectories(Paths.get("src/test/resources/importResultsTestDir"))

        writeJson(
            resultValue = 28.5,
            resultUnit = "m",
            attrFQN = "gain")

        loadSysMLv2("""       
            attribute a: ISQ::LengthValue = [5.0 .. 30.0] m;
            attribute b: ISQ::LengthValue = [10.0 .. 20.0] m;
            attribute gain: ScalarValues::Real = characterizedResult(max(a,b),"${Paths.get("").toAbsolutePath()}/src/test/resources/importResultsTestDir/testFile.json"); 
        """, Runlevel.ALL)
        assertNoIssues()

        assertEquals(28.5, global.resolveVar("gain")!!.min(),0.00001)
        assertEquals(28.5,global.resolveVar("gain")!!.max(),0.00001)
        assertNoIssues()
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