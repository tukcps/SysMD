package exportstests

import com.github.tukcps.sysmd.exports.Exporter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import util.mockup.loadSysMLv2
import util.testSession
import java.io.File

class TestBenchTests {

    @Test @Disabled
    fun pipecleaner_TB_Test() = testSession("Parts", "Ports", "Requirements", "Connections", "ISQ") {
        loadSysMLv2("""
        package Amp_Pipecleaner {
            private import ScalarValues::*; 
            private import ISQ::*; 
            
            // Library instances --> SystemC classes
            part def Amplifier isA Base::Anything {
                attribute gain: Quantities::ScalarQuantityValue = [0.0 .. 100.0] dB {:>> unit = "dB";}
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
                    attribute gain: Quantities::ScalarQuantityValue = [15.0 .. 20.0] dB {:>> unit = "dB";}  
                }
                
                part stage2: Amplifier {
                    in port input{
                       attribute value: ScalarValues::Real;  
                    }
                    out port output{
                       attribute value: ScalarValues::Real;  
                    }
                    attribute gain: Quantities::ScalarQuantityValue = [5.0 .. 20.0] dB {:>> unit = "dB";}  
                }
                
                part driver: Amplifier {
                    in port input{
                       attribute value: ScalarValues::Real;  
                    }
                    out port output{
                       attribute value: ScalarValues::Real;  
                    }
                    attribute gain: Quantities::ScalarQuantityValue = [5.0 .. 20.0] dB {:>> unit = "dB";}  
                }
                
                attribute total_gain: Quantities::ScalarQuantityValue = productOverParts(gain) {:>> unit = "dB"; :>> range = "20 .. 30";} 
            }
            
            attribute ambientTemperature : ISQ::TemperatureValue = [-15.0 .. 40.0] °C {:>> unit = "°C";}
            
            connection def Signal;
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
        """)
        solver.propagate()

        val testDirectory = File("src/test/resources/toSystemC")
        val exporter = Exporter()

        val pack = global.resolve("Amp_Pipecleaner")!!.memberElement
        //classResolution(pack,0)
        exporter.analyzeSysMD(pack)

        val ucb = exporter.getUCBData()
        ucb.requirements.forEach { it.generateTB = true; it.existsTB = false }

        exporter.toSystemC(pathIn = testDirectory.path, tbLibFolder = "")

        Assertions.assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
    }

}