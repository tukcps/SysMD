package sysmlv2specificationtests.modelstests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue


class PartsTest {
    @Test
    fun testParts() = testSession("Parts") {
        loadSysMLv2("""
                package 'Parts Example-2' {
                    
                    // Definitions
                    part def Vehicle;	
                    part def Engine;	
                    part def Cylinder;
                    
                    // Usages
                    part vehicle : Vehicle {
                        part eng : Engine {
                            part cyl : Cylinder[4..6];
                        }
                    }
                    
                    part smallVehicle :> vehicle {
                        part redefines eng {
                            part redefines cyl[4];
                        }
                    }
                    
                    part bigVehicle :> vehicle {
                        part redefines eng {
                            part redefines cyl[6];
                        }
                    }     
                }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

    }
}