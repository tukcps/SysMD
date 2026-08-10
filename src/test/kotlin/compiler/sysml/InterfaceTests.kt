package compiler.sysml

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test

class InterfaceTests {
    @Test
    fun interfaceTest() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            item def BoolSignal {
                attribute value: ScalarValues::Boolean; 
                attribute time: ScalarValues::Real = 0.0;  
            }
            port def BoolPort {
                in item BoolSignal; 
            }
            part cpu {
                port clk: BoolPort; 
            }
            part clock {
                port clk: ~BoolPort; // conjugation by ~ inverses direction
            }
            interface clockSignal connect cpu.clk to clock.clk;
        """, Runlevel.MODEL)
        assertNoIssues()

    }
}