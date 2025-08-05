package sysmltests.constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import org.junit.jupiter.api.Disabled
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IteTests {

    @Test fun iteTest() = testSession("ScalarValues", "Ranges") {
        loadKerML(input = """
                feature a: Ranges::RealInRange  {:>> range = "1.0 .. 1.0";} 
                feature b: Ranges::RealInRange {:>> range = "2.0";} 
                feature c: Ranges::RealInRange {:>> range = "3.0";}
                feature value2: Ranges::RealInRange = a + ITE( (b > 1.0) and (b < 100.0), 10.0, 20.0) {:>> range = "1 .. 100";} 
            """.trimIndent(), catchExceptions = false)
        settings.catchExceptions = false
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test fun iteTestInteger() = testSession("ScalarValues", "Ranges") {
        loadKerML(input ="""
                feature a: Ranges::IntegerInRange {:>> range = "1..1";}
                feature b: Ranges::IntegerInRange {:>> range = "2..2";}
                feature c: Ranges::IntegerInRange {:>> range = "3..3";}
                feature value2: Ranges::IntegerInRange = a + ITE( (b > 1) and (b < 100), 10, 20) {:>> range = "1 .. 100";}
            """.trimIndent(), catchExceptions = false)
        settings.catchExceptions = false
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test @Disabled
    fun nestedITETest() = testSession("ScalarValues", "SI", "ISO26262", "Ranges") {
        loadSysMLv2("""   
            package Smartgrid{
                private import ISO26262::*;
                private import SI::*;
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
                    attribute stateOfCharge: RealInRange {:>> unit="%"; :>> range="20..90";}
                    attribute startDischargingLimit: Real {:>> unit="%";}
                    attribute startChargingLimit: Real {:>> unit="%";}
                    attribute capacity: Charge {:>> unit="Ah";}
                    attribute voltage: Voltage 
                    attribute maximumChargePower: Power {:>> unit="kW";} 
                    attribute maximumDischargePower: Power {:>> unit="kW";}
                    attribute isSupplier: Boolean = if stateOfCharge >= startChargingLimit ? true else false;
                    attribute isCharging: Boolean = if stateOfCharge <= startDischargingLimit ? true else false;
                    attribute vehiclePowConsumption: Power = if isSupplier ? maximumDischargePower else 
                                (if isCharging ? maximumChargePower else 0.0 [kW]) {:>> unit="kW";}
                }
            }
            
            package Microgrid{
                import ISO26262::*;
                import SI::*;
                import Ranges::*;
                import ScalarValues::*;
                package Consumers;
                package Vehicles{
                    part def PublicTrafficVehicle isA Smartgrid::Vehicle{
                        attribute capacity: Charge = 360.0 [Ah] {:>> unit="Ah";}
                        attribute startDischargingLimit: Real = 30.0 [%] {:>> unit="%";}
                        attribute startChargingLimit: Real = 70.0 [%] {:>> unit="%";}
                        attribute voltage: Voltage = 384.0 [V];
                        attribute maximumChargePower: Power = 108.0 [kW] {:>> unit="kW";}
                        attribute maximumDischargePower: Power = - 147.9 [kW] {:>> unit="kW";}
                    }
                    part def PublicTrafficVehicle2 isA Smartgrid::Vehicle{
                        attribute capacity: Charge = 360.0 [Ah] {:>> unit="Ah";}
                        attribute startDischargingLimit: Real = 30.0 [%] {:>> unit="%";}
                        attribute startChargingLimit: Real = 70.0 [%] {:>> unit="%";}
                        attribute voltage: Voltage = 384.0 [V];
                        attribute maximumChargePower: Power = 108.0 [kW] {:>> unit="kW";}
                        attribute maximumDischargePower: Power = - 147.9 [kW] {:>> unit="kW";}
                    }
                }
                import Smartgrid::*;
                part def VehicleChargingStation isA Component {
                    part vehicles8: [1..1] Microgrid::Vehicles::PublicTrafficVehicle,
                    part vehicles9: [1..1] Microgrid::Vehicles::PublicTrafficVehicle2,
                    attribute vehiclePowConsumption: Real  = sumOverParts(vehiclePowConsumption) {:>> unit="kW";}
                }
                part def connectGrid isA Component {
                    part chargingStation: Microgrid::VehicleChargingStation,  
                    attribute mainsupplyOn: Boolean {:>> spec ="true";}
                    attribute mainsupply: RealInRange {:>> unit="MW";:>> range="2..2";}
                    attribute powDemand: Real =  chargingStation::vehiclePowConsumption {:>> unit="kW";}
                    //attribute outageCover: Boolean = ITE(mainsupplyOn, powDemand < mainsupply, powDemand < (battery::capacity / 0.5[h])).
                }
            }
           """)
        propagate()
        assertTrue(status.issues.isEmpty(), "Exceptions: ${status.issues}")
    }

    @Test @Disabled
    fun ITERuntimeTest() = testSession("ScalarValues", "Ranges") {
        loadKerML("""  
            import ScalarValues::*;
            import Ranges::*;
            feature x: RealInRange {:>> range="0.2..0.8";}
            feature cond: Boolean = if x <= 0.3 ? true else false;
            feature res: Real = if cond? 2.0 else 0.0.
           """)
        propagate()
        val x = global.resolveVar("x")
        assertNotNull(x)
        val cond = global.resolveVar("cond")
        val res = global.resolveVar("res")
        assertTrue(cond!!.bdd().height() < 2)
        assertTrue(res!!.aadd().height() < 2)
        propagate()
        assertTrue(status.issues.isEmpty(), "Exceptions: ${status.issues}")
    }
}