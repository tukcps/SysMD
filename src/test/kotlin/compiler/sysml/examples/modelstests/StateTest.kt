package compiler.sysml.examples.modelstests

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test


class StateTest {
    /**
     * Source: Tutorial SysML v2, OMG
     * LGPL License, share and treat accordingly.
     * Test fails as unclear how to map it to grammar (bug in grammar?)
     */
    @Test
    fun testStates() = testSession("States") {
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

    @Test
    fun testStatesLong() = testSession("States") {
        loadSysMLv2("""
            state def VehicleStates {
                entry start; then off; 
                
                state off;
                
                transition off_to_starting
                    first off
                    accept VehicleStartSignal
                    then starting;
                    
                state starting;

                transition starting_to_on
                    first starting
                    accept VehicleOnSignal
                    then on;

                state on;

                transition on_to_off
                    first on
                    accept VehicleOffSignal
                    then off;
        }
        """)
        assertNoIssues { !it.message.contains("resolved") }
    }
}