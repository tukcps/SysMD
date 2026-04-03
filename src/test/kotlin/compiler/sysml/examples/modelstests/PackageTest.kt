package sysmlv2specificationtests.modelstests

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test


class PackageTest {

    @Test
    fun testPackage() = testSession("States", "Requirements", "Interfaces", "Allocations", "Connections",
        "Attributes", "Ports", "Parts", "Items", "Occurrences", initialize = false) {
        loadSysMLv2("""
            package 'Package Example' {
                public import ISQ::TorqueValue;
                private import ScalarValues::*;
                 
                private part def Automobile;
                
                public alias Car for Automobile;	                         
                alias Torque for ISQ::TorqueValue;
            }
        """)
        assertNoIssues()
    }
}