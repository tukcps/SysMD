package compiler.sysml.examples.modelstests

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test


class StateTest {
    @Ignore
    @Test
    fun testStates() = testSession("States", "Parts") {
        loadSysMLv2("""
                package 'State Example' {
                    
                    attribute def VehicleStartSignal;
                    attribute def VehicleOnSignal;
                    attribute def VehicleOffSignal;
                    
                    state def VehicleStates;
                        
                    state vehicleStates : VehicleStates {
                        entry; then off;
                        
                        state off;
                        accept VehicleStartSignal 
                            then starting;
                            
                        state starting;
                        accept VehicleOnSignal
                            then on;
                            
                        state on;
                        accept VehicleOffSignal
                            then off;
                    }
                }
        """)
        assertNoIssues()
    }
}