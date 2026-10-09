package quantitytests

import util.variable
import util.assertBounds
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
        assertEquals("m", solver.variable("s").vectorQuantity.unit.toString())
        assertBounds(10.0, solver.variable("s"))
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
        assertEquals("Speed", solver.variable("v").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("s").vectorQuantity.getDomain())
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
        assertEquals("m / s", solver.variable("v2").vectorQuantity.unit.toString())
        assertBounds(10.0, solver.variable("v2"))
        assertBounds(20.0, solver.variable("s"))
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
        assertEquals("Speed", solver.variable("v").vectorQuantity.getDomain())
        assertEquals("Speed", solver.variable("v2").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("s").vectorQuantity.getDomain())
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
        assertEquals("m / s", solver.variable("v").vectorQuantity.unit.toString())
        assertBounds(2.0, solver.variable("t"))
        assertBounds(10.0, solver.variable("v"))
        assertBounds(20.0, solver.variable("s"))
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
        assertEquals("Speed", solver.variable("v").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("s").vectorQuantity.getDomain())
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
        assertEquals("m / s", solver.variable("v0").vectorQuantity.unit.toString())
        assertBounds(4.0, solver.variable("s"))
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
        assertEquals("Speed", solver.variable("v0").vectorQuantity.getDomain())
        assertEquals("Acceleration", solver.variable("a").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("s").vectorQuantity.getDomain())
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
        assertBounds(5.0, solver.variable("s"))
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
        assertEquals("Speed", solver.variable("v").vectorQuantity.getDomain())
        assertEquals("Acceleration", solver.variable("g").vectorQuantity.getDomain())
        assertEquals("Speed", solver.variable("s").vectorQuantity.getDomain())
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
        assertBounds(6.0, solver.variable("s"))
        assertEquals("Length", solver.variable("h").vectorQuantity.getDomain())
        assertEquals("Speed", solver.variable("v0").vectorQuantity.getDomain())
        assertEquals("Acceleration", solver.variable("g").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("s").vectorQuantity.getDomain())
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
        assertBounds(45.0, solver.variable("Fz"))
        assertEquals("Length", solver.variable("r").vectorQuantity.getDomain())
        assertEquals("Frequency", solver.variable("Omega").vectorQuantity.getDomain())
        assertEquals("Mass", solver.variable("m").vectorQuantity.getDomain())
        assertEquals("Force", solver.variable("Fz").vectorQuantity.getDomain())
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
        assertBounds(45.0, solver.variable("Fz"))
        assertEquals("Length", solver.variable("r").vectorQuantity.getDomain())
        assertEquals("Frequency", solver.variable("Omega").vectorQuantity.getDomain())
        assertEquals("Mass", solver.variable("m").vectorQuantity.getDomain())
        assertEquals("Force", solver.variable("Fz").vectorQuantity.getDomain())
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
        assertBounds(45.0, solver.variable("E"))
        assertEquals("Force", solver.variable("F").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("s").vectorQuantity.getDomain())
        assertEquals("Energy", solver.variable("E").vectorQuantity.getDomain())
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
        assertBounds(45.0, solver.variable("p"))
        assertEquals("Mass", solver.variable("m").vectorQuantity.getDomain())
        assertEquals("Speed", solver.variable("v").vectorQuantity.getDomain())
        assertEquals("Momentum", solver.variable("p").vectorQuantity.getDomain())
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
        assertBounds(18.0, solver.variable("Epot"))
        assertEquals("Mass", solver.variable("m1").vectorQuantity.getDomain())
        assertEquals("Mass", solver.variable("m2").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("r1").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("r2").vectorQuantity.getDomain())
        assertEquals("Energy", solver.variable("Epot").vectorQuantity.getDomain())
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
        assertBounds(26.5258462540730, solver.variable("E"))
        assertEquals("ElectricCharge", solver.variable("Q").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("r").vectorQuantity.getDomain())
        assertEquals("ElectricFieldStrength", solver.variable("E").vectorQuantity.getDomain())
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
        assertBounds(26.525846254073027..26.525846254073066, solver.variable("F"))
        assertEquals("ElectricCharge", solver.variable("Q1").vectorQuantity.getDomain())
        assertEquals("ElectricCharge", solver.variable("Q2").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("r").vectorQuantity.getDomain())
        assertEquals("Force", solver.variable("F").vectorQuantity.getDomain())
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
        solver.propagate()
        assertNoIssues()
        assertEquals("ElectricFieldStrength", solver.variable("E").vectorQuantity.getDomain())
        assertEquals("ElectricCharge", solver.variable("q").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("s").vectorQuantity.getDomain())
        assertEquals("Energy", solver.variable("W").vectorQuantity.getDomain())
        assertBounds(2.0, solver.variable("W"))
    }

    @Test
    fun unitsElectricity5() = testSession("ISQ") {
        loadKerML("""
            feature epsilon0: Quantities::ScalarQuantityValue  = 3.0 [A^2 s^2 / N m^2]{:>> range = (*..*) [A^2 s^2 / N m^2];}
            feature E: ISQ::ElectricFieldStrengthValue = 2.0 [V / m];
            feature d: ISQ::LengthValue = 0.2 [m];
            feature U: ISQ::ElectricPotentialDifferenceValue= E*d; 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.4, solver.variable("U"))
        assertEquals("ElectricFieldStrength", solver.variable("E").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("d").vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", solver.variable("U").vectorQuantity.getDomain())
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
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0, solver.variable("W"))
        assertEquals("ElectricCharge", solver.variable("Q1").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("r1").vectorQuantity.getDomain())
        assertEquals("Energy", solver.variable("W").vectorQuantity.getDomain())
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
        assertBounds(0.2, solver.variable("G"))
        assertEquals("m^2 / s^2", solver.variable("G").vectorQuantity.unit.toString())
        assertEquals("AbsorbedDose", solver.variable("G").vectorQuantity.getDomain())
    }

    @Test
    fun activityTest() = testSession("ISQ") {
        loadKerML("""
                feature t1: ISQ::DurationValue = 1.0 [s];
                feature A: ISQ::NuclearActivityValue = 1.0/t1;
            """)
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0, solver.variable("A"))
        assertEquals("1 / s", solver.variable("A").vectorQuantity.unit.toString())
        assertEquals("NuclearActivity", solver.variable("A").vectorQuantity.getDomain())
        assertEquals("Duration", solver.variable("t1").vectorQuantity.getDomain())
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
        assertBounds(0.5, solver.variable("A"))
        assertEquals("m^2", solver.variable("A").vectorQuantity.unit.toString())
        assertEquals("Area", solver.variable("A").vectorQuantity.getDomain())
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
        assertBounds(2.0, solver.variable("C"))
        assertEquals("A^2 s^4 / kg m^2", solver.variable("C").vectorQuantity.unit.toString())
        assertEquals("Capacitance", solver.variable("C").vectorQuantity.getDomain())
        assertEquals("ElectricCharge", solver.variable("Q").vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", solver.variable("U").vectorQuantity.getDomain())
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
        assertBounds(2.0, solver.variable("d"))
        assertEquals("kg / m^3", solver.variable("d").vectorQuantity.unit.toString())
        assertEquals("MassDensity", solver.variable("d").vectorQuantity.getDomain())
        assertEquals("Volume", solver.variable("V").vectorQuantity.getDomain())
        assertEquals("Mass", solver.variable("m").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("G"))
        assertEquals("A^2 s^3 / kg m^2", solver.variable("G").vectorQuantity.unit.toString())
        assertEquals("Conductance", solver.variable("G").vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", solver.variable("V").vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", solver.variable("I").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("R"))
        assertEquals("kg m^2 / A^2 s^3", solver.variable("R").vectorQuantity.unit.toString())
        assertEquals("Resistance", solver.variable("R").vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", solver.variable("U").vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", solver.variable("I").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("Q"))
        assertEquals("A s", solver.variable("Q").vectorQuantity.unit.toString())
        assertEquals("ElectricCharge", solver.variable("Q").vectorQuantity.getDomain())
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", solver.variable("I").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("U"))
        assertEquals("kg m^2 / A s^3", solver.variable("U").vectorQuantity.unit.toString())
        assertEquals("ElectricPotentialDifference", solver.variable("U").vectorQuantity.getDomain())
        assertEquals("Power", solver.variable("P").vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", solver.variable("I").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("E"))
        assertEquals("kg m^2 / s^2", solver.variable("E").vectorQuantity.unit.toString())
        assertEquals("Energy", solver.variable("E").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("l").vectorQuantity.getDomain())
        assertEquals("Force", solver.variable("F").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("ED"))
        assertEquals("kg / m s^2", solver.variable("ED").vectorQuantity.unit.toString())
        assertEquals("EnergyDensity", solver.variable("ED").vectorQuantity.getDomain())
        assertEquals("Energy", solver.variable("E").vectorQuantity.getDomain())
        assertEquals("Volume", solver.variable("V").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("S"))
        assertEquals("kg m^2 / K s^2", solver.variable("S").vectorQuantity.unit.toString())
        assertEquals("Entropy", solver.variable("S").vectorQuantity.getDomain())
        assertEquals("ThermodynamicTemperature", solver.variable("T").vectorQuantity.getDomain())
        assertEquals("Energy", solver.variable("E").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("F"))
        assertEquals("kg m / s^2", solver.variable("F").vectorQuantity.unit.toString())
        assertEquals("Force", solver.variable("F").vectorQuantity.getDomain())
        assertEquals("Acceleration", solver.variable("a").vectorQuantity.getDomain())
        assertEquals("Mass", solver.variable("m").vectorQuantity.getDomain())
    }

    @Test
    fun frequencyTest() = testSession("ISQ") {
        loadKerML("""
            feature t: ISQ::DurationValue = 1.0 [s];
            feature f: ISQ::FrequencyValue = 1.0/t;
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0, solver.variable("f"))
        assertEquals("1 / s", solver.variable("f").vectorQuantity.unit.toString())
        assertEquals("Frequency", solver.variable("f").vectorQuantity.getDomain())
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
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
        assertBounds(1000.0, solver.variable("flops"))
        assertEquals("1 / s", solver.variable("flops").vectorQuantity.unit.toString())
        assertEquals("Frequency", solver.variable("flops").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("E"))
        assertEquals("cd / m^2", solver.variable("E").vectorQuantity.unit.toString())
        assertEquals("Illuminance", solver.variable("E").vectorQuantity.getDomain())
        assertEquals("Area", solver.variable("A").vectorQuantity.getDomain())
        assertEquals("LuminousIntensity", solver.variable("I").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("L"))
        assertEquals("kg m^2 / A^2 s^2", solver.variable("L").vectorQuantity.unit.toString())
        assertEquals("Inductance", solver.variable("L").vectorQuantity.getDomain())
        assertEquals("ElectricCurrent", solver.variable("I").vectorQuantity.getDomain())
        assertEquals("MagneticFlux", solver.variable("W").vectorQuantity.getDomain())
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
        assertBounds(10000.0, solver.variable("v"), unit = "St")
        assertEquals("m^2 / s", solver.variable("v").vectorQuantity.unit.toString())
        assertEquals("KinematicViscosity", solver.variable("v").vectorQuantity.getDomain())
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
        assertEquals("Area", solver.variable("A").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("I"))
        assertEquals("cd", solver.variable("I").vectorQuantity.unit.toString())
        assertEquals("Luminance", solver.variable("v").vectorQuantity.getDomain())
        assertEquals("Area", solver.variable("A").vectorQuantity.getDomain())
        assertEquals("LuminousIntensity", solver.variable("I").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("K"))
        assertEquals("cd s^3 / kg m^2", solver.variable("K").vectorQuantity.unit.toString())
        assertEquals("LuminousEfficacy", solver.variable("K").vectorQuantity.getDomain())
        assertEquals("LuminousFlux", solver.variable("A").vectorQuantity.getDomain())
        assertEquals("Power", solver.variable("P").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("Q"))
        assertEquals("cd s", solver.variable("Q").vectorQuantity.unit.toString())
        assertEquals("LuminousEnergy", solver.variable("Q").vectorQuantity.getDomain())
        assertEquals("LuminousFlux", solver.variable("A").vectorQuantity.getDomain())
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
    }

    @Test
    fun luminousFluxTest() = testSession("ISQ") {
        loadKerML("feature t: ISQ::LuminousFluxValue = 1.0 [lm];")
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0, solver.variable("t"))
        assertEquals("cd", solver.variable("t").vectorQuantity.unit.toString())
        assertEquals("LuminousFlux", solver.variable("t").vectorQuantity.getDomain())
    }

    @Test
    fun magneticFlux() = testSession("ISQ") {
        loadKerML("""
            feature U: ISQ::ElectricPotentialDifferenceValue= 1.0 [V] ;
            feature t: ISQ::DurationValue = 1.0 [s] ;
            feature Phi: ISQ::MagneticFluxValue = U*t;""")
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0, solver.variable("Phi"))
        assertEquals("kg m^2 / A s^2", solver.variable("Phi").vectorQuantity.unit.toString())
        assertEquals("MagneticFlux", solver.variable("Phi").vectorQuantity.getDomain())
        assertEquals("ElectricPotentialDifference", solver.variable("U").vectorQuantity.getDomain())
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
    }

    @Test
    fun magneticFluxDensityTest() = testSession("ISQ") {
        loadKerML("""
            feature Phi: ISQ::MagneticFluxValue  = 1.0 [Wb];
            feature t: ISQ::AreaValue = 1.0 [m^2];
            feature B: ISQ::MagneticFluxDensityValue = Phi/t;""")
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0, solver.variable("B"))
        assertEquals("kg / A s^2", solver.variable("B").vectorQuantity.unit.toString())
        assertEquals("MagneticFluxDensity", solver.variable("B").vectorQuantity.getDomain())
        assertEquals("Area", solver.variable("t").vectorQuantity.getDomain())
        assertEquals("MagneticFlux", solver.variable("Phi").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("B"))
        assertEquals("kg / s", solver.variable("B").vectorQuantity.unit.toString())
        assertEquals("MassFlow", solver.variable("B").vectorQuantity.getDomain())
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
        assertEquals("Mass", solver.variable("m").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("B"))
        assertEquals("kg m^2 / s^2", solver.variable("B").vectorQuantity.unit.toString())
        assertEquals("MomentOfForce", solver.variable("B").vectorQuantity.getDomain())
        assertEquals("Force", solver.variable("F").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("l").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("I"))
        assertEquals("kg m^2", solver.variable("I").vectorQuantity.unit.toString())
        assertEquals("MomentOfInertia", solver.variable("I").vectorQuantity.getDomain())
        assertEquals("Area", solver.variable("A").vectorQuantity.getDomain())
        assertEquals("Mass", solver.variable("m").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("p"))
        assertEquals("kg m / s", solver.variable("p").vectorQuantity.unit.toString())
        assertEquals("Momentum", solver.variable("p").vectorQuantity.getDomain())
        assertEquals("Speed", solver.variable("v").vectorQuantity.getDomain())
        assertEquals("Mass", solver.variable("m").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("epsilon"))
        assertEquals("A^2 s^4 / kg m^3", solver.variable("epsilon").vectorQuantity.unit.toString())
        assertEquals("Permittivity", solver.variable("epsilon").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("l").vectorQuantity.getDomain())
        assertEquals("Capacitance", solver.variable("I").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("P"))
        assertEquals("kg m^2 / s^3", solver.variable("P").vectorQuantity.unit.toString())
        assertEquals("Power", solver.variable("P").vectorQuantity.getDomain())
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
        assertEquals("Energy", solver.variable("E").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("p"))
        assertEquals("kg / m s^2", solver.variable("p").vectorQuantity.unit.toString())
        assertEquals("Pressure", solver.variable("p").vectorQuantity.getDomain())
        assertEquals("Area", solver.variable("A").vectorQuantity.getDomain())
        assertEquals("Force", solver.variable("F").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("p2"))
        assertBounds(0.01, solver.variable("p"), unit = "mbar")
        assertEquals("kg / m s^2", solver.variable("p").vectorQuantity.unit.toString())
        assertEquals("Pressure", solver.variable("p").vectorQuantity.getDomain())
        assertEquals("Area", solver.variable("A").vectorQuantity.getDomain())
        assertEquals("Force", solver.variable("F").vectorQuantity.getDomain())
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
        assertBounds(10000.0, solver.variable("p"), unit = "%")
        assertBounds(20.0, solver.variable("f"), unit = "dB")
        assertBounds(100.0, solver.variable("E2"))
        assertEquals("1", solver.variable("p").vectorQuantity.unit.toString())
        assertEquals("DimensionOne", solver.variable("p").vectorQuantity.getDomain())
        assertEquals("DimensionOne", solver.variable("E").vectorQuantity.getDomain())
        assertEquals("DimensionOne", solver.variable("E2").vectorQuantity.getDomain())
        assertEquals("DimensionOne", solver.variable("f").vectorQuantity.getDomain())
    }

    @Test
    fun flopsTest() = testSession("ISQ") {
        loadKerML("""
            feature operations: ScalarValues::Real = 1000.0;
            feature flops: Quantities::ScalarQuantityValue = operations {:>> range = *..* [FLOPs];}
            """)
        solver.propagate()
        assertNoIssues()
        assertBounds(1000.0, solver.variable("flops"))
        assertEquals("1", solver.variable("flops").vectorQuantity.unit.toString())
        assertEquals("DimensionOne", solver.variable("flops").vectorQuantity.getDomain())
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
        assertBounds(10.0, solver.variable("v1"))
        assertBounds(36.0, solver.variable("v2"), unit = "km/h")
        assertEquals("m / s", solver.variable("v1").vectorQuantity.unit.toString())
        assertEquals("Speed", solver.variable("v1").vectorQuantity.getDomain())
        assertEquals("Speed", solver.variable("v2").vectorQuantity.getDomain())
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("l").vectorQuantity.getDomain())
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
        assertBounds(1.0, solver.variable("V1"))
        assertBounds(1000.0, solver.variable("V2"), unit = "l")
        assertEquals("m^3", solver.variable("V1").vectorQuantity.unit.toString())
        assertEquals("Volume", solver.variable("V1").vectorQuantity.getDomain())
        assertEquals("Volume", solver.variable("V2").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("l1").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("l2").vectorQuantity.getDomain())
        assertEquals("Length", solver.variable("l3").vectorQuantity.getDomain())
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
        solver.propagate()
        assertNoIssues()
        assertBounds(0.5, solver.variable("BR1"), unit = "Mbps")
        assertBounds(3.0, solver.variable("BR2"), unit = "kB/s")
        assertEquals("StorageCapacity", solver.variable("i1").vectorQuantity.getDomain())
        assertEquals("StorageCapacity", solver.variable("i2").vectorQuantity.getDomain())
        assertEquals("Duration", solver.variable("t").vectorQuantity.getDomain())
        assertEquals("BitRate", solver.variable("BR1").vectorQuantity.getDomain())
        assertEquals("BitRate", solver.variable("BR2").vectorQuantity.getDomain())
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
        assertEquals("Mass", solver.variable("Mass").vectorQuantity.unit.unitDomain)
    }

    @Test
    fun defineDomainTest2() = testSession("ISQ") {
        loadKerML("""
            feature Mass: ISQ::MassValue = 10.0 [kg];
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals("kg", solver.variable("Mass").vectorQuantity.unit.toString())
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
