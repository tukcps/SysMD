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

    @Test fun iteTest() = testSession("ScalarValues") {
        loadKerML(input = """
                feature a: ScalarValues::Real {:>> range = "1.0 .. 1.0";} 
                feature b: ScalarValues::Real(2.0); 
                feature c: ScalarValues::Real(3.0);
                feature value2: ScalarValues::Real = a + ITE( (b > 1.0) and (b < 100.0), 10.0, 20.0) {:>> range = "1 .. 100";} 
            """.trimIndent(), catchExceptions = false)
        settings.catchExceptions = false
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test fun iteTestInteger() = testSession("ScalarValues") {
        loadKerML(input ="""
                feature a: ScalarValues::Integer {:>> range = "1..1";}
                feature b: ScalarValues::Integer {:>> range = "2..2";}
                feature c: ScalarValues::Integer {:>> range = "3..3";}
                feature value2: ScalarValues::Integer = a + ITE( (b > 1) and (b < 100), 10, 20) {:>> range = "1 .. 100";}
            """.trimIndent(), catchExceptions = false)
        settings.catchExceptions = false
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test @Disabled
    fun nestedITETest() = testSession("ScalarValues", "SI", "ISO26262") {
        loadSysMLv2("""   
            package Smartgrid{
                private import ISO26262::*;
                private import SI::*;
                private import ScalarValues::*;
                part def microgrid isA Component;
                part def AASsystem isA Component;
                part def EhealthAsystem isA Component;
                part def Nodegenerator isA Component;
                part def Houseconsumer isA Component;
                part def Vehicle isA Component;
                part def EnergyMobilitysystem isA Component;
                part def Vehicle {
                    attribute stateOfCharge: Real(20..90) [%];
                    attribute startDischargingLimit: Real [%];
                    attribute startChargingLimit: Real [%];
                    attribute capacity: Charge [Ah];
                    attribute voltage: Voltage [V];
                    attribute maximumChargePower: Power [kW];
                    attribute maximumDischargePower: Power [kW];
                    attribute isSupplier: Boolean = if stateOfCharge >= startChargingLimit ? true else false;
                    attribute isCharging: Boolean = if stateOfCharge <= startDischargingLimit ? true else false;
                    attribute vehiclePowConsumption: Power [kW] = if isSupplier ? maximumDischargePower else 
                                (if isCharging ? maximumChargePower else 0.0 [kW]).
                }
            }
            
            package Microgrid{
                import ISO26262::*;
                import SI::*;
                import ScalarValues::*;
                package Consumers;
                package Vehicles{
                    part def PublicTrafficVehicle isA Smartgrid::Vehicle{
                        attribute capacity: Charge [Ah] = 360.0 [Ah];
                        attribute startDischargingLimit: Real [%] = 30.0 [%];
                        attribute startChargingLimit: Real [%] = 70.0 [%];
                        attribute voltage: Voltage [V] = 384.0 [V];
                        attribute maximumChargePower: Power [kW] = 108.0 [kW];
                        attribute maximumDischargePower: Power [kW] = - 147.9 [kW].
                    }
                    part def PublicTrafficVehicle2 isA Smartgrid::Vehicle{
                        attribute capacity: Charge [Ah] = 360.0 [Ah];
                        attribute startDischargingLimit: Real [%] = 30.0 [%];
                        attribute startChargingLimit: Real [%] = 70.0 [%];
                        attribute voltage: Voltage [V] = 384.0 [V];
                        attribute maximumChargePower: Power [kW] = 108.0 [kW];
                        attribute maximumDischargePower: Power [kW] = - 147.9 [kW].
                    }
                }
                import Smartgrid::*;
                part def VehicleChargingStation isA Component {
                    part vehicles8: [1..1] Microgrid::Vehicles::PublicTrafficVehicle,
                    part vehicles9: [1..1] Microgrid::Vehicles::PublicTrafficVehicle2,
                    attribute vehiclePowConsumption: Real [kW] = sumOverParts(vehiclePowConsumption).
                }
                part def connectGrid isA Component {
                    part chargingStation: Microgrid::VehicleChargingStation,  
                    attribute mainsupplyOn: Boolean(true),
                    attribute mainsupply: Real(2..2) [MW],
                    attribute powDemand: Real [kW] =  chargingStation::vehiclePowConsumption.
                    //attribute outageCover: Boolean = ITE(mainsupplyOn, powDemand < mainsupply, powDemand < (battery::capacity / 0.5[h])).
                }
            }
           """)
        propagate()
        assertTrue(status.issues.isEmpty(), "Exceptions: ${status.issues}")
    }

    @Test @Disabled
    fun ITERuntimeTest() = testSession("ScalarValues") {
        loadKerML("""  
            import ScalarValues::*;
            feature x: Real(0.2..0.8);
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