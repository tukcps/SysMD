package sysmlv2specificationtests.modelstests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue


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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}