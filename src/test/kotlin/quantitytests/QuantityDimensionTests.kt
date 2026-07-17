package quantitytests

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
        assertEquals("m", global.resolveVar("s")!!.vectorQuantity.unit.toString())
        assertEquals(10.0, global.resolveVar("s")!!.max(), 0.00001)
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
        assertEquals("Speed", global.resolveVar("v")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDomain())
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
        assertEquals("m / s", global.resolveVar("v2")!!.vectorQuantity.unit.toString())
        assertEquals(10.0, global.resolveVar("v2")!!.min(), 0.00001)
        assertEquals(20.0, global.resolveVar("s")!!.max(), 0.00001)
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
        assertEquals("Speed", global.resolveVar("v")!!.vectorQuantity.getDomain())
        assertEquals("Speed", global.resolveVar("v2")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDomain())
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
        assertEquals("m / s", global.resolveVar("v")!!.vectorQuantity.unit.toString())
        assertEquals(2.0, global.resolveVar("t")!!.min(), 0.00001)
        assertEquals(10.0, global.resolveVar("v")!!.max(), 0.00001)
        assertEquals(20.0, global.resolveVar("s")!!.min(), 0.00001)
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
        assertEquals("Speed", global.resolveVar("v")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDomain())
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
        assertEquals("m / s", global.resolveVar("v0")!!.vectorQuantity.unit.toString())
        assertEquals(4.0, global.resolveVar("s")!!.min(), 0.00001)
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
        assertEquals("Speed", global.resolveVar("v0")!!.vectorQuantity.getDomain())
        assertEquals("Acceleration", global.resolveVar("a")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDomain())
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
        assertEquals(5.0, global.resolveVar("s")!!.min(), 0.00001)
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
        assertEquals("Speed", global.resolveVar("v")!!.vectorQuantity.getDomain())
        assertEquals("Acceleration", global.resolveVar("g")!!.vectorQuantity.getDomain())
        assertEquals("Speed", global.resolveVar("s")!!.vectorQuantity.getDomain())
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
        assertEquals(6.0, global.resolveVar("s")!!.min(), 0.00001)
        assertEquals("Length", global.resolveVar("h")!!.vectorQuantity.getDomain())
        assertEquals("Speed", global.resolveVar("v0")!!.vectorQuantity.getDomain())
        assertEquals("Acceleration", global.resolveVar("g")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDomain())
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
        assertEquals(45.0, global.resolveVar("Fz")!!.min(), 0.00001)
        assertEquals("Length", global.resolveVar("r")!!.vectorQuantity.getDomain())
        assertEquals("Frequency", global.resolveVar("Omega")!!.vectorQuantity.getDomain())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDomain())
        assertEquals("Force", global.resolveVar("Fz")!!.vectorQuantity.getDomain())
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
        assertEquals(45.0, global.resolveVar("Fz")!!.min(), 0.00001)
        assertEquals("Length", global.resolveVar("r")!!.vectorQuantity.getDomain())
        assertEquals("Frequency", global.resolveVar("Omega")!!.vectorQuantity.getDomain())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDomain())
        assertEquals("Force", global.resolveVar("Fz")!!.vectorQuantity.getDomain())
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
        assertEquals(45.0, global.resolveVar("E")!!.min(), 0.00001)
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDomain())
        assertEquals("Energy", global.resolveVar("E")!!.vectorQuantity.getDomain())
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
        assertEquals(45.0, global.resolveVar("p")!!.min(), 0.00001)
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDomain())
        assertEquals("Speed", global.resolveVar("v")!!.vectorQuantity.getDomain())
        assertEquals("Momentum", global.resolveVar("p")!!.vectorQuantity.getDomain())
    }

    /**Units with gravitation**/
    @Test
    fun unitsGravitation() = testSession("ISQ") {
        loadKerML("""
            feature gamma: Quantities::ScalarQuantityValue = 3.0 [m^3/kg s^2]{:>> unit = "m^3/kg s^2";}
            feature m1: ISQ::MassValue = 4.0 [kg];
            feature m2: ISQ::MassValue  = 2.0 [kg];
            feature r1: ISQ::LengthValue  = 2.0 [m];
            feature r2: ISQ::LengthValue = 4.0 [m];
            feature Epot: ISQ::EnergyValue = gamma * m1 * m2 * (1.0/r1 + 1.0/r2);"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(18.0, global.resolveVar("Epot")!!.min(), 0.00001)
        assertEquals("Mass", global.resolveVar("m1")!!.vectorQuantity.getDomain())
        assertEquals("Mass", global.resolveVar("m2")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("r1")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("r2")!!.vectorQuantity.getDomain())
        assertEquals("Energy", global.resolveVar("Epot")!!.vectorQuantity.getDomain())
    }

    /**Units with electricity**/
    @Test
    fun unitsElectricity1() = testSession("ISQ") {
        loadKerML(
            """
            feature epsilon0: Quantities::ScalarQuantityValue = 3.0 [A^2 s^2 / N m^2]{:>> unit = "A^2 s^2 / N m^2";}
            feature Q: ISQ::ElectricChargeValue = 40.0 [C];
            feature r: ISQ::LengthValue = 0.2 [m];
            feature E: ISQ::ElectricFieldStrengthValue = 1.0/(4.0 * 3.14159*epsilon0) * Q/sqr(r); """
        )
        solver.propagate()
        assertEquals(26.5258, global.resolveVar("E")!!.min(), 0.0001)
        assertEquals("ElectricCharge", global.resolveVar("Q")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("r")!!.vectorQuantity.getDomain())
        assertEquals("ElectricFieldStrength", global.resolveVar("E")!!.vectorQuantity.getDomain())
        assertNoIssues()
    }

    @Test
    fun unitsElectricity2() = testSession("ISQ") {
        loadKerML(
            """
            feature epsilon0: Quantities::ScalarQuantityValue  = 3.0 [A^2 s^2 / N m^2]{:>> unit = "A^2 s^2 / N m^2";}
            feature Q1: ISQ::ElectricChargeValue = 40.0 [C];
            feature Q2: ISQ::ElectricChargeValue = 1.0 [C];
            feature r: ISQ::LengthValue = 0.2 [m];
            feature F: ISQ::ForceValue = 1.0/(4.0 * 3.14159*epsilon0) * (Q1*Q2)/sqr(r) ;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(26.5258, global.resolveVar("F")!!.min(), 0.0001)
        assertEquals("ElectricCharge", global.resolveVar("Q1")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCharge", global.resolveVar("Q2")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("r")!!.vectorQuantity.getDomain())
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDomain())
    }

    @Test
    fun unitsElectricity3() = testSession("ISQ") {
        loadKerML(
            """feature epsilon0: Quantities::ScalarQuantityValue = 3.0 [A^2 s^2 / N m^2]{:>> unit = "A^2 s^2 / N m^2";}
            feature E: ISQ::ElectricFieldStrengthValue = 2.0 [V / m] ;
            feature q: ISQ::ElectricChargeValue = 5.0 [A s] ;
            feature s: ISQ::LengthValue = 0.2 [m];
            feature W: ISQ::EnergyValue = E*q*s ;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals("ElectricFieldStrength", global.resolveVar("E")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCharge", global.resolveVar("q")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDomain())
        assertEquals("Energy", global.resolveVar("W")!!.vectorQuantity.getDomain())
        assertEquals(2.0, global.resolveVar("W")!!.min(), 0.0001)
    }

    @Test
    fun unitsElectricity5() = testSession("ISQ") {
        loadKerML(
            """feature epsilon0: Quantities::ScalarQuantityValue  = 3.0 [A^2 s^2 / N m^2]{:>> unit = "A^2 s^2 / N m^2";}
                feature E: ISQ::ElectricFieldStrengthValue = 2.0 [V / m];
                feature d: ISQ::LengthValue = 0.2 [m];
                feature U: ISQ::ElectricPotentialDifferenceValue= E*d; """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(0.4, global.resolveVar("U")!!.min(), 0.0001)
        assertEquals("ElectricFieldStrength", global.resolveVar("E")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("d")!!.vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", global.resolveVar("U")!!.vectorQuantity.getDomain())
    }

    @Test
    fun unitsElectricity6() = testSession("ISQ") {
        loadKerML(
            """
            feature epsilon0: Quantities::ScalarQuantityValue  = 3.0 [A^2 s^2 / N m^2]{:>> unit = "A^2 s^2 / N m^2";}
            feature Q1: ISQ::ElectricChargeValue  = 3.14159 [C];
            feature Q2: ISQ::ElectricChargeValue  = 3000.0 [mC];
            feature r1: ISQ::LengthValue  = 1.0 [m] ;
            feature r2: ISQ::LengthValue  = 50.0 [cm];
            feature W: ISQ::EnergyValue  = (Q1*Q2)/(3.0*3.14159*epsilon0)*(1.0/r1+1.0/r2); """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("W")!!.min(), 0.0001)
        assertEquals("ElectricCharge", global.resolveVar("Q1")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("r1")!!.vectorQuantity.getDomain())
        assertEquals("Energy", global.resolveVar("W")!!.vectorQuantity.getDomain())
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
        assertEquals(0.2, global.resolveVar("G")!!.min(), 0.0001)
        assertEquals("m^2 / s^2", global.resolveVar("G")!!.vectorQuantity.unit.toString())
        assertEquals("AbsorbedDose", global.resolveVar("G")!!.vectorQuantity.getDomain())
    }

    @Test
    fun activityTest() = testSession("ISQ") {
        loadKerML("""
                feature t1: ISQ::DurationValue = 1.0 [s];
                feature A: ISQ::NuclearActivityValue = 1.0/t1;
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("A")!!.min(), 0.0001)
        assertEquals("1 / s", global.resolveVar("A")!!.vectorQuantity.unit.toString())
        assertEquals("NuclearActivity", global.resolveVar("A")!!.vectorQuantity.getDomain())
        assertEquals("Duration", global.resolveVar("t1")!!.vectorQuantity.getDomain())
    }

    @Test
    fun areaTest() = testSession("ISQ") {
        loadKerML("""
                feature l1: ISQ::LengthValue = 10.0 [dm];
                feature l2: ISQ::LengthValue = 50.0 [cm]{:>> unit = "cm";}
                feature A: ISQ::AreaValue = l1*l2;
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.5, global.resolveVar("A")!!.min(), 0.0001)
        assertEquals("m^2", global.resolveVar("A")!!.vectorQuantity.unit.toString())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDomain())
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
        assertEquals(2.0, global.resolveVar("C")!!.min(), 0.0001)
        assertEquals("A^2 s^4 / kg m^2", global.resolveVar("C")!!.vectorQuantity.unit.toString())
        assertEquals("Capacitance", global.resolveVar("C")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCharge", global.resolveVar("Q")!!.vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", global.resolveVar("U")!!.vectorQuantity.getDomain())
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
        assertEquals(2.0, global.resolveVar("d")!!.min(), 0.0001)
        assertEquals("kg / m^3", global.resolveVar("d")!!.vectorQuantity.unit.toString())
        assertEquals("MassDensity", global.resolveVar("d")!!.vectorQuantity.getDomain())
        assertEquals("Volume", global.resolveVar("V")!!.vectorQuantity.getDomain())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("G")!!.min(), 0.0001)
        assertEquals("A^2 s^3 / kg m^2", global.resolveVar("G")!!.vectorQuantity.unit.toString())
        assertEquals("Conductance", global.resolveVar("G")!!.vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", global.resolveVar("V")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", global.resolveVar("I")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("R")!!.min(), 0.0001)
        assertEquals("kg m^2 / A^2 s^3", global.resolveVar("R")!!.vectorQuantity.unit.toString())
        assertEquals("Resistance", global.resolveVar("R")!!.vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", global.resolveVar("U")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", global.resolveVar("I")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("Q")!!.min(), 0.0001)
        assertEquals("A s", global.resolveVar("Q")!!.vectorQuantity.unit.toString())
        assertEquals("ElectricCharge", global.resolveVar("Q")!!.vectorQuantity.getDomain())
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", global.resolveVar("I")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("U")!!.min(), 0.0001)
        assertEquals("kg m^2 / A s^3", global.resolveVar("U")!!.vectorQuantity.unit.toString())
        assertEquals("ElectricPotentialDifference", global.resolveVar("U")!!.vectorQuantity.getDomain())
        assertEquals("Power", global.resolveVar("P")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", global.resolveVar("I")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("E")!!.min(), 0.0001)
        assertEquals("kg m^2 / s^2", global.resolveVar("E")!!.vectorQuantity.unit.toString())
        assertEquals("Energy", global.resolveVar("E")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("l")!!.vectorQuantity.getDomain())
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("ED")!!.min(), 0.0001)
        assertEquals("kg / m s^2", global.resolveVar("ED")!!.vectorQuantity.unit.toString())
        assertEquals("EnergyDensity", global.resolveVar("ED")!!.vectorQuantity.getDomain())
        assertEquals("Energy", global.resolveVar("E")!!.vectorQuantity.getDomain())
        assertEquals("Volume", global.resolveVar("V")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("S")!!.min(), 0.0001)
        assertEquals("kg m^2 / K s^2", global.resolveVar("S")!!.vectorQuantity.unit.toString())
        assertEquals("Entropy", global.resolveVar("S")!!.vectorQuantity.getDomain())
        assertEquals("ThermodynamicTemperature", global.resolveVar("T")!!.vectorQuantity.getDomain())
        assertEquals("Energy", global.resolveVar("E")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("F")!!.min(), 0.0001)
        assertEquals("kg m / s^2", global.resolveVar("F")!!.vectorQuantity.unit.toString())
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDomain())
        assertEquals("Acceleration", global.resolveVar("a")!!.vectorQuantity.getDomain())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDomain())
    }

    @Test
    fun frequencyTest() = testSession("ISQ") {
        loadKerML("""
            feature t: ISQ::DurationValue = 1.0 [s];
            feature f: ISQ::FrequencyValue = 1.0/t;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("f")!!.min(), 0.0001)
        assertEquals("1 / s", global.resolveVar("f")!!.vectorQuantity.unit.toString())
        assertEquals("Frequency", global.resolveVar("f")!!.vectorQuantity.getDomain())
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
    }

    @Test
    fun flopsFrequencyTest() = testSession("ISQ") {
        loadKerML("""
            feature operations: ISQ::DimensionOneValue = 1000.0 {:>> unit = "FLOPs";}
            feature time: ISQ::DurationValue = 1.0 [s];
            feature flops: ISQ::FrequencyValue = operations/time {:>> unit = "FLOPS";}
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1000.0, global.resolveVar("flops")!!.min(), 0.0001)
        assertEquals("1 / s", global.resolveVar("flops")!!.vectorQuantity.unit.toString())
        assertEquals("Frequency", global.resolveVar("flops")!!.vectorQuantity.getDomain())
    }

    @Test
    fun illuminanceTest() = testSession("ISQ") {
        loadKerML(
            """
            feature I: ISQ::LuminousIntensityValue  = 1.0 [cd];
            feature A: ISQ::AreaValue = 1.0 [m^2];
            feature E: ISQ::IlluminanceValue = I/A;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("E")!!.min(), 0.0001)
        assertEquals("cd / m^2", global.resolveVar("E")!!.vectorQuantity.unit.toString())
        assertEquals("Illuminance", global.resolveVar("E")!!.vectorQuantity.getDomain())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDomain())
        assertEquals("LuminousIntensity", global.resolveVar("I")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("L")!!.min(), 0.0001)
        assertEquals("kg m^2 / A^2 s^2", global.resolveVar("L")!!.vectorQuantity.unit.toString())
        assertEquals("Inductance", global.resolveVar("L")!!.vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", global.resolveVar("I")!!.vectorQuantity.getDomain())
        assertEquals("MagneticFlux", global.resolveVar("W")!!.vectorQuantity.getDomain())
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
        assertEquals(10000.0, global.resolveVar("v")!!.min(), 0.0001)
        assertEquals("m^2 / s", global.resolveVar("v")!!.vectorQuantity.unit.toString())
        assertEquals("KinematicViscosity", global.resolveVar("v")!!.vectorQuantity.getDomain())
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("I")!!.min(), 0.0001)
        assertEquals("cd", global.resolveVar("I")!!.vectorQuantity.unit.toString())
        assertEquals("Luminance", global.resolveVar("v")!!.vectorQuantity.getDomain())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDomain())
        assertEquals("LuminousIntensity", global.resolveVar("I")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("K")!!.min(), 0.0001)
        assertEquals("cd s^3 / kg m^2", global.resolveVar("K")!!.vectorQuantity.unit.toString())
        assertEquals("LuminousEfficacy", global.resolveVar("K")!!.vectorQuantity.getDomain())
        assertEquals("LuminousFlux", global.resolveVar("A")!!.vectorQuantity.getDomain())
        assertEquals("Power", global.resolveVar("P")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("Q")!!.min(), 0.0001)
        assertEquals("cd s", global.resolveVar("Q")!!.vectorQuantity.unit.toString())
        assertEquals("LuminousEnergy", global.resolveVar("Q")!!.vectorQuantity.getDomain())
        assertEquals("LuminousFlux", global.resolveVar("A")!!.vectorQuantity.getDomain())
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
    }

    @Test
    fun luminousFluxTest() = testSession("ISQ") {
        loadKerML("feature t: ISQ::LuminousFluxValue = 1.0 [lm];")
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("t")!!.min(), 0.0001)
        assertEquals("cd", global.resolveVar("t")!!.vectorQuantity.unit.toString())
        assertEquals("LuminousFlux", global.resolveVar("t")!!.vectorQuantity.getDomain())
    }

    @Test
    fun magneticFlux() = testSession("ISQ") {
        loadKerML("""
            feature U: ISQ::ElectricPotentialDifferenceValue= 1.0 [V] ;
            feature t: ISQ::DurationValue = 1.0 [s] ;
            feature Phi: ISQ::MagneticFluxValue = U*t;""")
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("Phi")!!.min(), 0.0001)
        assertEquals("kg m^2 / A s^2", global.resolveVar("Phi")!!.vectorQuantity.unit.toString())
        assertEquals("MagneticFlux", global.resolveVar("Phi")!!.vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", global.resolveVar("U")!!.vectorQuantity.getDomain())
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
    }

    @Test
    fun magneticFluxDensityTest() = testSession("ISQ") {
        loadKerML("""
            feature Phi: ISQ::MagneticFluxValue  = 1.0 [Wb];
            feature t: ISQ::AreaValue = 1.0 [m^2];
            feature B: ISQ::MagneticFluxDensityValue = Phi/t;""")
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("B")!!.min(), 0.0001)
        assertEquals("kg / A s^2", global.resolveVar("B")!!.vectorQuantity.unit.toString())
        assertEquals("MagneticFluxDensity", global.resolveVar("B")!!.vectorQuantity.getDomain())
        assertEquals("Area", global.resolveVar("t")!!.vectorQuantity.getDomain())
        assertEquals("MagneticFlux", global.resolveVar("Phi")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("B")!!.min(), 0.0001)
        assertEquals("kg / s", global.resolveVar("B")!!.vectorQuantity.unit.toString())
        assertEquals("MassFlow", global.resolveVar("B")!!.vectorQuantity.getDomain())
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("B")!!.min(), 0.0001)
        assertEquals("kg m^2 / s^2", global.resolveVar("B")!!.vectorQuantity.unit.toString())
        assertEquals("MomentOfForce", global.resolveVar("B")!!.vectorQuantity.getDomain())
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("l")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("I")!!.min(), 0.0001)
        assertEquals("kg m^2", global.resolveVar("I")!!.vectorQuantity.unit.toString())
        assertEquals("MomentOfInertia", global.resolveVar("I")!!.vectorQuantity.getDomain())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDomain())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("p")!!.min(), 0.0001)
        assertEquals("kg m / s", global.resolveVar("p")!!.vectorQuantity.unit.toString())
        assertEquals("Momentum", global.resolveVar("p")!!.vectorQuantity.getDomain())
        assertEquals("Speed", global.resolveVar("v")!!.vectorQuantity.getDomain())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("epsilon")!!.min(), 0.0001)
        assertEquals("A^2 s^4 / kg m^3", global.resolveVar("epsilon")!!.vectorQuantity.unit.toString())
        assertEquals("Permittivity", global.resolveVar("epsilon")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("l")!!.vectorQuantity.getDomain())
        assertEquals("Capacitance", global.resolveVar("I")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("P")!!.min(), 0.0001)
        assertEquals("kg m^2 / s^3", global.resolveVar("P")!!.vectorQuantity.unit.toString())
        assertEquals("Power", global.resolveVar("P")!!.vectorQuantity.getDomain())
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
        assertEquals("Energy", global.resolveVar("E")!!.vectorQuantity.getDomain())
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
        assertEquals(1.0, global.resolveVar("p")!!.min(), 0.0001)
        assertEquals("kg / m s^2", global.resolveVar("p")!!.vectorQuantity.unit.toString())
        assertEquals("Pressure", global.resolveVar("p")!!.vectorQuantity.getDomain())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDomain())
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDomain())
    }

    @Test
    fun pressureTest2() = testSession("ISQ") {
        loadKerML("""
            feature F: ISQ::ForceValue = 1.0 [N];
            feature A: ISQ::AreaValue = 1.0 [m^2];
            feature p: ISQ::PressureValue = F/A {:>> unit = "mbar";}
            feature p2: ISQ::PressureValue = F/A;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("p2")!!.min(), 0.0001)
        assertEquals(0.01, global.resolveVar("p")!!.min(), 0.0001)
        assertEquals("kg / m s^2", global.resolveVar("p")!!.vectorQuantity.unit.toString())
        assertEquals("Pressure", global.resolveVar("p")!!.vectorQuantity.getDomain())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDomain())
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDomain())
    }

    @Test
    fun quantityOfDomainOne() = testSession("ISQ") {
        loadKerML("""
            feature E: ScalarValues::Real = 100.0;
            feature p: Quantities::ScalarQuantityValue  = E {:>> unit = "%";}
            feature E2: ScalarValues::Real = p;
            feature f: Quantities::ScalarQuantityValue  = E {:>> unit = "dB";}
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(10000.0, global.resolveVar("p")!!.min(), 0.0001)
        assertEquals(20.0, global.resolveVar("f")!!.min(), 0.0001)
        assertEquals(100.0, global.resolveVar("E2")!!.min(), 0.0001)
        assertEquals("1", global.resolveVar("p")!!.vectorQuantity.unit.toString())
        assertEquals("DimensionOne", global.resolveVar("p")!!.vectorQuantity.getDomain())
        assertEquals("DimensionOne", global.resolveVar("E")!!.vectorQuantity.getDomain())
        assertEquals("DimensionOne", global.resolveVar("E2")!!.vectorQuantity.getDomain())
        assertEquals("DimensionOne", global.resolveVar("f")!!.vectorQuantity.getDomain())
    }

    @Test
    fun flopsTest() = testSession("ISQ") {
        loadKerML("""
            feature operations: ScalarValues::Real = 1000.0;
            feature flops: Quantities::ScalarQuantityValue = operations {:>> unit = "FLOPs";}
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1000.0, global.resolveVar("flops")!!.min(), 0.0001)
        assertEquals("1", global.resolveVar("flops")!!.vectorQuantity.unit.toString())
        assertEquals("DimensionOne", global.resolveVar("flops")!!.vectorQuantity.getDomain())
    }

    @Test
    fun speedTest() = testSession("ISQ") {
        loadKerML("""
            feature l: ISQ::LengthValue = 10.0 [m] ;
            feature t: ISQ::DurationValue = 1.0 [s];
            feature v1: ISQ::SpeedValue = l/t;
            feature v2: ISQ::SpeedValue = l/t{:>> unit = "km/h";}
            """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(10.0, global.resolveVar("v1")!!.min(), 0.0001)
        assertEquals(36.0, global.resolveVar("v2")!!.min(), 0.0001)
        assertEquals("m / s", global.resolveVar("v1")!!.vectorQuantity.unit.toString())
        assertEquals("Speed", global.resolveVar("v1")!!.vectorQuantity.getDomain())
        assertEquals("Speed", global.resolveVar("v2")!!.vectorQuantity.getDomain())
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("l")!!.vectorQuantity.getDomain())
    }

    @Test
    fun volumeTest() = testSession("ISQ") {
        loadKerML("""
            feature l1: ISQ::LengthValue = 1.0 [m];
            feature l2: ISQ::LengthValue = 100.0 [cm];
            feature l3: ISQ::LengthValue = 10.0 [dm];
            feature V1: ISQ::VolumeValue = l1*l2*l3;
            feature V2: ISQ::VolumeValue = l1*l2*l3 {:>> unit = "l";}
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("V1")!!.min(), 0.0001)
        assertEquals(1000.0, global.resolveVar("V2")!!.min(), 0.0001)
        assertEquals("m^3", global.resolveVar("V1")!!.vectorQuantity.unit.toString())
        assertEquals("Volume", global.resolveVar("V1")!!.vectorQuantity.getDomain())
        assertEquals("Volume", global.resolveVar("V2")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("l1")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("l2")!!.vectorQuantity.getDomain())
        assertEquals("Length", global.resolveVar("l3")!!.vectorQuantity.getDomain())
    }

    @Test
    fun informationCapacityTest() = testSession("ISQ") {
        loadKerML(
            """
            feature i1: ISQ::StorageCapacityValue  = 5000000.0 [bit];
            feature i2: ISQ::StorageCapacityValue  = 30.0 [kB];
            feature t: ISQ::DurationValue  = 10.0 [s];
            feature BR1: ISQ::BitRateValue = i1/t {:>> unit = "Mbps";}
            feature BR2: ISQ::BitRateValue = i2/t{:>> unit = "kB/s";}
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.5, global.resolveVar("BR1")!!.min(), 0.0001)
        assertEquals(3.0, global.resolveVar("BR2")!!.min(), 0.0001)
        assertEquals("StorageCapacity", global.resolveVar("i1")!!.vectorQuantity.getDomain())
        assertEquals("StorageCapacity", global.resolveVar("i2")!!.vectorQuantity.getDomain())
        assertEquals("Duration", global.resolveVar("t")!!.vectorQuantity.getDomain())
        assertEquals("BitRate", global.resolveVar("BR1")!!.vectorQuantity.getDomain())
        assertEquals("BitRate", global.resolveVar("BR2")!!.vectorQuantity.getDomain())
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
        assertEquals("Mass", global.resolveVar("Mass")!!.vectorQuantity.unit.unitDomain)
    }

    @Test
    fun defineDomainTest2() = testSession("ISQ") {
        loadKerML("""
            feature Mass: ISQ::MassValue = 10.0 [kg];
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals("kg", global.resolveVar("Mass")!!.vectorQuantity.unit.toString())
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
