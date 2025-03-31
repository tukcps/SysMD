package sysmlv2specificationtests.modelstests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue


class ConnectionTest {

    @Ignore
    @Test
    fun testConnection() = testSession("Connections", "Parts") {
        loadSysMLv2("""
                package 'Connections Example' {
                    
                    part def WheelHubAssembly;
                    part def WheelAssembly;
                    part def Tire;
                    part def TireBead;
                    part def Wheel;
                    part def TireMountingRim;
                    part def LugBoltMountingHole;
                    part def Hub;
                    part def LugBoltThreadableHole;
                    part def LugBoltJoint;
                    
                    connection def PressureSeat {
                        end bead : TireBead[1];
                        end mountingRim : TireMountingRim[1];
                    }
                    
                    part wheelHubAssembly : WheelHubAssembly {
                        
                        part wheel : WheelAssembly[1] {
                            part t : Tire[1] {
                                part bead : TireBead[2];			
                            }
                            part w: Wheel[1] {
                                part rim : TireMountingRim[2];
                                part mountingHoles : LugBoltMountingHole[5];
                            }						
                            connection : PressureSeat 
                                connect bead references t.bead 
                                to mountingRim references w.rim;		
                        }
                        
                        part lugBoltJoints : LugBoltJoint[0..5];
                        part hub : Hub[1] {
                            part h : LugBoltThreadableHole[5];
                        }
                        connect lugBoltJoints[0..1] to wheel.w.mountingHoles[1];
                        connect lugBoltJoints[0..1] to hub.h[1];
                    }
                }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

    }
}