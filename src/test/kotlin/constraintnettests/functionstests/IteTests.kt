package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IteTests {

    @Test fun iteTest() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange  {:>> range = 1.0 .. 1.0;} 
            feature b: Ranges::RealInRange {:>> range = 2.0;} 
            feature c: Ranges::RealInRange {:>> range = 3.0;}
            feature value2: Ranges::RealInRange = a + ITE( (b > 1.0) and (b < 100.0), 10.0, 20.0) {:>> range = 1 .. 100;} 
        """, Runlevel.ALL)
        assertNoIssues()
    }

    @Test fun iteTestInteger() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = 1..1;}
                feature b: Ranges::IntegerInRange {:>> range = 2..2;}
                feature c: Ranges::IntegerInRange {:>> range = 3..3;}
                feature value2: Ranges::IntegerInRange = a + ITE( (b > 1) and (b < 100), 10, 20) {:>> range = 1 .. 100;}
        """, Runlevel.ALL)
        assertNoIssues()
    }

    @Test @Ignore
    fun nestedITETest() = testSession("ScalarValues", "ISQ", "ISO26262", "Ranges") {
        loadSysMLv2("""   
            package Smartgrid{
                private import ISO26262::*;
                private import ISQ::*;
                private import Ranges::*;
                private import ScalarValues::*;
                part def microgrid isA Component;
                part def AASsystem isA Component;
                part def EhealthAsystem isA Component;
                part def Nodegenerator isA Component;
                part def Houseconsumer isA Component;
                part def Vehicle isA Component;
                part def EnergyMobilitysystem isA Component;
                part def Vehicle {
                    attribute stateOfCharge: DimensionOne {:>> range = 20..90 [%];}
                    attribute startDischargingLimit: DimensionOne {:>> range = *..* [%];}
                    attribute startChargingLimit: DimensionOne {:>> range = *..* [%];}
                    attribute capacity: ChargeValue {:>> range = *..* [Ah];}
                    attribute voltage: VoltageValue 
                    attribute maximumChargePower: PowerValue {:>> range = *..* [kW];} 
                    attribute maximumDischargePower: PowerValue {:>> range = *..* [kW];}
                    attribute isSupplier: Boolean = if stateOfCharge >= startChargingLimit ? true else false;
                    attribute isCharging: Boolean = if stateOfCharge <= startDischargingLimit ? true else false;
                    attribute vehiclePowConsumption: Power = if isSupplier ? maximumDischargePower else 
                                (if isCharging ? maximumChargePower else 0.0 [kW]) {:>> range = *..* [kW];}
                }
            }
            
            package Microgrid{
                import ISO26262::*;
                import ISQ::*;
                import Ranges::*;
                import ScalarValues::*;
                package Consumers;
                package Vehicles{
                    part def PublicTrafficVehicle isA Smartgrid::Vehicle{
                        attribute capacity: ChargeValue = 360.0 [Ah];
                        attribute startDischargingLimit: DimensionOne = 30.0 [%];
                        attribute startChargingLimit: DimensionOne = 70.0 [%];
                        attribute voltage: VoltageValue = 384.0 [V];
                        attribute maximumChargePower: PowerValue = 108.0 [kW];
                        attribute maximumDischargePower: PowerValue = - 147.9 [kW];
                    }
                    part def PublicTrafficVehicle2 isA Smartgrid::Vehicle{
                        attribute capacity: ChargeValue = 360.0 [Ah];
                        attribute startDischargingLimit: DimensionOne = 30.0 [%];
                        attribute startChargingLimit: DimensionOne = 70.0 [%];
                        attribute voltage: VoltageValue = 384.0 [V];
                        attribute maximumChargePower: PowerValue = 108.0 [kW];
                        attribute maximumDischargePower: PowerValue = - 147.9 [kW] ;
                    }
                }
                import Smartgrid::*;
                part def VehicleChargingStation isA Component {
                    part vehicles8: [1..1] Microgrid::Vehicles::PublicTrafficVehicle,
                    part vehicles9: [1..1] Microgrid::Vehicles::PublicTrafficVehicle2,
                    attribute vehiclePowConsumption: PowerValue  = sumOverParts(vehiclePowConsumption) {:>> range = *..* [kW];}
                }
                part def connectGrid isA Component {
                    part chargingStation: Microgrid::VehicleChargingStation,  
                    attribute mainsupplyOn: Boolean {:>> range ="true";}
                    attribute mainsupply: PowerValue {:>> range = 2..2 [MW];}
                    attribute powDemand: PowerValue =  chargingStation::vehiclePowConsumption {:>> range = *..* [kW];}
                    //attribute outageCover: Boolean = ITE(mainsupplyOn, powDemand < mainsupply, powDemand < (battery::capacity / 0.5[h])).
                }
            }
        """)
        solver.propagate()
        assertTrue(status.issues.isEmpty(), "Exceptions: ${status.issues}")
    }

    @Test @Ignore
    fun iteRuntimeTest() = testSession("ScalarValues", "Ranges") {
        loadKerML("""  
            import ScalarValues::*;
            import Ranges::*;
            feature x: RealInRange {:>> range="0.2..0.8";}
            feature cond: Boolean = if x <= 0.3 ? true else false;
            feature res: Real = if cond? 2.0 else 0.0.
        """, Runlevel.ALL)
        val x = solver.getVariable("x")
        assertNotNull(x)
        val cond = solver.getVariable("cond")
        val res = solver.getVariable("res")
        assertTrue(cond!!.bdd().height() < 2)
        assertTrue(res!!.aadd().height() < 2)
        solver.propagate()
        assertTrue(status.issues.isEmpty(), "Exceptions: ${status.issues}")
    }
}
