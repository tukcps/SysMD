package quantitytests

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertIssue
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class QuantityDimensionTests {

    /** Kinematic Formulars */
    @Test
    fun unitsKinematic1() = testSession("ISQ") {
        loadKerML("""
            feature t: ISQ::DurationValue = 2.0 [s];
            feature v: ISQ::SpeedValue  = 5.0 [m/s];
            feature s: ISQ::LengthValue  = v*t;"""
        )
        solver.propagate()
        assertEquals("m", solver.getVariable("s")!!.vectorQuantity.unit.toString())
        assertEquals(10.0, solver.getVariable("s")!!.max(), 0.00001)
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
        assertEquals("Speed", solver.getVariable("v")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("s")!!.vectorQuantity.getDomain())
        assertNoIssues()
    }

    @Test
    fun unitsKinematic2() = testSession("ISQ") {
        loadKerML("""
                feature t: ISQ::DurationValue  = 2000.0 [ms];
                feature v: ISQ::SpeedValue = 36.0 [km/h];
                feature v2: ISQ::SpeedValue  = v;
                feature s: ISQ::LengthValue  = v2*t;"""
        )
        solver.propagate()
        assertEquals("m / s", solver.getVariable("v2")!!.vectorQuantity.unit.toString())
        assertEquals(10.0, solver.getVariable("v2")!!.min(), 0.00001)
        assertEquals(20.0, solver.getVariable("s")!!.max(), 0.00001)
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
        assertEquals("Speed", solver.getVariable("v")!!.vectorQuantity.getDomain())
        assertEquals("Speed", solver.getVariable("v2")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("s")!!.vectorQuantity.getDomain())
        assertNoIssues()
    }


    @Test
    fun unitsKinematic3() = testSession("ISQ") {
        loadKerML("""
            feature t: ISQ::DurationValue = 2000.0 [ms] ;
            feature v: ISQ::SpeedValue = 36.0 [km/h];
            feature s: ISQ::LengthValue  = v*t;"""
        )
        solver.propagate()
        assertEquals("m / s", solver.getVariable("v")!!.vectorQuantity.unit.toString())
        assertEquals(2.0, solver.getVariable("t")!!.min(), 0.00001)
        assertEquals(10.0, solver.getVariable("v")!!.max(), 0.00001)
        assertEquals(20.0, solver.getVariable("s")!!.min(), 0.00001)
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
        assertEquals("Speed", solver.getVariable("v")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("s")!!.vectorQuantity.getDomain())
        assertNoIssues()
    }

    @Test
    fun unitsKinematic4() = testSession("ISQ") {
        loadKerML("""
                feature t: ISQ::DurationValue = 2.0 [s];
                feature v0: ISQ::SpeedValue  = 1.0 [m/s];
                feature a: ISQ::AccelerationValue  = 1.0 [m/s^2];
                feature s: ISQ::LengthValue  = 0.5*a*sqr(t)+v0*t;"""
        )
        solver.propagate()
        assertEquals("m / s", solver.getVariable("v0")!!.vectorQuantity.unit.toString())
        assertEquals(4.0, solver.getVariable("s")!!.min(), 0.00001)
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
        assertEquals("Speed", solver.getVariable("v0")!!.vectorQuantity.getDomain())
        assertEquals("Acceleration", solver.getVariable("a")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("s")!!.vectorQuantity.getDomain())
        assertNoIssues()
    }

    @Test
    fun unitsKinematic5() = testSession("ISQ") {
        loadKerML("""
            feature t: ISQ::DurationValue = 1.0 [s];
            feature v: ISQ::SpeedValue = 3.0 [m/s];
            feature g: ISQ::AccelerationValue = 4.0 [m/s^2];
            feature s: ISQ::SpeedValue  = sqrt(sqr(v)+sqr(g)*sqr(t));"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(5.0, solver.getVariable("s")!!.min(), 0.00001)
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
        assertEquals("Speed", solver.getVariable("v")!!.vectorQuantity.getDomain())
        assertEquals("Acceleration", solver.getVariable("g")!!.vectorQuantity.getDomain())
        assertEquals("Speed", solver.getVariable("s")!!.vectorQuantity.getDomain())
    }

    @Test
    fun unitsKinematic6() = testSession("ISQ") {
        loadKerML("""
            feature h: ISQ::LengthValue = 100.0 [dm];
            feature v0: ISQ::SpeedValue = 3.0 [m/s];
            feature g: ISQ::AccelerationValue = 5.0 [m/s^2];
            feature s: ISQ::LengthValue = v0*sqrt(2.0*h/g); """.trimIndent()
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(6.0, solver.getVariable("s")!!.min(), 0.00001)
        assertEquals("Length", solver.getVariable("h")!!.vectorQuantity.getDomain())
        assertEquals("Speed", solver.getVariable("v0")!!.vectorQuantity.getDomain())
        assertEquals("Acceleration", solver.getVariable("g")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("s")!!.vectorQuantity.getDomain())
    }

    @Test
    fun unitsKinematic7() = testSession("ISQ") {
        loadKerML("""
            feature r: ISQ::LengthValue = 100.0 [cm];
            feature Omega: ISQ::FrequencyValue = 3.0 [Hz];
            feature m: ISQ::MassValue = 5.0 [kg];
            feature Fz: ISQ::ForceValue = m*sqr(Omega)*r;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(45.0, solver.getVariable("Fz")!!.min(), 0.00001)
        assertEquals("Length", solver.getVariable("r")!!.vectorQuantity.getDomain())
        assertEquals("Frequency", solver.getVariable("Omega")!!.vectorQuantity.getDomain())
        assertEquals("Mass", solver.getVariable("m")!!.vectorQuantity.getDomain())
        assertEquals("Force", solver.getVariable("Fz")!!.vectorQuantity.getDomain())
        assertNoIssues()
    }

    @Test
    fun unitsKinematic8() = testSession("ISQ")  {
        loadKerML("""
            feature r: ISQ::LengthValue  = 1.0 [m];
            feature Omega: ISQ::FrequencyValue = 3.0 [Hz]; 
            feature m: ISQ::MassValue  = 5.0 [kg];
            feature Fz: ISQ::ForceValue  = m*sqr(Omega)*r; 
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(45.0, solver.getVariable("Fz")!!.min(), 0.00001)
        assertEquals("Length", solver.getVariable("r")!!.vectorQuantity.getDomain())
        assertEquals("Frequency", solver.getVariable("Omega")!!.vectorQuantity.getDomain())
        assertEquals("Mass", solver.getVariable("m")!!.vectorQuantity.getDomain())
        assertEquals("Force", solver.getVariable("Fz")!!.vectorQuantity.getDomain())
        assertNoIssues()
    }

    /**Units with Energy**/
    @Test
    fun unitsEnergy1() = testSession("ISQ") {
        loadKerML("""
            feature F: ISQ::ForceValue = 15.0 [N];
            feature s: ISQ::LengthValue = 3.0 [m];
            feature E: ISQ::EnergyValue = F*s ;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(45.0, solver.getVariable("E")!!.min(), 0.00001)
        assertEquals("Force", solver.getVariable("F")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("s")!!.vectorQuantity.getDomain())
        assertEquals("Energy", solver.getVariable("E")!!.vectorQuantity.getDomain())
        assertNoIssues()
    }

    @Test
    fun unitsEnergy2() = testSession("ISQ") {
        loadKerML("""
            feature m: ISQ::MassValue = 3.0 [kg];
            feature v: ISQ::SpeedValue  = 15.0 [m/s];
            feature p: ISQ::MomentumValue = m * v;"""

        )
        solver.propagate()
        assertNoIssues()
        assertEquals(45.0, solver.getVariable("p")!!.min(), 0.00001)
        assertEquals("Mass", solver.getVariable("m")!!.vectorQuantity.getDomain())
        assertEquals("Speed", solver.getVariable("v")!!.vectorQuantity.getDomain())
        assertEquals("Momentum", solver.getVariable("p")!!.vectorQuantity.getDomain())
    }

    /**Units with gravitation**/
    @Test
    fun unitsGravitation() = testSession("ISQ") {
        loadKerML("""
            feature gamma: Quantities::ScalarQuantityValue = 3.0 [m^3/kg s^2]{:>> range = (*..*) [m^3/kg s^2];}
            feature m1: ISQ::MassValue = 4.0 [kg];
            feature m2: ISQ::MassValue  = 2.0 [kg];
            feature r1: ISQ::LengthValue  = 2.0 [m];
            feature r2: ISQ::LengthValue = 4.0 [m];
            feature Epot: ISQ::EnergyValue = gamma * m1 * m2 * (1.0/r1 + 1.0/r2);"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(18.0, solver.getVariable("Epot")!!.min(), 0.00001)
        assertEquals("Mass", solver.getVariable("m1")!!.vectorQuantity.getDomain())
        assertEquals("Mass", solver.getVariable("m2")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("r1")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("r2")!!.vectorQuantity.getDomain())
        assertEquals("Energy", solver.getVariable("Epot")!!.vectorQuantity.getDomain())
    }

    /**Units with electricity**/
    @Test
    fun unitsElectricity1() = testSession("ISQ") {
        loadKerML("""
            feature epsilon0: Quantities::ScalarQuantityValue = 3.0 [A^2 s^2 / N m^2] {:>> range = (*..*) [A^2 s^2 / N m^2];}
            feature Q: ISQ::ElectricChargeValue = 40.0 [C];
            feature r: ISQ::LengthValue = 0.2 [m];
            feature E: ISQ::ElectricFieldStrengthValue = 1.0/(4.0 * 3.14159*epsilon0) * Q/sqr(r); 
        """)
        solver.propagate()
        assertEquals(26.5258, solver.getVariable("E")!!.min(), 0.0001)
        assertEquals("ElectricCharge", solver.getVariable("Q")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("r")!!.vectorQuantity.getDomain())
        assertEquals("ElectricFieldStrength", solver.getVariable("E")!!.vectorQuantity.getDomain())
        assertNoIssues()
    }

    @Test
    fun unitsElectricity2() = testSession("ISQ") {
        loadKerML(
            """
            feature epsilon0: Quantities::ScalarQuantityValue  = 3.0 [A^2 s^2 / N m^2]{:>> range = (*..*) [A^2 s^2 / N m^2];}
            feature Q1: ISQ::ElectricChargeValue = 40.0 [C];
            feature Q2: ISQ::ElectricChargeValue = 1.0 [C];
            feature r: ISQ::LengthValue = 0.2 [m];
            feature F: ISQ::ForceValue = 1.0/(4.0 * 3.14159*epsilon0) * (Q1*Q2)/sqr(r) ;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(26.5258, solver.getVariable("F")!!.min(), 0.0001)
        assertEquals("ElectricCharge", solver.getVariable("Q1")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCharge", solver.getVariable("Q2")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("r")!!.vectorQuantity.getDomain())
        assertEquals("Force", solver.getVariable("F")!!.vectorQuantity.getDomain())
    }

    @Test
    fun unitsElectricity3() = testSession("ISQ") {
        loadKerML(
            """feature epsilon0: Quantities::ScalarQuantityValue = 3.0 [A^2 s^2 / N m^2]{:>> range = (*..*) [A^2 s^2 / N m^2];}
            feature E: ISQ::ElectricFieldStrengthValue = 2.0 [V / m] ;
            feature q: ISQ::ElectricChargeValue = 5.0 [A s] ;
            feature s: ISQ::LengthValue = 0.2 [m];
            feature W: ISQ::EnergyValue = E*q*s ;
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("ElectricFieldStrength", solver.getVariable("E")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCharge", solver.getVariable("q")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("s")!!.vectorQuantity.getDomain())
        assertEquals("Energy", solver.getVariable("W")!!.vectorQuantity.getDomain())
        assertEquals(2.0, solver.getVariable("W")!!.min(), 0.0001)
    }

    @Test
    fun unitsElectricity5() = testSession("ISQ") {
        loadKerML("""
            feature epsilon0: Quantities::ScalarQuantityValue  = 3.0 [A^2 s^2 / N m^2]{:>> range = (*..*) [A^2 s^2 / N m^2];}
            feature E: ISQ::ElectricFieldStrengthValue = 2.0 [V / m];
            feature d: ISQ::LengthValue = 0.2 [m];
            feature U: ISQ::ElectricPotentialDifferenceValue= E*d; 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(0.4, solver.getVariable("U")!!.min(), 0.0001)
        assertEquals("ElectricFieldStrength", solver.getVariable("E")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("d")!!.vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", solver.getVariable("U")!!.vectorQuantity.getDomain())
    }

    @Test
    fun unitsElectricity6() = testSession("ISQ") {
        loadKerML("""
            feature epsilon0: Quantities::ScalarQuantityValue  = 3.0 [A^2 s^2 / N m^2]{:>> range = (*..*) [A^2 s^2 / N m^2];}
            feature Q1: ISQ::ElectricChargeValue  = 3.14159 [C];
            feature Q2: ISQ::ElectricChargeValue  = 3000.0 [mC];
            feature r1: ISQ::LengthValue  = 1.0 [m] ;
            feature r2: ISQ::LengthValue  = 50.0 [cm];
            feature W: ISQ::EnergyValue  = (Q1*Q2)/(3.0*3.14159*epsilon0)*(1.0/r1+1.0/r2); 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("W")!!.min(), 0.0001)
        assertEquals("ElectricCharge", solver.getVariable("Q1")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("r1")!!.vectorQuantity.getDomain())
        assertEquals("Energy", solver.getVariable("W")!!.vectorQuantity.getDomain())
    }


    @Test
    fun absorbedDoseTest() = testSession("ISQ") {
        loadKerML("""
                feature E: ISQ::EnergyValue = 1.0 [J];
                feature w: ISQ::MassValue = 5.0 [kg];
                feature G: ISQ::AbsorbedDoseValue = E/w;
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.2, solver.getVariable("G")!!.min(), 0.0001)
        assertEquals("m^2 / s^2", solver.getVariable("G")!!.vectorQuantity.unit.toString())
        assertEquals("AbsorbedDose", solver.getVariable("G")!!.vectorQuantity.getDomain())
    }

    @Test
    fun activityTest() = testSession("ISQ") {
        loadKerML("""
                feature t1: ISQ::DurationValue = 1.0 [s];
                feature A: ISQ::NuclearActivityValue = 1.0/t1;
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("A")!!.min(), 0.0001)
        assertEquals("1 / s", solver.getVariable("A")!!.vectorQuantity.unit.toString())
        assertEquals("NuclearActivity", solver.getVariable("A")!!.vectorQuantity.getDomain())
        assertEquals("Duration", solver.getVariable("t1")!!.vectorQuantity.getDomain())
    }

    @Test
    fun areaTest() = testSession("ISQ") {
        loadKerML("""
                feature l1: ISQ::LengthValue = 10.0 [dm];
                feature l2: ISQ::LengthValue = 50.0 [cm]{:>> range = *..* [cm];}
                feature A: ISQ::AreaValue = l1*l2;
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.5, solver.getVariable("A")!!.min(), 0.0001)
        assertEquals("m^2", solver.getVariable("A")!!.vectorQuantity.unit.toString())
        assertEquals("Area", solver.getVariable("A")!!.vectorQuantity.getDomain())
    }

    @Test
    fun capacitanceTest() = testSession("ISQ") {
        loadKerML("""
                feature Q: ISQ::ElectricChargeValue  = 10.0 [C];
                feature U: ISQ::ElectricPotentialDifferenceValue = 5.0 [V];
                feature C: ISQ::CapacitanceValue = Q/U;
            """ )
        solver.propagate()
        assertNoIssues()
        assertEquals(2.0, solver.getVariable("C")!!.min(), 0.0001)
        assertEquals("A^2 s^4 / kg m^2", solver.getVariable("C")!!.vectorQuantity.unit.toString())
        assertEquals("Capacitance", solver.getVariable("C")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCharge", solver.getVariable("Q")!!.vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", solver.getVariable("U")!!.vectorQuantity.getDomain())
    }

    @Test
    fun density() = testSession("ISQ") {
        loadKerML(
            """
            feature m: ISQ::MassValue = 2.0 [g];
            feature V: ISQ::VolumeValue = 1.0 [dm^3];
            feature d: ISQ::MassDensityValue = m/V;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(2.0, solver.getVariable("d")!!.min(), 0.0001)
        assertEquals("kg / m^3", solver.getVariable("d")!!.vectorQuantity.unit.toString())
        assertEquals("MassDensity", solver.getVariable("d")!!.vectorQuantity.getDomain())
        assertEquals("Volume", solver.getVariable("V")!!.vectorQuantity.getDomain())
        assertEquals("Mass", solver.getVariable("m")!!.vectorQuantity.getDomain())
    }

    @Test
    fun electricalConductanceTest() = testSession("ISQ") {
        loadKerML(
             """feature I: ISQ::ElectricCurrentValue  = 1000.0 [mA] ;
            feature V: ISQ::ElectricPotentialDifferenceValue= 1.0 [V] ;
            feature G: ISQ::ConductanceValue = I/V;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("G")!!.min(), 0.0001)
        assertEquals("A^2 s^3 / kg m^2", solver.getVariable("G")!!.vectorQuantity.unit.toString())
        assertEquals("Conductance", solver.getVariable("G")!!.vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", solver.getVariable("V")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", solver.getVariable("I")!!.vectorQuantity.getDomain())
    }

    @Test
    fun electricalresistanceTest() = testSession("ISQ") {
        loadKerML(
            """
            feature I: ISQ::ElectricCurrentValue = 1000.0 [mA];
            feature U: ISQ::ElectricPotentialDifferenceValue= 1.0 [V] ;
            feature R: ISQ::ResistanceValue  = U/I;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("R")!!.min(), 0.0001)
        assertEquals("kg m^2 / A^2 s^3", solver.getVariable("R")!!.vectorQuantity.unit.toString())
        assertEquals("Resistance", solver.getVariable("R")!!.vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", solver.getVariable("U")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", solver.getVariable("I")!!.vectorQuantity.getDomain())
    }

    @Test
    fun electrichargeTest() = testSession("ISQ") {
        loadKerML("""
            feature I: ISQ::ElectricCurrentValue = 1000.0 [mA];
            feature t: ISQ::DurationValue = 1.0 [s];
            feature Q: ISQ::ElectricChargeValue = t*I;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("Q")!!.min(), 0.0001)
        assertEquals("A s", solver.getVariable("Q")!!.vectorQuantity.unit.toString())
        assertEquals("ElectricCharge", solver.getVariable("Q")!!.vectorQuantity.getDomain())
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", solver.getVariable("I")!!.vectorQuantity.getDomain())
    }

    @Test
    fun electricPotentialDifferenceTest() = testSession("ISQ") {
        loadKerML(
            """
            feature I: ISQ::ElectricCurrentValue = 1000.0 [mA];
            feature P: ISQ::PowerValue = 1.0 [W];
            feature U: ISQ::ElectricPotentialDifferenceValue= P/I;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("U")!!.min(), 0.0001)
        assertEquals("kg m^2 / A s^3", solver.getVariable("U")!!.vectorQuantity.unit.toString())
        assertEquals("ElectricPotentialDifference", solver.getVariable("U")!!.vectorQuantity.getDomain())
        assertEquals("Power", solver.getVariable("P")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", solver.getVariable("I")!!.vectorQuantity.getDomain())
    }

    @Test
    fun energyTest() = testSession("ISQ") {
        loadKerML("""
                feature F: ISQ::ForceValue = 1000.0 [mN];
                feature l: ISQ::LengthValue = 1.0 [m];
                feature E: ISQ::EnergyValue = F*l;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("E")!!.min(), 0.0001)
        assertEquals("kg m^2 / s^2", solver.getVariable("E")!!.vectorQuantity.unit.toString())
        assertEquals("Energy", solver.getVariable("E")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("l")!!.vectorQuantity.getDomain())
        assertEquals("Force", solver.getVariable("F")!!.vectorQuantity.getDomain())
    }

    @Test
    fun energyDensityTest() = testSession("ISQ") {
        loadKerML("""
            feature E: ISQ::EnergyValue = 1.0 [J];
            feature V: ISQ::VolumeValue = 1.0 [m^3];
            feature ED: ISQ::EnergyDensityValue = E/V;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("ED")!!.min(), 0.0001)
        assertEquals("kg / m s^2", solver.getVariable("ED")!!.vectorQuantity.unit.toString())
        assertEquals("EnergyDensity", solver.getVariable("ED")!!.vectorQuantity.getDomain())
        assertEquals("Energy", solver.getVariable("E")!!.vectorQuantity.getDomain())
        assertEquals("Volume", solver.getVariable("V")!!.vectorQuantity.getDomain())
    }

    @Test
    fun entropyTest() = testSession("ISQ") {
        loadKerML("""
            feature E: ISQ::EnergyValue = 1.0 [J];
            feature T: ISQ::ThermodynamicTemperatureValue = 1.0 [K];
            feature S: ISQ::EntropyValue  = E/T;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("S")!!.min(), 0.0001)
        assertEquals("kg m^2 / K s^2", solver.getVariable("S")!!.vectorQuantity.unit.toString())
        assertEquals("Entropy", solver.getVariable("S")!!.vectorQuantity.getDomain())
        assertEquals("ThermodynamicTemperature", solver.getVariable("T")!!.vectorQuantity.getDomain())
        assertEquals("Energy", solver.getVariable("E")!!.vectorQuantity.getDomain())
    }

    @Test
    fun forceTest() = testSession("ISQ") {
        loadKerML(
            """feature m: ISQ::MassValue = 1.0 [kg];
            feature a: ISQ::AccelerationValue = 1.0 [m/s^2];
            feature F: ISQ::ForceValue = m*a;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("F")!!.min(), 0.0001)
        assertEquals("kg m / s^2", solver.getVariable("F")!!.vectorQuantity.unit.toString())
        assertEquals("Force", solver.getVariable("F")!!.vectorQuantity.getDomain())
        assertEquals("Acceleration", solver.getVariable("a")!!.vectorQuantity.getDomain())
        assertEquals("Mass", solver.getVariable("m")!!.vectorQuantity.getDomain())
    }

    @Test
    fun frequencyTest() = testSession("ISQ") {
        loadKerML("""
            feature t: ISQ::DurationValue = 1.0 [s];
            feature f: ISQ::FrequencyValue = 1.0/t;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("f")!!.min(), 0.0001)
        assertEquals("1 / s", solver.getVariable("f")!!.vectorQuantity.unit.toString())
        assertEquals("Frequency", solver.getVariable("f")!!.vectorQuantity.getDomain())
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
    }

    @Test
    fun flopsFrequencyTest() = testSession("ISQ") {
        loadKerML("""
            feature operations: ISQ::DimensionOneValue = 1000.0 {:>> range = *..* [FLOPs];}
            feature time: ISQ::DurationValue = 1.0 [s];
            feature flops: ISQ::FrequencyValue = operations/time {:>> range = *..* [FLOPS];}
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1000.0, solver.getVariable("flops")!!.min(), 0.0001)
        assertEquals("1 / s", solver.getVariable("flops")!!.vectorQuantity.unit.toString())
        assertEquals("Frequency", solver.getVariable("flops")!!.vectorQuantity.getDomain())
    }

    @Test
    fun illuminanceTest() = testSession("ISQ") {
        loadKerML("""
            feature I: ISQ::LuminousIntensityValue  = 1.0 [cd];
            feature A: ISQ::AreaValue = 1.0 [m^2];
            feature E: ISQ::IlluminanceValue = I/A;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("E")!!.min(), 0.0001)
        assertEquals("cd / m^2", solver.getVariable("E")!!.vectorQuantity.unit.toString())
        assertEquals("Illuminance", solver.getVariable("E")!!.vectorQuantity.getDomain())
        assertEquals("Area", solver.getVariable("A")!!.vectorQuantity.getDomain())
        assertEquals("LuminousIntensity", solver.getVariable("I")!!.vectorQuantity.getDomain())
    }

    @Test
    fun inductanceTest() = testSession("ISQ") {
        loadKerML("""
            feature I: ISQ::ElectricCurrentValue = 1000.0 [mA];
            feature W: ISQ::MagneticFluxValue = 1.0 [Wb];
            feature L: ISQ::InductanceValue = W/I;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("L")!!.min(), 0.0001)
        assertEquals("kg m^2 / A^2 s^2", solver.getVariable("L")!!.vectorQuantity.unit.toString())
        assertEquals("Inductance", solver.getVariable("L")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", solver.getVariable("I")!!.vectorQuantity.getDomain())
        assertEquals("MagneticFlux", solver.getVariable("W")!!.vectorQuantity.getDomain())
    }

    @Test
    fun kinematicViscosityTest() = testSession("ISQ") {
        loadKerML("""
                feature A: ISQ::AreaValue = 1.0 [m^2];
                feature t: ISQ::DurationValue = 1.0 [s];
                feature v: ISQ::KinematicViscosityValue = A/t;
        """ )
        solver.propagate()
        assertNoIssues()
        assertEquals(10000.0, solver.getVariable("v")!!.min(), 0.0001)
        assertEquals("m^2 / s", solver.getVariable("v")!!.vectorQuantity.unit.toString())
        assertEquals("KinematicViscosity", solver.getVariable("v")!!.vectorQuantity.getDomain())
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
        assertEquals("Area", solver.getVariable("A")!!.vectorQuantity.getDomain())
    }

    @Test
    fun luminanceTest() = testSession("ISQ") {
        loadKerML("""
            feature I: ISQ::LuminousIntensityValue = 1.0 [cd];
            feature A: ISQ::AreaValue = 1.0 [m^2];
            feature v: ISQ::LuminanceValue = I/A;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("I")!!.min(), 0.0001)
        assertEquals("cd", solver.getVariable("I")!!.vectorQuantity.unit.toString())
        assertEquals("Luminance", solver.getVariable("v")!!.vectorQuantity.getDomain())
        assertEquals("Area", solver.getVariable("A")!!.vectorQuantity.getDomain())
        assertEquals("LuminousIntensity", solver.getVariable("I")!!.vectorQuantity.getDomain())
    }

    @Test
    fun luminousEfficacyTest() = testSession("ISQ") {
        loadKerML("""
            feature P: ISQ::PowerValue = 1.0 [W];
            feature A: ISQ::LuminousFluxValue  = 1.0 [lm];
            feature K: ISQ::LuminousEfficacyValue = A/P;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("K")!!.min(), 0.0001)
        assertEquals("cd s^3 / kg m^2", solver.getVariable("K")!!.vectorQuantity.unit.toString())
        assertEquals("LuminousEfficacy", solver.getVariable("K")!!.vectorQuantity.getDomain())
        assertEquals("LuminousFlux", solver.getVariable("A")!!.vectorQuantity.getDomain())
        assertEquals("Power", solver.getVariable("P")!!.vectorQuantity.getDomain())
    }

    @Test
    fun luminousEnergyTest() = testSession("ISQ") {
        loadKerML("""
            feature t: ISQ::DurationValue = 1.0 [s];
            feature A: ISQ::LuminousFluxValue = 1.0 [lm];
            feature Q: ISQ::LuminousEnergyValue = t*A; 
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("Q")!!.min(), 0.0001)
        assertEquals("cd s", solver.getVariable("Q")!!.vectorQuantity.unit.toString())
        assertEquals("LuminousEnergy", solver.getVariable("Q")!!.vectorQuantity.getDomain())
        assertEquals("LuminousFlux", solver.getVariable("A")!!.vectorQuantity.getDomain())
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
    }

    @Test
    fun luminousFluxTest() = testSession("ISQ") {
        loadKerML("feature t: ISQ::LuminousFluxValue = 1.0 [lm];")
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("t")!!.min(), 0.0001)
        assertEquals("cd", solver.getVariable("t")!!.vectorQuantity.unit.toString())
        assertEquals("LuminousFlux", solver.getVariable("t")!!.vectorQuantity.getDomain())
    }

    @Test
    fun magneticFlux() = testSession("ISQ") {
        loadKerML("""
            feature U: ISQ::ElectricPotentialDifferenceValue= 1.0 [V] ;
            feature t: ISQ::DurationValue = 1.0 [s] ;
            feature Phi: ISQ::MagneticFluxValue = U*t;""")
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("Phi")!!.min(), 0.0001)
        assertEquals("kg m^2 / A s^2", solver.getVariable("Phi")!!.vectorQuantity.unit.toString())
        assertEquals("MagneticFlux", solver.getVariable("Phi")!!.vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", solver.getVariable("U")!!.vectorQuantity.getDomain())
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
    }

    @Test
    fun magneticFluxDensityTest() = testSession("ISQ") {
        loadKerML("""
            feature Phi: ISQ::MagneticFluxValue  = 1.0 [Wb];
            feature t: ISQ::AreaValue = 1.0 [m^2];
            feature B: ISQ::MagneticFluxDensityValue = Phi/t;""")
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("B")!!.min(), 0.0001)
        assertEquals("kg / A s^2", solver.getVariable("B")!!.vectorQuantity.unit.toString())
        assertEquals("MagneticFluxDensity", solver.getVariable("B")!!.vectorQuantity.getDomain())
        assertEquals("Area", solver.getVariable("t")!!.vectorQuantity.getDomain())
        assertEquals("MagneticFlux", solver.getVariable("Phi")!!.vectorQuantity.getDomain())
    }

    @Test
    fun massFlowTest() = testSession("ISQ") {
        loadKerML("""
            feature m: ISQ::MassValue = 1.0 [kg];
            feature t: ISQ::DurationValue = 1.0 [s];
            feature B: ISQ::MassFlowValue = m/t;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("B")!!.min(), 0.0001)
        assertEquals("kg / s", solver.getVariable("B")!!.vectorQuantity.unit.toString())
        assertEquals("MassFlow", solver.getVariable("B")!!.vectorQuantity.getDomain())
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
        assertEquals("Mass", solver.getVariable("m")!!.vectorQuantity.getDomain())
    }

    @Test
    fun momentOfForceTest() = testSession("ISQ") {
        loadKerML(
           """
            feature l: ISQ::LengthValue  = 1.0 [m];
            feature F: ISQ::ForceValue = 1.0 [N];
            feature B: ISQ::MomentOfForceValue = l*F;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("B")!!.min(), 0.0001)
        assertEquals("kg m^2 / s^2", solver.getVariable("B")!!.vectorQuantity.unit.toString())
        assertEquals("MomentOfForce", solver.getVariable("B")!!.vectorQuantity.getDomain())
        assertEquals("Force", solver.getVariable("F")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("l")!!.vectorQuantity.getDomain())
    }

    @Test
    fun momentOfInertiaTest() = testSession("ISQ") {
        loadKerML(
            """
            feature m: ISQ::MassValue = 1.0 [kg];
            feature A: ISQ::AreaValue = 1.0 [m^2];
            feature I: ISQ::MomentOfInertiaValue = m*A;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("I")!!.min(), 0.0001)
        assertEquals("kg m^2", solver.getVariable("I")!!.vectorQuantity.unit.toString())
        assertEquals("MomentOfInertia", solver.getVariable("I")!!.vectorQuantity.getDomain())
        assertEquals("Area", solver.getVariable("A")!!.vectorQuantity.getDomain())
        assertEquals("Mass", solver.getVariable("m")!!.vectorQuantity.getDomain())
    }

    @Test
    fun momentumTest() = testSession("ISQ") {
        loadKerML(
            """
            feature m: ISQ::MassValue = 1.0 [kg];
            feature v: ISQ::SpeedValue = 1.0 [m/s];
            feature p: ISQ::MomentumValue = m*v;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("p")!!.min(), 0.0001)
        assertEquals("kg m / s", solver.getVariable("p")!!.vectorQuantity.unit.toString())
        assertEquals("Momentum", solver.getVariable("p")!!.vectorQuantity.getDomain())
        assertEquals("Speed", solver.getVariable("v")!!.vectorQuantity.getDomain())
        assertEquals("Mass", solver.getVariable("m")!!.vectorQuantity.getDomain())
    }

    @Test
    fun permittivityTest() = testSession("ISQ") {
        loadKerML(
            """
            feature I: ISQ::CapacitanceValue = 1.0 [F];
            feature l: ISQ::LengthValue = 1.0 [m];
            feature epsilon: ISQ::PermittivityValue = I/l;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("epsilon")!!.min(), 0.0001)
        assertEquals("A^2 s^4 / kg m^3", solver.getVariable("epsilon")!!.vectorQuantity.unit.toString())
        assertEquals("Permittivity", solver.getVariable("epsilon")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("l")!!.vectorQuantity.getDomain())
        assertEquals("Capacitance", solver.getVariable("I")!!.vectorQuantity.getDomain())
    }

    @Test
    fun powerTest() = testSession("ISQ") {
        loadKerML(
            """
            feature E: ISQ::EnergyValue  = 1.0 [J];
            feature t: ISQ::DurationValue = 1.0 [s]; 
            feature P: ISQ::PowerValue = E/t; 
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("P")!!.min(), 0.0001)
        assertEquals("kg m^2 / s^3", solver.getVariable("P")!!.vectorQuantity.unit.toString())
        assertEquals("Power", solver.getVariable("P")!!.vectorQuantity.getDomain())
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
        assertEquals("Energy", solver.getVariable("E")!!.vectorQuantity.getDomain())
    }

    @Test
    fun pressureTest() = testSession("ISQ") {
        loadKerML("""
            feature F: ISQ::ForceValue  = 1.0 [N];
            feature A: ISQ::AreaValue = 1.0 [m^2];
            feature p: ISQ::PressureValue  = F/A;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("p")!!.min(), 0.0001)
        assertEquals("kg / m s^2", solver.getVariable("p")!!.vectorQuantity.unit.toString())
        assertEquals("Pressure", solver.getVariable("p")!!.vectorQuantity.getDomain())
        assertEquals("Area", solver.getVariable("A")!!.vectorQuantity.getDomain())
        assertEquals("Force", solver.getVariable("F")!!.vectorQuantity.getDomain())
    }

    @Test
    fun pressureTest2() = testSession("ISQ") {
        loadKerML("""
            feature F: ISQ::ForceValue = 1.0 [N];
            feature A: ISQ::AreaValue = 1.0 [m^2];
            feature p: ISQ::PressureValue = F/A {:>> range = (*..*) [mbar];}
            feature p2: ISQ::PressureValue = F/A;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("p2")!!.min(), 0.0001)
        assertEquals(0.01, solver.getVariable("p")!!.min(), 0.0001)
        assertEquals("kg / m s^2", solver.getVariable("p")!!.vectorQuantity.unit.toString())
        assertEquals("Pressure", solver.getVariable("p")!!.vectorQuantity.getDomain())
        assertEquals("Area", solver.getVariable("A")!!.vectorQuantity.getDomain())
        assertEquals("Force", solver.getVariable("F")!!.vectorQuantity.getDomain())
    }

    @Test
    fun quantityOfDomainOne() = testSession("ISQ") {
        loadKerML("""
            feature E: ScalarValues::Real = 100.0;
            feature p: Quantities::ScalarQuantityValue  = E {:>> range = (*..*) [%];}
            feature E2: ScalarValues::Real = p;
            feature f: Quantities::ScalarQuantityValue  = E {:>> range = (*..*) [dB];}
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(10000.0, solver.getVariable("p")!!.min(), 0.0001)
        assertEquals(20.0, solver.getVariable("f")!!.min(), 0.0001)
        assertEquals(100.0, solver.getVariable("E2")!!.min(), 0.0001)
        assertEquals("1", solver.getVariable("p")!!.vectorQuantity.unit.toString())
        assertEquals("DimensionOne", solver.getVariable("p")!!.vectorQuantity.getDomain())
        assertEquals("DimensionOne", solver.getVariable("E")!!.vectorQuantity.getDomain())
        assertEquals("DimensionOne", solver.getVariable("E2")!!.vectorQuantity.getDomain())
        assertEquals("DimensionOne", solver.getVariable("f")!!.vectorQuantity.getDomain())
    }

    @Test
    fun flopsTest() = testSession("ISQ") {
        loadKerML("""
            feature operations: ScalarValues::Real = 1000.0;
            feature flops: Quantities::ScalarQuantityValue = operations {:>> range = *..* [FLOPs];}
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1000.0, solver.getVariable("flops")!!.min(), 0.0001)
        assertEquals("1", solver.getVariable("flops")!!.vectorQuantity.unit.toString())
        assertEquals("DimensionOne", solver.getVariable("flops")!!.vectorQuantity.getDomain())
    }

    @Test
    fun speedTest() = testSession("ISQ") {
        loadKerML("""
            feature l: ISQ::LengthValue = 10.0 [m] ;
            feature t: ISQ::DurationValue = 1.0 [s];
            feature v1: ISQ::SpeedValue = l/t;
            feature v2: ISQ::SpeedValue = l/t{:>> range = (*..*) [km/h];}
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(10.0, solver.getVariable("v1")!!.min(), 0.0001)
        assertEquals(36.0, solver.getVariable("v2")!!.min(), 0.0001)
        assertEquals("m / s", solver.getVariable("v1")!!.vectorQuantity.unit.toString())
        assertEquals("Speed", solver.getVariable("v1")!!.vectorQuantity.getDomain())
        assertEquals("Speed", solver.getVariable("v2")!!.vectorQuantity.getDomain())
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("l")!!.vectorQuantity.getDomain())
    }

    @Test
    fun volumeTest() = testSession("ISQ") {
        loadKerML("""
            feature l1: ISQ::LengthValue = 1.0 [m];
            feature l2: ISQ::LengthValue = 100.0 [cm];
            feature l3: ISQ::LengthValue = 10.0 [dm];
            feature V1: ISQ::VolumeValue = l1*l2*l3;
            feature V2: ISQ::VolumeValue = l1*l2*l3 {:>> range = (*..*) [l];}
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("V1")!!.min(), 0.0001)
        assertEquals(1000.0, solver.getVariable("V2")!!.min(), 0.0001)
        assertEquals("m^3", solver.getVariable("V1")!!.vectorQuantity.unit.toString())
        assertEquals("Volume", solver.getVariable("V1")!!.vectorQuantity.getDomain())
        assertEquals("Volume", solver.getVariable("V2")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("l1")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("l2")!!.vectorQuantity.getDomain())
        assertEquals("Length", solver.getVariable("l3")!!.vectorQuantity.getDomain())
    }

    @Test
    fun informationCapacityTest() = testSession("ISQ") {
        loadKerML("""
            feature i1: ISQ::StorageCapacityValue  = 5000000.0 [bit];
            feature i2: ISQ::StorageCapacityValue  = 30.0 [kB];
            feature t: ISQ::DurationValue  = 10.0 [s];
            feature BR1: ISQ::BitRateValue = i1/t { :>> range = (*..*) [Mbps];}
            feature BR2: ISQ::BitRateValue = i2/t { :>> range = (*..*) [kB/s];}
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(0.5, solver.getVariable("BR1")!!.min(), 0.0001)
        assertEquals(3.0, solver.getVariable("BR2")!!.min(), 0.0001)
        assertEquals("StorageCapacity", solver.getVariable("i1")!!.vectorQuantity.getDomain())
        assertEquals("StorageCapacity", solver.getVariable("i2")!!.vectorQuantity.getDomain())
        assertEquals("Duration", solver.getVariable("t")!!.vectorQuantity.getDomain())
        assertEquals("BitRate", solver.getVariable("BR1")!!.vectorQuantity.getDomain())
        assertEquals("BitRate", solver.getVariable("BR2")!!.vectorQuantity.getDomain())
    }

    @Test
    fun multipleOperationsTest() = testSession("ISQ") {
        loadKerML("""
              private import ISQ::*;       
              feature plugCosts: ISQ::DurationValue = 2.0 [s]; 
            """)
        solver.propagate()
        assertNoIssues()
    }

    @Test
    fun defineDomainTest1() = testSession("ISQ") {
        loadKerML("""  
             feature Mass: ISQ::MassValue = 10.0 [kg];
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals("Mass", solver.getVariable("Mass")!!.vectorQuantity.unit.unitDomain)
    }

    @Test
    fun defineDomainTest2() = testSession("ISQ") {
        loadKerML("""
            feature Mass: ISQ::MassValue = 10.0 [kg];
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals("kg", solver.getVariable("Mass")!!.vectorQuantity.unit.toString())
    }

    @Test
    fun defineDomainTestWrongUnit() = testSession("ISQ") {
        loadKerML("""
            feature Mass: ISQ::MassValue = 10.0 [m]; // No match
        """)
        solver.propagate()
        assertIssue("Unit")
    }
}
