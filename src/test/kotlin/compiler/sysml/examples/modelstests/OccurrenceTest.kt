package sysmlv2specificationtests.modelstests

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue


class OccurrenceTest {

    @Test
    fun testOccurrence() = testSession("Parts", "Occurrences") {
        loadSysMLv2("""
            package 'Event Occurrence Example' {	
                part def Driver;
                part def CruiseController;
                part def Speedometer;
                part def Engine;
                part def Vehicle;
                
                part driver : Driver {
                    event occurrence setSpeedSent;
                }
                
                part vehicle : Vehicle {
                
                    part cruiseController : CruiseController {
                        event occurrence setSpeedReceived;		
                        then event occurrence sensedSpeedReceived;		
                        then event occurrence fuelCommandSent;
                    }
                    
                    part speedometer : Speedometer {
                        event occurrence sensedSpeedSent;
                    }
                    
                    part engine : Engine {
                        event occurrence fuelCommandReceived;
                    }
                }
            }
        """)
        assertNoIssues()
    }
}