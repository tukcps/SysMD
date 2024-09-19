package exportstests

import com.github.tukcps.sysmd.exports.Exporter
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class TestBenchTests {

    @Test
    fun pipecleaner_TB_Test() = SessionManager.testSession("ScalarValues", "Parts", "Ports", "Requirements") {
        settings.catchExceptions = true
        loadSysMD(
            """
        package Amp_Pipecleaner {
            import ScalarValues::*; 
            import SI::*; 
            
            // Library instances --> SystemC classes
            part def Amplifier isA Base::Anything {
                attribute gain: Real [dB] = [0.0 .. 100.0] dB;
            }
            
            // Concrete model --> SystemC instances of library classes
            part myAmplifier {
                part lna: Amplifier {
                    in port input{
                       attribute value: ScalarValues::Real;  
                    }
                    out port output{
                       attribute value: ScalarValues::Real;  
                    }   
                    attribute gain: Real [dB] = [15.0 .. 20.0] dB;  
                }
                
                part stage2: Amplifier {
                    in port input{
                       attribute value: ScalarValues::Real;  
                    }
                    out port output{
                       attribute value: ScalarValues::Real;  
                    }
                    attribute gain: Real [dB] = [5.0 .. 20.0] dB;  
                }
                
                part driver: Amplifier {
                    in port input{
                       attribute value: ScalarValues::Real;  
                    }
                    out port output{
                       attribute value: ScalarValues::Real;  
                    }
                    attribute gain: Real [dB] = [5.0 .. 20.0] dB;  
                }
                
                attribute total_gain: Real(20 .. 30) [dB] = productOverParts(gain); 
            }
            
            attribute ambientTemperature : Real [°C] = [-15.0 .. 40.0] °C;
            
            assoc Signal;
            interface lna_to_stage2 : Signal connect Amp_Pipecleaner::myAmplifier::lna::output to Amp_Pipecleaner::myAmplifier::stage2::input;
            interface stage2_to_driver : Signal connect Amp_Pipecleaner::myAmplifier::stage2::output to Amp_Pipecleaner::myAmplifier::driver::input;
            
            requirement PipeCleaner_Requirements {
                subject f references myAmplifier;
                
                assert ambientTempRange { (ambientTemperature >= -15.0 [°C]) } 
                    // The code for creating Constraint in exporter has a bug: 
                    // it assumes assertions of kind Real (OP) Real, but this is then: 
                    //   Real OP Real AND Real OP real which has a root with two child of type boolean 
                    //   that have leaves of type Real !!!
                    //   --> Tree would have to be traversed completely 
                    // &  (ambientTemperature <= 40.0 [°C]) }
                
                require outputGain {
                    f::total_gain >= 2.0
                }
            }
            
        }
            """.trimIndent()
        )

        val testDirectory = File("src/test/resources/toSystemC")
        val exporter = Exporter()

        val pack = global.resolve<Element>("Amp_Pipecleaner") as Element
        //classResolution(pack,0)
        exporter.analyzeSysMD(pack)

        val ucb = exporter.getUCBData()
        ucb.requirements.forEach { it.generateTB = true; it.existsTB = false }

        exporter.toSystemC(pathIn = testDirectory.path, tbLibFolder = "")

        Assertions.assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
    }

}