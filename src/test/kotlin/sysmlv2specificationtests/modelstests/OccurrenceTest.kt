package sysmlv2specificationtests.modelstests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue


class OccurrenceTest {

    @Ignore
    @Test
    fun testOccurrence() = testSession( "Attributes", "Parts", "Occurrences") {
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
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}