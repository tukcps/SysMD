package sysmlv2specificationtests.modelstests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue


class GeneralizationTest {


    @Test
    fun testGeneralization() = testSession("States", "Requirements", "Interfaces", "Allocations", "Connections",
        "Attributes", "Ports", "Parts", "Items", "Occurrences") {
        loadSysMLv2(
            """
                package 'Generalization Example' {
                
                    abstract part def Vehicle;
                    
                    part def HumanDrivenVehicle specializes Vehicle {
                    //ref
                        part driver : Person;
                    }
                    
                    part def PoweredVehicle :> Vehicle {
                        part eng : Engine;
                    }
                    
                    part def HumanDrivenPoweredVehicle :> 
                        HumanDrivenVehicle, PoweredVehicle;
                    
                    part def Engine;	
                    part def Person;
                    
                }
    """.trimIndent()
        )
        assertTrue(status.issues.isEmpty(), status.issues.toString())

    }
}