package examples

import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Tuebingen {

    @Test
    fun estimationExample() = SessionManager.testSession {
        loadSysMD(
            catchExceptions = false,
            input = """
            package HwSwPerformance {
                class NeuralNetworkModel;
                class HardwareModel; 
                class HwSwComponent.
            }
             
            HwSwPerformance::NeuralNetworkModel hasA
                attribute C: ScalarValues::Integer(1 .. *);
                attribute H: ScalarValues::Integer(1 .. *);
                attribute W: ScalarValues::Integer(1 .. *);
                attribute K: ScalarValues::Integer(1 .. *);
                attribute X: ScalarValues::Integer(1 .. *);
                attribute Y: ScalarValues::Integer(1 .. *);
                attribute FH: ScalarValues::Integer(1 .. *);
                attribute FW: ScalarValues::Integer(1 .. *);
                attribute fn: ScalarValues::Integer(1 .. *);
                attribute dn: ScalarValues::Integer(1 .. *).
              
            HwSwPerformance::HardwareModel hasA
                attribute s1: ScalarValues::Integer(1 .. *); 
                attribute s2: ScalarValues::Integer(1 .. *);
                attribute s3: ScalarValues::Integer(1 .. *);
                attribute s4: ScalarValues::Integer(1 .. *).
             
                attribute peakPerformance: ScalarValues::Real(0 .. *) [1/s];   
                attribute peakBandwidth: ScalarValues::Real(0 .. *) [1/s].      
             
            HwSwPerformance::HwSwComponent hasA
                part hw: HardwareModel;
                part nn: NeuralNetworkModel;
                attribute utilizationEfficiency: ScalarValues::Integer = hw::s1 * hw::s2 * hw::s3.                 
            """.trimIndent()
        )
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }
}