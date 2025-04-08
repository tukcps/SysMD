package sysmlv2specificationtests.modelstests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue


class InterfaceTest {

    @Test
    fun testInterface() = testSession("Interfaces", "Connections", "Ports") {
        loadSysMLv2("""
            package 'Interface Decomposition Example' {
                
                port def SpigotBank;
                port def Spigot;
                
                port def Faucet;
                port def FaucetInlet;
                
                interface def WaterDelivery {
                    end suppliedBy : SpigotBank[1] {
                        port hot : Spigot;
                        port cold : Spigot;
                    }
                    end deliveredTo : Faucet[1..*] {
                        port hot : FaucetInlet;
                        port cold : FaucetInlet;
                    }
                    
                    connect suppliedBy.hot to deliveredTo.hot;
                    connect suppliedBy.cold to deliveredTo.cold;
                }	
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

    }
}