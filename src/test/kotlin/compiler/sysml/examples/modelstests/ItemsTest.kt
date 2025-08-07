package sysmlv2specificationtests.modelstests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue


class ItemsTest {

    @Test
    fun testItems() = testSession( "Parts", "Items") {
        loadSysMLv2("""
                package 'Items Example' {
                    private import ScalarValues::*;
                    
                    item def Fuel;
                    item def Person;
                    
                    part def Vehicle {
                        attribute mass : Real;
                        //ref
                        item driver : Person;
                
                        part fuelTank {
                            item fuel: Fuel;
                        }		
                    }
                }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}