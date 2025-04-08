package quantitytests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuantityTestDimensions {

    /** Kinematic Formulars */
    @Test
    fun unitskinematik1() = testSession("SI") {
        loadKerML("""
            feature t: SI::Time = 2.0 [s];
            feature v: SI::Speed  = 5.0 [m/s];
            feature s: SI::Length  = v*t;"""
        )
        propagate()
        assertEquals("m", global.resolveVar("s")!!.vectorQuantity.unit.toString())
        assertEquals(10.0, global.resolveVar("s")!!.aadd().getRange().max, 0.00001)
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("Speed", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun unitskinematik2() = testSession("SI") {
        loadKerML("""
                feature t: SI::Time  = 2000.0 [ms];
                feature v: SI::Speed = 36.0 [km/h];
                feature v2: SI::Speed  = v;
                feature s: SI::Length  = v2*t;"""
        )
        propagate()
        assertEquals("m / s", global.resolveVar("v2")!!.vectorQuantity.unit.toString())
        assertEquals(10.0, global.resolveVar("v2")!!.aadd().getRange().min, 0.00001)
        assertEquals(20.0, global.resolveVar("s")!!.aadd().getRange().max, 0.00001)
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("Speed", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("Speed", global.resolveVar("v2")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }


    @Test
    fun unitskinematik3() = testSession("SI") {
        loadKerML("""
            feature t: SI::Time = 2000.0 [ms] ;
            feature v: SI::Speed = 36.0 [km/h];
            feature s: SI::Length  = v*t;"""

        )
        propagate()
        assertEquals("m / s", global.resolveVar("v")!!.vectorQuantity.unit.toString())
        assertEquals(2.0, global.resolveVar("t")!!.aadd().getRange().min, 0.00001)
        assertEquals(10.0, global.resolveVar("v")!!.aadd().getRange().max, 0.00001)
        assertEquals(20.0, global.resolveVar("s")!!.aadd().getRange().min, 0.00001)
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("Speed", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    // Test Fails caused by parser error with sqr, works without adding v0*t
    @Test
    fun unitskinematik4() = testSession("SI") {
        loadKerML("""
                feature t: SI::Time = 2.0 [s];
                feature v0: SI::Speed  = 1.0 [m/s];
                feature a: SI::Acceleration  = 1.0 [m/s^2];
                feature s: SI::Length  = 0.5*a*sqr(t)+v0*t;"""
        )
        propagate()
        assertEquals("m / s", global.resolveVar("v0")!!.vectorQuantity.unit.toString())
        assertEquals(4.0, global.resolveVar("s")!!.aadd().getRange().min, 0.00001)
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("Speed", global.resolveVar("v0")!!.vectorQuantity.getDimension())
        assertEquals("Acceleration", global.resolveVar("a")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun unitskinematik5() = testSession("SI") {
        loadKerML("""
            feature t: SI::Time = 1.0 [s];
            feature v: SI::Speed  = 3.0 [m/s];
            feature g: SI::Acceleration  = 4.0 [m/s^2];
            feature s: SI::Speed  = sqrt(sqr(v)+sqr(g)*sqr(t));"""
        )
        propagate()
        assertEquals(5.0, global.resolveVar("s")!!.aadd().getRange().min, 0.00001)
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("Speed", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("Acceleration", global.resolveVar("g")!!.vectorQuantity.getDimension())
        assertEquals("Speed", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun unitskinematik6() = testSession("SI") {
        loadKerML("""
            feature h: SI::Length = 100.0 [dm];
            feature v0: SI::Speed  = 3.0 [m/s];
            feature g: SI::Acceleration = 5.0 [m/s^2];
            feature s: SI::Length  = v0*sqrt(2.0*h/g); """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(6.0, global.resolveVar("s")!!.aadd().getRange().min, 0.00001)
        assertEquals("Length", global.resolveVar("h")!!.vectorQuantity.getDimension())
        assertEquals("Speed", global.resolveVar("v0")!!.vectorQuantity.getDimension())
        assertEquals("Acceleration", global.resolveVar("g")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDimension())
    }

    @Test
    fun unitskinematik7() = testSession("SI") {
        loadKerML("""
            feature r: SI::Length = 100.0 [cm];
            feature Omega: SI::Frequency = 3.0 [Hz];
            feature m: SI::Mass = 5.0 [kg];
            feature Fz: SI::Force = m*sqr(Omega)*r;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(45.0, global.resolveVar("Fz")!!.aadd().getRange().min, 0.00001)
        assertEquals("Length", global.resolveVar("r")!!.vectorQuantity.getDimension())
        assertEquals("Frequency", global.resolveVar("Omega")!!.vectorQuantity.getDimension())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
        assertEquals("Force", global.resolveVar("Fz")!!.vectorQuantity.getDimension())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun unitskinematik8() = testSession("SI")  {
        loadKerML("""
            feature r: SI::Length  = 1.0 [m];
            feature Omega: SI::Frequency = 3.0 [Hz]; 
            feature m: SI::Mass  = 5.0 [kg];
            feature Fz: SI::Force  = m*sqr(Omega)*r; """
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(45.0, global.resolveVar("Fz")!!.aadd().getRange().min, 0.00001)
        assertEquals("Length", global.resolveVar("r")!!.vectorQuantity.getDimension())
        assertEquals("Frequency", global.resolveVar("Omega")!!.vectorQuantity.getDimension())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
        assertEquals("Force", global.resolveVar("Fz")!!.vectorQuantity.getDimension())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /**Units with Energy**/
    @Test
    fun unitsEnergy1() = testSession("SI") {
        loadKerML("""
            feature F: SI::Force = 15.0 [N];
            feature s: SI::Length  = 3.0 [m];
            feature E: SI::Energy = F*s ;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(45.0, global.resolveVar("E")!!.aadd().getRange().min, 0.00001)
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals("Energy", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun unitsEnergy2() = testSession("SI") {
        loadKerML("""
            feature m: SI::Mass = 3.0 [kg];
            feature v: SI::Speed  = 15.0 [m/s];
            feature p: SI::Momentum = m * v;"""

        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(45.0, global.resolveVar("p")!!.aadd().getRange().min, 0.00001)
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
        assertEquals("Speed", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("Momentum", global.resolveVar("p")!!.vectorQuantity.getDimension())
    }

    /**Units with gravitation**/
    @Test
    fun unitsGravitation() = testSession("SI") {
        loadKerML("""
            feature gamma: SI::Quantity = 3.0 [m^3/kg s^2]{:>> unit = "m^3/kg s^2";}
            feature m1: SI::Mass = 4.0 [kg];
            feature m2: SI::Mass  = 2.0 [kg];
            feature r1: SI::Length  = 2.0 [m];
            feature r2: SI::Length = 4.0 [m];
            feature Epot: SI::Energy = gamma * m1 * m2 * (1.0/r1 + 1.0/r2);"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(18.0, global.resolveVar("Epot")!!.aadd().getRange().min, 0.00001)
        assertEquals("Mass", global.resolveVar("m1")!!.vectorQuantity.getDimension())
        assertEquals("Mass", global.resolveVar("m2")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("r1")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("r2")!!.vectorQuantity.getDimension())
        assertEquals("Energy", global.resolveVar("Epot")!!.vectorQuantity.getDimension())
    }

    /**Units with electricity**/
    @Test
    fun unitsElectricity1() = testSession("SI") {
        loadKerML(
            """
            feature epsilon0: SI::Quantity = 3.0 [A^2 s^2 / N m^2]{:>> unit = "A^2 s^2 / N m^2";}
            feature Q: SI::ElectricCharge = 40.0 [C];
            feature r: SI::Length = 0.2 [m];
            feature E: SI::ElectricField = 1.0/(4.0 * 3.14159*epsilon0) * Q/sqr(r); """
        )
        propagate()
        assertEquals(26.5258, global.resolveVar("E")!!.aadd().getRange().min, 0.0001)
        assertEquals("ElectricCharge", global.resolveVar("Q")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("r")!!.vectorQuantity.getDimension())
        assertEquals("ElectricField", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun unitsElectricity2() = testSession("SI") {
        loadKerML(
            """
            feature epsilon0: SI::Quantity  = 3.0 [A^2 s^2 / N m^2]{:>> unit = "A^2 s^2 / N m^2";}
            feature Q1: SI::ElectricCharge = 40.0 [C];
            feature Q2: SI::ElectricCharge = 1.0 [C];
            feature r: SI::Length = 0.2 [m];
            feature F: SI::Force = 1.0/(4.0 * 3.14159*epsilon0) * (Q1*Q2)/sqr(r) ;"""
        )
        propagate() // Strange, destroys an already correct result.
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(26.5258, global.resolveVar("F")!!.aadd().getRange().min, 0.0001)
        assertEquals("ElectricCharge", global.resolveVar("Q1")!!.vectorQuantity.getDimension())
        assertEquals("ElectricCharge", global.resolveVar("Q2")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("r")!!.vectorQuantity.getDimension())
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDimension())
    }

    @Test
    fun unitsElectricity3() = testSession("SI") {
        loadKerML(
            """feature epsilon0: SI::Quantity = 3.0 [A^2 s^2 / N m^2]{:>> unit = "A^2 s^2 / N m^2";}
            feature E: SI::ElectricField = 2.0 [V / m] ;
            feature q: SI::ElectricCharge = 5.0 [A s] ;
            feature s: SI::Length = 0.2 [m];
            feature W: SI::Energy = E*q*s ;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals("ElectricField", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("ElectricCharge", global.resolveVar("q")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals("Energy", global.resolveVar("W")!!.vectorQuantity.getDimension())
        assertEquals(2.0, global.resolveVar("W")!!.aadd().getRange().min, 0.0001)
    }

    @Test
    fun unitsElectricity5() = testSession("SI") {
        loadKerML(
            """feature epsilon0: SI::Quantity  = 3.0 [A^2 s^2 / N m^2]{:>> unit = "A^2 s^2 / N m^2";}
                feature E: SI::ElectricField = 2.0 [V / m];
                feature d: SI::Length = 0.2 [m];
                feature U: SI::Voltage = E*d; """
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0.4, global.resolveVar("U")!!.aadd().getRange().min, 0.0001)
        assertEquals("ElectricField", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("d")!!.vectorQuantity.getDimension())
        assertEquals("Voltage", global.resolveVar("U")!!.vectorQuantity.getDimension())
    }

    @Test
    fun unitsElectricity6() = testSession("SI") {
        loadKerML(
            """
            feature epsilon0: SI::Quantity  = 3.0 [A^2 s^2 / N m^2]{:>> unit = "A^2 s^2 / N m^2";}
            feature Q1: SI::ElectricCharge  = 3.14159 [C];
            feature Q2: SI::ElectricCharge  = 3000.0 [mC];
            feature r1: SI::Length  = 1.0 [m] ;
            feature r2: SI::Length  = 50.0 [cm];
            feature W: SI::Energy  = (Q1*Q2)/(3.0*3.14159*epsilon0)*(1.0/r1+1.0/r2); """
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("W")!!.aadd().getRange().min, 0.0001)
        assertEquals("ElectricCharge", global.resolveVar("Q1")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("r1")!!.vectorQuantity.getDimension())
        assertEquals("Energy", global.resolveVar("W")!!.vectorQuantity.getDimension())
    }

  
    @Test
    fun absorbedDoseTest() = testSession("SI") {
        loadKerML("""
                feature E: SI::Energy = 1.0 [J];
                feature w: SI::Mass = 5.0 [kg];
                feature G: SI::AbsorbedDose = E/w;
            """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0.2, global.resolveVar("G")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2 / s^2", global.resolveVar("G")!!.vectorQuantity.unit.toString())
        assertEquals("AbsorbedDose", global.resolveVar("G")!!.vectorQuantity.getDimension())
    }

    @Test
    fun activityTest() = testSession("SI") {
        loadKerML("""
                feature t1: SI::Time = 1.0 [s];
                feature A: SI::Activity = 1.0/t1;
            """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("A")!!.aadd().getRange().min, 0.0001)
        assertEquals("1 / s", global.resolveVar("A")!!.vectorQuantity.unit.toString())
        assertEquals("Activity", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("Time", global.resolveVar("t1")!!.vectorQuantity.getDimension())
    }

    @Test
    fun areaTest() = testSession("SI") {
        loadKerML("""
                feature l1: SI::Length = 10.0 [dm];
                feature l2: SI::Length = 50.0 [cm]{:>> unit = "cm";}
                feature A: SI::Area = l1*l2;
            """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0.5, global.resolveVar("A")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2", global.resolveVar("A")!!.vectorQuantity.unit.toString())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDimension())
    }

    @Test
    fun capacitanceTest() = testSession("SI") {
        loadKerML("""
                feature Q: SI::ElectricCharge  = 10.0 [C];
                feature U: SI::Voltage  = 5.0 [V];
                feature C: SI::Capacitance = Q/U;
            """ )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(2.0, global.resolveVar("C")!!.aadd().getRange().min, 0.0001)
        assertEquals("A^2 s^4 / kg m^2", global.resolveVar("C")!!.vectorQuantity.unit.toString())
        assertEquals("Capacitance", global.resolveVar("C")!!.vectorQuantity.getDimension())
        assertEquals("ElectricCharge", global.resolveVar("Q")!!.vectorQuantity.getDimension())
        assertEquals("Voltage", global.resolveVar("U")!!.vectorQuantity.getDimension())
    }

    @Test
    fun catalyticActivityTest() = testSession("SI") {
        loadKerML("""
                feature Q: SI::AmountOfSubstance  = 60.0 [mol]; 
                feature t: SI::Time = 1.0 [min]; 
                feature K: SI::CatalyticActivity = Q/t;
            """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("K")!!.aadd().getRange().min, 0.0001)
        assertEquals("mol / s", global.resolveVar("K")!!.vectorQuantity.unit.toString())
        assertEquals("CatalyticActivity", global.resolveVar("K")!!.vectorQuantity.getDimension())
        assertEquals("AmountOfSubstance", global.resolveVar("Q")!!.vectorQuantity.getDimension())
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
    }

    @Test
    fun density() = testSession("SI") {
        loadKerML(
            """
            feature m: SI::Mass = 2.0 [g];
            feature V: SI::Volume = 1.0 [dm^3];
            feature d: SI::Density = m/V;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(2.0, global.resolveVar("d")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / m^3", global.resolveVar("d")!!.vectorQuantity.unit.toString())
        assertEquals("Density", global.resolveVar("d")!!.vectorQuantity.getDimension())
        assertEquals("Volume", global.resolveVar("V")!!.vectorQuantity.getDimension())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
    }

    @Test
    fun electricalConductanceTest() = testSession("SI") {
        loadKerML(
             """feature I: SI::ElectricCurrent  = 1000.0 [mA] ;
            feature V: SI::Voltage = 1.0 [V] ;
            feature G: SI::ElectricalConductance = I/V;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("G")!!.aadd().getRange().min, 0.0001)
        assertEquals("A^2 s^3 / kg m^2", global.resolveVar("G")!!.vectorQuantity.unit.toString())
        assertEquals("ElectricalConductance", global.resolveVar("G")!!.vectorQuantity.getDimension())
        assertEquals("Voltage", global.resolveVar("V")!!.vectorQuantity.getDimension())
        assertEquals("ElectricCurrent", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun electricalresistanceTest() = testSession("SI") {
        loadKerML(
            """
            feature I: SI::ElectricCurrent = 1000.0 [mA];
            feature U: SI::Voltage = 1.0 [V] ;
            feature R: SI::ElectricalResistance  = U/I;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("R")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m^2 / A^2 s^3", global.resolveVar("R")!!.vectorQuantity.unit.toString())
        assertEquals("ElectricalResistance", global.resolveVar("R")!!.vectorQuantity.getDimension())
        assertEquals("Voltage", global.resolveVar("U")!!.vectorQuantity.getDimension())
        assertEquals("ElectricCurrent", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun electrichargeTest() = testSession("SI") {
        loadKerML("""
            feature I: SI::ElectricCurrent = 1000.0 [mA];
            feature t: SI::Time = 1.0 [s];
            feature Q: SI::ElectricCharge = t*I;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("Q")!!.aadd().getRange().min, 0.0001)
        assertEquals("A s", global.resolveVar("Q")!!.vectorQuantity.unit.toString())
        assertEquals("ElectricCharge", global.resolveVar("Q")!!.vectorQuantity.getDimension())
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("ElectricCurrent", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun electricPotentialDifferenceTest() = testSession("SI") {
        loadKerML(
            """
            feature I: SI::ElectricCurrent = 1000.0 [mA];
            feature P: SI::Power = 1.0 [W];
            feature U: SI::Voltage = P/I;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("U")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m^2 / A s^3", global.resolveVar("U")!!.vectorQuantity.unit.toString())
        assertEquals("Voltage", global.resolveVar("U")!!.vectorQuantity.getDimension())
        assertEquals("Power", global.resolveVar("P")!!.vectorQuantity.getDimension())
        assertEquals("ElectricCurrent", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun EnergyTest() = testSession("SI") {
        loadKerML("""
                feature F: SI::Force = 1000.0 [mN];
                feature l: SI::Length = 1.0 [m];
                feature E: SI::Energy = F*l;
        """

        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("E")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m^2 / s^2", global.resolveVar("E")!!.vectorQuantity.unit.toString())
        assertEquals("Energy", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("l")!!.vectorQuantity.getDimension())
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDimension())
    }

    @Test
    fun EnergyDensityTest() = testSession("SI") {
        loadKerML("""
            feature E: SI::Energy = 1.0 [J];
            feature V: SI::Volume = 1.0 [m^3];
            feature ED: SI::EnergyDensity = E/V;"""
        )

        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("ED")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / m s^2", global.resolveVar("ED")!!.vectorQuantity.unit.toString())
        assertEquals("EnergyDensity", global.resolveVar("ED")!!.vectorQuantity.getDimension())
        assertEquals("Energy", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("Volume", global.resolveVar("V")!!.vectorQuantity.getDimension())
    }

    @Test
    fun entropyTest() = testSession("SI") {
        loadKerML(
            """
            feature E: SI::Energy = 1.0 [J];
            feature T: SI::Temperature = 1.0 [K];
            feature S: SI::Entropy  = E/T;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("S")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m^2 / K s^2", global.resolveVar("S")!!.vectorQuantity.unit.toString())
        assertEquals("Entropy", global.resolveVar("S")!!.vectorQuantity.getDimension())
        assertEquals("Temperature", global.resolveVar("T")!!.vectorQuantity.getDimension())
        assertEquals("Energy", global.resolveVar("E")!!.vectorQuantity.getDimension())
    }

    @Test
    fun ForceTest() = testSession("SI") {
        loadKerML(
            """feature m: SI::Mass = 1.0 [kg];
            feature a: SI::Acceleration = 1.0 [m/s^2];
            feature F: SI::Force = m*a;"""

        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("F")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m / s^2", global.resolveVar("F")!!.vectorQuantity.unit.toString())
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDimension())
        assertEquals("Acceleration", global.resolveVar("a")!!.vectorQuantity.getDimension())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
    }

    @Test
    fun FrequencyTest() = testSession("SI") {
        loadKerML("""
            feature t: SI::Time = 1.0 [s];
            feature f: SI::Frequency = 1.0/t;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("f")!!.aadd().getRange().min, 0.0001)
        assertEquals("1 / s", global.resolveVar("f")!!.vectorQuantity.unit.toString())
        assertEquals("Frequency", global.resolveVar("f")!!.vectorQuantity.getDimension())
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
    }

    @Test
    fun illuminanceTest() = testSession("SI") {
        loadKerML(
            """
            feature I: SI::LuminousIntensity  = 1.0 [cd];
            feature A: SI::Area = 1.0 [m^2];
            feature E: SI::Illuminance = I/A;"""


        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("E")!!.aadd().getRange().min, 0.0001)
        assertEquals("cd / m^2", global.resolveVar("E")!!.vectorQuantity.unit.toString())
        assertEquals("Illuminance", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("LuminousIntensity", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun inductanceTest() = testSession("SI") {
        loadKerML(
           """
            feature I: SI::ElectricCurrent = 1000.0 [mA];
            feature W: SI::MagneticFlux = 1.0 [Wb];
            feature L: SI::Inductance = W/I;"""
            )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("L")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m^2 / A^2 s^2", global.resolveVar("L")!!.vectorQuantity.unit.toString())
        assertEquals("Inductance", global.resolveVar("L")!!.vectorQuantity.getDimension())
        assertEquals("ElectricCurrent", global.resolveVar("I")!!.vectorQuantity.getDimension())
        assertEquals("MagneticFlux", global.resolveVar("W")!!.vectorQuantity.getDimension())
    }

    @Test
    fun kinematicViscosityTest() = testSession("SI") {
        loadKerML("""
                feature A: SI::Area = 1.0 [m^2];
                feature t: SI::Time = 1.0 [s];
                feature v: SI::KinematicViscosity = A/t;""" )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("v")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2 / s", global.resolveVar("v")!!.vectorQuantity.unit.toString())
        assertEquals("KinematicViscosity", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDimension())
    }

    @Test
    fun luminanceTest() = testSession("SI") {
        loadKerML("""
            feature I: SI::LuminousIntensity = 1.0 [cd];
            feature A: SI::Area = 1.0 [m^2];
            feature v: SI::Luminance = I/A;""" )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("I")!!.aadd().getRange().min, 0.0001)
        assertEquals("cd", global.resolveVar("I")!!.vectorQuantity.unit.toString())
        assertEquals("Luminance", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("LuminousIntensity", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun luminousEfficacyTest() = testSession("SI") {
        loadKerML(
            """
            feature P: SI::Power = 1.0 [W];
            feature A: SI::LuminousFlux  = 1.0 [lm];
            feature K: SI::LuminousEfficacy = A/P;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("K")!!.aadd().getRange().min, 0.0001)
        assertEquals("cd s^3 / kg m^2", global.resolveVar("K")!!.vectorQuantity.unit.toString())
        assertEquals("LuminousEfficacy", global.resolveVar("K")!!.vectorQuantity.getDimension())
        assertEquals("LuminousFlux", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("Power", global.resolveVar("P")!!.vectorQuantity.getDimension())
    }

    @Test
    fun luminousEnergyTest() = testSession("SI") {
        loadKerML(
            """
            feature t: SI::Time = 1.0 [s];
            feature A: SI::LuminousFlux = 1.0 [lm];
            feature Q: SI::LuminousEnergy = t*A."""

        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("Q")!!.aadd().getRange().min, 0.0001)
        assertEquals("cd s", global.resolveVar("Q")!!.vectorQuantity.unit.toString())
        assertEquals("LuminousEnergy", global.resolveVar("Q")!!.vectorQuantity.getDimension())
        assertEquals("LuminousFlux", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
    }

    @Test  //Ambiguity with cd
    fun luminousFluxTest() = testSession("SI") {
        loadKerML("""
            feature t: SI::LuminousFlux = 1.0 [lm];""")
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("t")!!.aadd().getRange().min, 0.0001)
        assertEquals("cd", global.resolveVar("t")!!.vectorQuantity.unit.toString())
        assertEquals("LuminousFlux", global.resolveVar("t")!!.vectorQuantity.getDimension())
    }

    @Test
    fun magneticFluxTest() = testSession("SI") {
        loadKerML("""
            feature U: SI::Voltage  = 1.0 [V] ;
            feature t: SI::Time = 1.0 [s] ;
            feature Phi: SI::MagneticFlux = U*t;""")
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("Phi")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m^2 / A s^2", global.resolveVar("Phi")!!.vectorQuantity.unit.toString())
        assertEquals("MagneticFlux", global.resolveVar("Phi")!!.vectorQuantity.getDimension())
        assertEquals("Voltage", global.resolveVar("U")!!.vectorQuantity.getDimension())
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
    }

    @Test
    fun magneticFluxDensityTest() = testSession("SI") {
        loadKerML("""
            feature Phi: SI::MagneticFlux  = 1.0 [Wb];
            feature t: SI::Area = 1.0 [m^2];
            feature B: SI::MagneticFluxDensity = Phi/t;""")
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("B")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / A s^2", global.resolveVar("B")!!.vectorQuantity.unit.toString())
        assertEquals("MagneticFluxDensity", global.resolveVar("B")!!.vectorQuantity.getDimension())
        assertEquals("Area", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("MagneticFlux", global.resolveVar("Phi")!!.vectorQuantity.getDimension())
    }

    @Test
    fun MassFlowTest() = testSession("SI") {
        loadKerML("""
            feature m: SI::Mass = 1.0 [kg];
            feature t: SI::Time = 1.0 [s];
            feature B: SI::MassFlow = m/t;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("B")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / s", global.resolveVar("B")!!.vectorQuantity.unit.toString())
        assertEquals("MassFlow", global.resolveVar("B")!!.vectorQuantity.getDimension())
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
    }

    @Test
    fun momentOfForceTest() = testSession("SI") {
        loadKerML(
           """
            feature l: SI::Length  = 1.0 [m];
            feature F: SI::Force = 1.0 [N];
            feature B: SI::MomentOfForce = l*F;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("B")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m^2 / s^2", global.resolveVar("B")!!.vectorQuantity.unit.toString())
        assertEquals("MomentOfForce", global.resolveVar("B")!!.vectorQuantity.getDimension())
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("l")!!.vectorQuantity.getDimension())
    }

    @Test
    fun momentOfInertiaTest() = testSession("SI") {
        loadKerML(
            """
            feature m: SI::Mass = 1.0 [kg];
            feature A: SI::Area = 1.0 [m^2];
            feature I: SI::MomentOfInertia = m*A;"""

        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("I")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m^2", global.resolveVar("I")!!.vectorQuantity.unit.toString())
        assertEquals("MomentOfInertia", global.resolveVar("I")!!.vectorQuantity.getDimension())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
    }

    @Test
    fun momentumTest() = testSession("SI") {
        loadKerML(
            """
            feature m: SI::Mass = 1.0 [kg];
            feature v: SI::Speed = 1.0 [m/s];
            feature p: SI::Momentum = m*v;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("p")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m / s", global.resolveVar("p")!!.vectorQuantity.unit.toString())
        assertEquals("Momentum", global.resolveVar("p")!!.vectorQuantity.getDimension())
        assertEquals("Speed", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("Mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
    }

    //Ambiguity Time, period
    @Test
    fun permittivityTest() = testSession("SI") {
        loadKerML(
            """
            feature I: SI::Capacitance = 1.0 [F];
            feature l: SI::Length = 1.0 [m];
            feature epsilon: SI::Permittivity = I/l;"""


        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("epsilon")!!.aadd().getRange().min, 0.0001)
        assertEquals("A^2 s^4 / kg m^3", global.resolveVar("epsilon")!!.vectorQuantity.unit.toString())
        assertEquals("Permittivity", global.resolveVar("epsilon")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("l")!!.vectorQuantity.getDimension())
        assertEquals("Capacitance", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun PowerTest() = testSession("SI") {
        loadKerML(
            """
            feature E: SI::Energy  = 1.0 [J];
            feature t: SI::Time = 1.0 [s]; 
            feature P: SI::Power = E/t; 
            """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("P")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m^2 / s^3", global.resolveVar("P")!!.vectorQuantity.unit.toString())
        assertEquals("Power", global.resolveVar("P")!!.vectorQuantity.getDimension())
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("Energy", global.resolveVar("E")!!.vectorQuantity.getDimension())
    }

    @Test
    fun PowerDensityTest() = testSession("SI") {
        loadKerML(
            """
            feature P: SI::Power = 1.0 [W];
            feature V: SI::Volume = 1.0 [m^3];
            feature PD: SI::PowerDensity = P/V;"""

        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("PD")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / m s^3", global.resolveVar("PD")!!.vectorQuantity.unit.toString())
        assertEquals("PowerDensity", global.resolveVar("PD")!!.vectorQuantity.getDimension())
        assertEquals("Volume", global.resolveVar("V")!!.vectorQuantity.getDimension())
        assertEquals("Power", global.resolveVar("P")!!.vectorQuantity.getDimension())
    }

    @Test
    fun pressureTest() = testSession("SI") {
        loadKerML("""
            feature F: SI::Force  = 1.0 [N];
            feature A: SI::Area = 1.0 [m^2];
            feature p: SI::Pressure  = F/A;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("p")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / m s^2", global.resolveVar("p")!!.vectorQuantity.unit.toString())
        assertEquals("Pressure", global.resolveVar("p")!!.vectorQuantity.getDimension())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDimension())
    }

    @Test
    fun pressureTest2() = testSession("SI") {
        loadKerML("""
            feature F: SI::Force = 1.0 [N];
            feature A: SI::Area = 1.0 [m^2];
            feature p: SI::Pressure = F/A {:>> unit = "mbar";}
            feature p2: SI::Pressure = F/A;"""

        )

        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("p2")!!.aadd().getRange().min, 0.0001)
        assertEquals(0.01, global.resolveVar("p")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / m s^2", global.resolveVar("p")!!.vectorQuantity.unit.toString())
        assertEquals("Pressure", global.resolveVar("p")!!.vectorQuantity.getDimension())
        assertEquals("Area", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("Force", global.resolveVar("F")!!.vectorQuantity.getDimension())
    }

    @Test
    fun quantityOfDimensionOne() = testSession("SI") {
        loadKerML("""
            feature E: ScalarValues::Real = 100.0;
            feature p: SI::Quantity  = E {:>> unit = "%";}
            feature E2: ScalarValues::Real = p;
            feature f: SI::Quantity  = E {:>> unit = "dB";}
            """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(10000.0, global.resolveVar("p")!!.aadd().getRange().min, 0.0001)
        assertEquals(20.0, global.resolveVar("f")!!.aadd().getRange().min, 0.0001)
        assertEquals(100.0, global.resolveVar("E2")!!.aadd().getRange().min, 0.0001)
        assertEquals("1", global.resolveVar("p")!!.vectorQuantity.unit.toString())
        assertEquals("QuantityOfDimensionOne", global.resolveVar("p")!!.vectorQuantity.getDimension())
        assertEquals("QuantityOfDimensionOne", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("QuantityOfDimensionOne", global.resolveVar("E2")!!.vectorQuantity.getDimension())
        assertEquals("QuantityOfDimensionOne", global.resolveVar("f")!!.vectorQuantity.getDimension())
    }

    @Test
    fun SpeedTest() = testSession("SI") {
        loadKerML("""
            feature l: SI::Length = 10.0 [m] ;
            feature t: SI::Time = 1.0 [s];
            feature v1: SI::Speed = l/t;
            feature v2: SI::Speed = l/t{:>> unit = "km/h";}
            """
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(10.0, global.resolveVar("v1")!!.aadd().getRange().min, 0.0001)
        assertEquals(36.0, global.resolveVar("v2")!!.aadd().getRange().min, 0.0001)
        assertEquals("m / s", global.resolveVar("v1")!!.vectorQuantity.unit.toString())
        assertEquals("Speed", global.resolveVar("v1")!!.vectorQuantity.getDimension())
        assertEquals("Speed", global.resolveVar("v2")!!.vectorQuantity.getDimension())
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("l")!!.vectorQuantity.getDimension())
    }

    @Test
    fun VolumeTest() = testSession("SI") {
        loadKerML("""
            feature l1: SI::Length = 1.0 [m];
            feature l2: SI::Length = 100.0 [cm];
            feature l3: SI::Length = 10.0 [dm];
            feature V1: SI::Volume = l1*l2*l3;
            feature V2: SI::Volume = l1*l2*l3 {:>> unit = "l";}
            
            """

        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("V1")!!.aadd().getRange().min, 0.0001)
        assertEquals(1000.0, global.resolveVar("V2")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^3", global.resolveVar("V1")!!.vectorQuantity.unit.toString())
        assertEquals("Volume", global.resolveVar("V1")!!.vectorQuantity.getDimension())
        assertEquals("Volume", global.resolveVar("V2")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("l1")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("l2")!!.vectorQuantity.getDimension())
        assertEquals("Length", global.resolveVar("l3")!!.vectorQuantity.getDimension())
    }

    @Test
    fun informationCapacityTest() = testSession("SI") {
        loadKerML(
            """
            feature i1: SI::InformationCapacity  = 5000000.0 [bit];
            feature i2: SI::InformationCapacity  = 30.0 [kB];
            feature t: SI::Time  = 10.0 [s];
            feature BR1: SI::BitRate = i1/t {:>> unit = "Mbps";}
            feature BR2: SI::BitRate = i2/t{:>> unit = "kB/s";}"""

        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0.5, global.resolveVar("BR1")!!.aadd().getRange().min, 0.0001)
        assertEquals(3.0, global.resolveVar("BR2")!!.aadd().getRange().min, 0.0001)
        assertEquals("InformationCapacity", global.resolveVar("i1")!!.vectorQuantity.getDimension())
        assertEquals("InformationCapacity", global.resolveVar("i2")!!.vectorQuantity.getDimension())
        assertEquals("Time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("BitRate", global.resolveVar("BR1")!!.vectorQuantity.getDimension())
        assertEquals("BitRate", global.resolveVar("BR2")!!.vectorQuantity.getDimension())
    }

    @Test
    fun multipleOperationsTest() = testSession("SI") {
        loadKerML("""
              private import SI::*;       
              feature plugCosts: Time = 2.0 [s]; 
        
            """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun defineDimensionTest1() = testSession("SI") {
        loadKerML("""
            
             feature Mass: SI::Mass = 10.0 [kg];
            """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals("Mass", global.resolveVar("Mass")!!.vectorQuantity.unit.unitDimension)
    }

    @Test
    fun defineDimensionTest2() = testSession("SI") {
        loadKerML("""
            feature Mass: SI::Mass = 10.0 [kg];
            """
          )
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals("kg", global.resolveVar("Mass")!!.vectorQuantity.unit.toString())
    }

    @Test
    fun defineDimensionTestWrongUnit() = testSession("SI") {
        loadKerML("""
            feature Mass: SI::Mass = 10.0 [m];
            """
            )
        propagate()
        assertEquals(1, status.issues.size)
        assertEquals("Unit of Mass (kg) does not match the unit of the dependency (m) in element Mass", status.issues.elementAt(0).message)
    }

}