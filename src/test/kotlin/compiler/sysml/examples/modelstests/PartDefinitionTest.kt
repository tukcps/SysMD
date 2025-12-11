package sysmlv2specificationtests.modelstests

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test


class PartDefinitionTest {

    @Test
    fun testPartDefinition() = testSession("Attributes", "Parts") {
        loadSysMLv2("""
            package 'Part Definition Example' {
                private import ScalarValues::*;
                
                part def Vehicle {
                    attribute mass : Real;
                    attribute status : VehicleStatus;
                    
                    part eng : Engine;
                    //ref
                    part driver : Person;
                }
                
                attribute def VehicleStatus {
                    attribute gearSetting : Integer;
                    attribute acceleratorPosition : Real;
                }
                
                part def Engine;	
                part def Person;
            }
        """)
        assertNoIssues()
    }
}