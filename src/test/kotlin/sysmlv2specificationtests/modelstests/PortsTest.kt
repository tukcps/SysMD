package sysmlv2specificationtests.modelstests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue


class PortsTest {

    @Test
    fun testPorts() = testSession("States", "Requirements", "Interfaces", "Allocations", "Connections",
        "Attributes", "Ports", "Parts", "Items", "Occurrences") {
        loadSysMLv2(
            """
                package 'Port Example' {
                    
                    attribute def Temp;
                    
                    part def Fuel;
                    
                    port def FuelOutPort {
                        attribute temperature : Temp;
                        out item fuelSupply : Fuel;
                        in item fuelReturn : Fuel;
                    }
                    
                    port def FuelInPort {
                        attribute temperature : Temp;
                        in item fuelSupply : Fuel;
                        out item fuelReturn : Fuel;
                    }
                    
                    part def FuelTankAssembly {
                        port fuelTankPort : FuelOutPort;
                    }
                    
                    part def Engine {
                        port engineFuelPort : FuelInPort;
                    }
                }
    """.trimIndent()
        )
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

    }
}