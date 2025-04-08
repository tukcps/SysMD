package sysmlv2specificationtests.modelstests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class SubsettingTest {

    @Test
    fun testSubsetting() = testSession("States", "Requirements", "Interfaces", "Allocations", "Connections",
        "Attributes", "Ports", "Parts", "Items", "Occurrences") {
        loadSysMLv2("""
                package 'Redefinition Example' {
                
                    part def Vehicle {
                        part eng : Engine;
                    }
                    part def SmallVehicle :> Vehicle {
                        part smallEng : SmallEngine redefines eng;
                    }
                    part def BigVehicle :> Vehicle {
                        part bigEng : BigEngine :>> eng;
                    }
                
                    part def Engine {
                        part cyl : Cylinder[4..6];
                    }
                    part def SmallEngine :> Engine {
                        part redefines cyl[4];
                    }
                    part def BigEngine :> Engine {
                        part redefines cyl[6];
                    }
                
                    part def Cylinder;
                }
    """.trimIndent()
        )
        assertTrue(status.issues.isEmpty(), status.issues.toString())

    }
}