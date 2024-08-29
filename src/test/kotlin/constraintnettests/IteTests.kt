package sysmltests.constraintnettests

import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.resolve.resolveVar
import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IteTests {

    @Test fun iteTest() = testSession {
        loadSysMD(input = """
                attribute a: ScalarValues::Real(1.0 .. 1.0); 
                attribute b: ScalarValues::Real(2.0); 
                attribute c: ScalarValues::Real(3.0);
                attribute value2: ScalarValues::Real(1 .. 100) = a + ITE( (b > 1.0) and (b < 100.0), 10.0, 20.0);
            """.trimIndent(), catchExceptions = false)
        settings.catchExceptions = false
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test fun iteTestInteger() = testSession {
        loadSysMD(input = """
                attribute a: ScalarValues::Integer(1..1); 
                attribute b: ScalarValues::Integer(2..2); 
                attribute c: ScalarValues::Integer(3..3);
                attribute value2: ScalarValues::Integer(1 .. 100) = a + ITE( (b > 1) and (b < 100), 10, 20);
            """.trimIndent(), catchExceptions = false)
        settings.catchExceptions = false
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test @Disabled
    fun nestedITETest() = testSession("ScalarValues","SI", "ISO26262") {
        loadSysMD("""   
            package Smartgrid{
                import ISO26262::*;
                import SI::*;
                import ScalarValues::*;
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
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
    }

    @Test @Disabled
    fun ITERuntimeTest() = testSession {
        loadSysMD("""  
            import ScalarValues::*;
            attribute x: Real(0.2..0.8);
            attribute cond: Boolean = if x <= 0.3 ? true else false;
            attribute res: Real = if cond? 2.0 else 0.0.
           """)
        propagate()
        val x = global.resolveVar("x")
        assertNotNull(x)
        val cond = global.resolveVar("cond")
        val res = global.resolveVar("res")
        assertTrue(cond!!.bdd().height() < 2)
        assertTrue(res!!.aadd().height() < 2)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
    }
}