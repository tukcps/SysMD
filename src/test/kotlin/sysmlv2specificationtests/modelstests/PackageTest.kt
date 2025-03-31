package sysmlv2specificationtests.modelstests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue


class PackageTest {

    @Ignore
    @Test
    fun testPackage() = testSession("States", "Requirements", "Interfaces", "Allocations", "Connections",
        "Attributes", "Ports", "Parts", "Items", "Occurrences") {
        loadSysMLv2(
            """
                package 'Package Example' {
                    public import ISQ::TorqueValue;
                    private import ScalarValues::*;
                     
                    private part def Automobile;
                    
                    public alias Car for Automobile;	                         
                    alias Torque for ISQ::TorqueValue;
                }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}