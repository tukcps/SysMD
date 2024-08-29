package quantitytests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class QuantityTestsWithFormulas {

    /** Kinematic Formulars */
    @Test
    fun unitskinematik1() = testSession {
        loadSysMD("""
            Value t: ScalarValues::Real [s] = 2.0 [s].
            Value v: ScalarValues::Real[m/s] = 5.0 [m/s].
            Value s: ScalarValues::Real[m] = v*t."""
        )
        propagate()
        assertEquals("m", global.resolveVar("s")!!.vectorQuantity.unit.toString())
        assertEquals(10.0, global.resolveVar("s")!!.aadd().getRange().max, 0.00001)
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("speed", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun unitskinematik2() = testSession {
        loadSysMD("""
                Value t: ScalarValues::Real [ms] = 2000.0 [ms].
                Value v: ScalarValues::Real[km/h] = 36.0 [km/h].
                Value v2: ScalarValues::Real[m/s] = v.
                Value s: ScalarValues::Real[m] = v2*t."""
        )
        propagate()
        assertEquals("m / s", global.resolveVar("v2")!!.vectorQuantity.unit.toString())
        assertEquals(10.0, global.resolveVar("v2")!!.aadd().getRange().min, 0.00001)
        assertEquals(20.0, global.resolveVar("s")!!.aadd().getRange().max, 0.00001)
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("speed", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("speed", global.resolveVar("v2")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    @Test
    fun unitskinematik3() = testSession {
        loadSysMD("""
            Value t: ScalarValues::Real[ms] = 2000.0 [ms].
            Value v: ScalarValues::Real[km/h] = 36.0 [km/h].
            Value s: ScalarValues::Real[m] = v*t."""
        )
        propagate()
        assertEquals("m / s", global.resolveVar("v")!!.vectorQuantity.unit.toString())
        assertEquals(2000.0, global.resolveVar("t")!!.aadd().getRange().min, 0.00001)
        assertEquals(36.0, global.resolveVar("v")!!.aadd().getRange().max, 0.00001)
        assertEquals(20.0, global.resolveVar("s")!!.aadd().getRange().min, 0.00001)
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("speed", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    // Test Fails caused by parser error with sqr, works without adding v0*t
    @Test
    fun unitskinematik4() = testSession {
        loadSysMD("""
                Value t: ScalarValues::Real [s] = 2.0 [s].
                Value v0: ScalarValues::Real[m/s] = 1.0 [m/s].
                Value a: ScalarValues::Real[m/s^2] = 1.0 [m/s^2].
                Value s: ScalarValues::Real[m] = 0.5*a*sqr(t)+v0*t."""
        )
        propagate()
        assertEquals("m / s", global.resolveVar("v0")!!.vectorQuantity.unit.toString())
        assertEquals(4.0, global.resolveVar("s")!!.aadd().getRange().min, 0.00001)
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("speed", global.resolveVar("v0")!!.vectorQuantity.getDimension())
        assertEquals("acceleration", global.resolveVar("a")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun unitskinematik5() = testSession {
        loadSysMD("""
            Value t: ScalarValues::Real [s] = 1.0 [s].
            Value v: ScalarValues::Real[m/s] = 3.0 [m/s].
            Value g: ScalarValues::Real[m/s^2] = 4.0 [m/s^2].
            Value s: ScalarValues::Real[m/s] = sqrt(sqr(v)+sqr(g)*sqr(t))."""
        )
        propagate()
        assertEquals(5.0, global.resolveVar("s")!!.aadd().getRange().min, 0.00001)
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("speed", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("acceleration", global.resolveVar("g")!!.vectorQuantity.getDimension())
        assertEquals("speed", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun unitskinematik6() = testSession {
        loadSysMD("""
            Value h: ScalarValues::Real [dm] = 100.0 [dm].
            Value v0: ScalarValues::Real[m/s] = 3.0 [m/s].
            Value g: ScalarValues::Real[m/s^2] = 5.0 [m/s^2].
            Value s: ScalarValues::Real[m] = v0*sqrt(2.0*h/g). """.trimIndent()
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(6.0, global.resolveVar("s")!!.aadd().getRange().min, 0.00001)
        assertEquals("length", global.resolveVar("h")!!.vectorQuantity.getDimension())
        assertEquals("speed", global.resolveVar("v0")!!.vectorQuantity.getDimension())
        assertEquals("acceleration", global.resolveVar("g")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("s")!!.vectorQuantity.getDimension())
    }

    @Test
    fun unitskinematik7() = testSession {
        loadSysMD("""
            Value r: ScalarValues::Real [cm] = 100.0 [cm].
            Value Omega: ScalarValues::Real[1/s] = 3.0 [Hz].
            Value m: ScalarValues::Real[kg] = 5.0 [kg].
            Value Fz: ScalarValues::Real[N] = m*sqr(Omega)*r."""
        )
        propagate()
        assertEquals(45.0, global.resolveVar("Fz")!!.aadd().getRange().min, 0.00001)
        assertEquals("length", global.resolveVar("r")!!.vectorQuantity.getDimension())
        assertEquals("frequency", global.resolveVar("Omega")!!.vectorQuantity.getDimension())
        assertEquals("mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
        assertEquals("force", global.resolveVar("Fz")!!.vectorQuantity.getDimension())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun unitskinematik8() = testSession {
        loadSysMD("""
            Value r: ScalarValues::Real [cm] = 1.0 [m].
            Value Omega: ScalarValues::Real[1/h] = 3.0 [Hz].
            Value m: ScalarValues::Real[g] = 5.0 [kg].
            Value Fz: ScalarValues::Real[N] = m*sqr(Omega)*r. """
        )
        propagate()
        assertEquals(45.0, global.resolveVar("Fz")!!.aadd().getRange().min, 0.00001)
        assertEquals("length", global.resolveVar("r")!!.vectorQuantity.getDimension())
        assertEquals("frequency", global.resolveVar("Omega")!!.vectorQuantity.getDimension())
        assertEquals("mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
        assertEquals("force", global.resolveVar("Fz")!!.vectorQuantity.getDimension())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /**Units with energy**/
    @Test
    fun unitsEnergy1() = testSession {
        loadSysMD("""
            Value F: ScalarValues::Real [N] = 15.0 [N].
            Value s: ScalarValues::Real[m] = 3.0 [m].
            Value E: ScalarValues::Real[J] = F*s ."""
        )
        propagate()
        assertEquals(45.0, global.resolveVar("E")!!.aadd().getRange().min, 0.00001)
        assertEquals("force", global.resolveVar("F")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals("energy", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun unitsEnergy2() = testSession {
        loadSysMD("""
            Value m: ScalarValues::Real [kg] = 3.0 [kg].
            Value v: ScalarValues::Real[m/s] = 15.0 [m/s].
            Value p: ScalarValues::Real[kg m / s] = m * v."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(45.0, global.resolveVar("p")!!.aadd().getRange().min, 0.00001)
        assertEquals("mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
        assertEquals("speed", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("momentum", global.resolveVar("p")!!.vectorQuantity.getDimension())
    }

    /**Units with gravitation**/
    @Test
    fun unitsGravitation() = testSession {
        loadSysMD("""
            Value gamma: ScalarValues::Real [m^3/kg s^2] = 3.0 [m^3/kg s^2].
            Value m1: ScalarValues::Real[kg] = 4.0 [kg].
            Value m2: ScalarValues::Real[kg] = 2.0 [kg].
            Value r1: ScalarValues::Real[m] = 2.0 [m].
            Value r2: ScalarValues::Real[m] = 4.0 [m].
            Value Epot: ScalarValues::Real[J] = gamma * m1 * m2 * (1.0/r1 + 1.0/r2)."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(18.0, global.resolveVar("Epot")!!.aadd().getRange().min, 0.00001)
        assertEquals("mass", global.resolveVar("m1")!!.vectorQuantity.getDimension())
        assertEquals("mass", global.resolveVar("m2")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("r1")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("r2")!!.vectorQuantity.getDimension())
        assertEquals("energy", global.resolveVar("Epot")!!.vectorQuantity.getDimension())
    }

    /**Units with electricity**/
    @Test
    fun unitsElectricity1() = testSession {
        loadSysMD(
            """
            Value epsilon0: ScalarValues::Real [A^2 s^2 / N m^2] = 3.0 [A^2 s^2 / N m^2].
            Value Q: ScalarValues::Real[C] = 40.0 [C].
            Value r: ScalarValues::Real[m] = 0.2 [m].
            Value E: ScalarValues::Real[V / m] = 1.0/(4.0 * 3.14159*epsilon0) * Q/sqr(r). """
        )
        propagate()
        assertEquals(26.5258, global.resolveVar("E")!!.aadd().getRange().min, 0.0001)
        assertEquals("electric charge", global.resolveVar("Q")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("r")!!.vectorQuantity.getDimension())
        assertEquals("electric field", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun unitsElectricity2() = testSession {
        loadSysMD(
            """
            Value epsilon0: ScalarValues::Real [A^2 s^2 / N m^2] = 3.0 [A^2 s^2 / N m^2].
            Value Q1: ScalarValues::Real[C] = 40.0 [C].
            Value Q2: ScalarValues::Real[C] = 1.0 [C].
            Value r: ScalarValues::Real[m] = 0.2 [m].
            Value F: ScalarValues::Real = 1.0/(4.0 * 3.14159*epsilon0) * (Q1*Q2)/sqr(r) ."""
        )
        propagate() // Strange, destroys an already correct result.
        assertEquals(26.5258, global.resolveVar("F")!!.aadd().getRange().min, 0.0001)
        assertEquals("electric charge", global.resolveVar("Q1")!!.vectorQuantity.getDimension())
        assertEquals("electric charge", global.resolveVar("Q2")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("r")!!.vectorQuantity.getDimension())
        assertEquals("force", global.resolveVar("F")!!.vectorQuantity.getDimension())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun unitsElectricity3() = testSession {
        loadSysMD(
            """Value epsilon0: ScalarValues::Real [A^2 s^2 / N m^2] = 3.0 [A^2 s^2 / N m^2].
            Value E: ScalarValues::Real[V / m] = 2.0 [V / m].
            Value q: ScalarValues::Real[A s] = 5.0 [A s].
            Value s: ScalarValues::Real[m] = 0.2 [m].
            Value W: ScalarValues::Real[J] = E*q*s ."""
        )
        propagate()
        assertEquals("electric field", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("electric charge", global.resolveVar("q")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("s")!!.vectorQuantity.getDimension())
        assertEquals("energy", global.resolveVar("W")!!.vectorQuantity.getDimension())
        assertEquals(2.0, global.resolveVar("W")!!.aadd().getRange().min, 0.0001)
    }

    @Test
    fun unitsElectricity4() = testSession {
        loadSysMD(
            """
            Value epsilon0: ScalarValues::Real [A^2 s^2 / N m^2] = 3.0 [A^2 s^2 / N m^2].
            Value E: ScalarValues::Real[V / m] = 2.0 [V / m].
            Value d: ScalarValues::Real[m] = 0.2 [m].
            Value U: ScalarValues::Real[V] = E*d. """
        )
        propagate()
        assertEquals(0.4, global.resolveVar("U")!!.aadd().getRange().min, 0.0001)
        assertEquals("electric field", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("d")!!.vectorQuantity.getDimension())
        assertEquals("electric potential difference", global.resolveVar("U")!!.vectorQuantity.getDimension())
    }

    @Test
    fun unitsElectricity5() = testSession {
        loadSysMD(
            """Value epsilon0: ScalarValues::Real [A^2 s^2 / N m^2] = 3.0 [A^2 s^2 / N m^2].
                Value E: ScalarValues::Real[V / m] = 2.0 [V / m].
                Value d: ScalarValues::Real[m] = 0.2 [m].
                Value U: ScalarValues::Real[V] = E*d. """
        )
        propagate()
        assertEquals(0.4, global.resolveVar("U")!!.aadd().getRange().min, 0.0001)
        assertEquals("electric field", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("d")!!.vectorQuantity.getDimension())
        assertEquals("electric potential difference", global.resolveVar("U")!!.vectorQuantity.getDimension())
    }

    @Test
    fun unitsElectricity6() = testSession {
        loadSysMD(
            """Value epsilon0: ScalarValues::Real [A^2 s^2 / N m^2] = 3.0 [A^2 s^2 / N m^2].
            Value Q1: ScalarValues::Real[C] = 3.14159 [C].
            Value Q2: ScalarValues::Real[C] = 3000.0 [mC].
            Value r1: ScalarValues::Real[m] = 1.0 [m].
            Value r2: ScalarValues::Real[m] = 50.0 [cm].
            Value f1: ScalarValues::Real[1/m] =(1.0/r1+1.0/r2) .
            Value f2: ScalarValues::Real =Q1*Q2/(epsilon0).
            Value W: ScalarValues::Real[J] = (Q1*Q2)/(3.0*3.14159*epsilon0)*(1.0/r1+1.0/r2). """
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("W")!!.aadd().getRange().min, 0.0001)
        assertEquals("electric charge", global.resolveVar("Q1")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("r1")!!.vectorQuantity.getDimension())
        assertEquals("energy", global.resolveVar("W")!!.vectorQuantity.getDimension())
    }

    @Test
    fun conversionTest1() = testSession {
        loadSysMD(
            """
             Value t1: ScalarValues::Real [h^2] = 1.0 [h^2].
            Value t2: ScalarValues::Real [min^2] = t1."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(3600.0, global.resolveVar("t2")!!.aadd().getRange().max, 0.0001)
    }

    @Test
    fun conversionTest2() = testSession {
        loadSysMD(
            """
            Value t1: ScalarValues::Real [km/min^2] = 1.0 [km/min^2].
            Value t2: ScalarValues::Real [m/s^2] = t1."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0.2777777, global.resolveVar("t2")!!.aadd().getRange().min, 0.0001)
    }

    @Test
    fun conversionTest3() = testSession {
        loadSysMD(
            """
                Value t1: ScalarValues::Real [N/m^2] = 1.0 [N/m^2].
                Value t2: ScalarValues::Real [mN/dm^2] = t1."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(10.0, global.resolveVar("t2")!!.aadd().getRange().min, 0.0001)

    }

    @Test
    fun absorbedDoseTest() = testSession {
        loadSysMD(
            """
            Value E: ScalarValues::Real [J] = 1.0 [J].
            Value w: ScalarValues::Real [kg] = 5.0 [kg].
            Value G: ScalarValues::Real [Gy] = E/w."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0.2, global.resolveVar("G")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2 / s^2", global.resolveVar("G")!!.vectorQuantity.unit.toString())
        assertEquals("absorbed dose", global.resolveVar("G")!!.vectorQuantity.getDimension())
    }

    @Test
    fun activityTest() = testSession {
        loadSysMD(
            """
            Value t1: ScalarValues::Real [s] = 1.0 [s].
            Value A: ScalarValues::Real [Bq] = 1.0/t1."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("A")!!.aadd().getRange().min, 0.0001)
        assertEquals("1 / s", global.resolveVar("A")!!.vectorQuantity.unit.toString())
        assertEquals("activity", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("time", global.resolveVar("t1")!!.vectorQuantity.getDimension())
    }

    @Test
    fun areatest() = testSession {
        loadSysMD(
            """
            Value l1: ScalarValues::Real [dm] = 10.0 [dm].
            Value l2: ScalarValues::Real [cm] = 50.0 [cm].
            Value A: ScalarValues::Real [m^2] = l1*l2."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0.5, global.resolveVar("A")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2", global.resolveVar("A")!!.vectorQuantity.unit.toString())
        assertEquals("area", global.resolveVar("A")!!.vectorQuantity.getDimension())
    }

    @Test
    fun capacitanceTest() = testSession {
        loadSysMD(
            """Value Q: ScalarValues::Real [C] = 10.0 [C].
            Value U: ScalarValues::Real [V] = 5.0 [V].
            Value C: ScalarValues::Real [F] = Q/U."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(2.0, global.resolveVar("C")!!.aadd().getRange().min, 0.0001)
        assertEquals("s^4 A^2 / m^2 kg", global.resolveVar("C")!!.vectorQuantity.unit.toString())
        assertEquals("capacitance", global.resolveVar("C")!!.vectorQuantity.getDimension())
        assertEquals("electric charge", global.resolveVar("Q")!!.vectorQuantity.getDimension())
        assertEquals("electric potential difference", global.resolveVar("U")!!.vectorQuantity.getDimension())
    }

    @Test
    fun catalyticActivityTest() = testSession {
        loadSysMD(
            """
            Value Q: ScalarValues::Real [mol] = 60.0 [mol].
            Value t: ScalarValues::Real [min] = 1.0 [min].
            Value K: ScalarValues::Real [kat] = Q/t."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("K")!!.aadd().getRange().min, 0.0001)
        assertEquals("mol / s", global.resolveVar("K")!!.vectorQuantity.unit.toString())
        assertEquals("catalytic activity", global.resolveVar("K")!!.vectorQuantity.getDimension())
        assertEquals("amount of substance", global.resolveVar("Q")!!.vectorQuantity.getDimension())
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
    }

    @Test
    fun density() = testSession {
        loadSysMD(
            """
            Value m: ScalarValues::Real [g] = 2.0 [g].
            Value V: ScalarValues::Real [dm^3] = 1.0 [dm^3].
            Value d: ScalarValues::Real [kg/m^3] = m/V."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(2.0, global.resolveVar("d")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / m^3", global.resolveVar("d")!!.vectorQuantity.unit.toString())
        assertEquals("density", global.resolveVar("d")!!.vectorQuantity.getDimension())
        assertEquals("volume", global.resolveVar("V")!!.vectorQuantity.getDimension())
        assertEquals("mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
    }

    @Test
    fun electricalConductanceTest() = testSession {
        loadSysMD(
            """Value I: ScalarValues::Real [A] = 1000.0 [mA].
            Value V: ScalarValues::Real [V] = 1.0 [V].
            Value G: ScalarValues::Real [S] = I/V."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("G")!!.aadd().getRange().min, 0.0001)
        assertEquals("A^2 s^3 / m^2 kg", global.resolveVar("G")!!.vectorQuantity.unit.toString())
        assertEquals("electrical conductance", global.resolveVar("G")!!.vectorQuantity.getDimension())
        assertEquals("electric potential difference", global.resolveVar("V")!!.vectorQuantity.getDimension())
        assertEquals("electric current", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun electricalresistanceTest() = testSession {
        loadSysMD(
            """
            Value I: ScalarValues::Real [A] = 1000.0 [mA].
            Value U: ScalarValues::Real [V] = 1.0 [V].
            Value R: ScalarValues::Real [Ohm] = U/I."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("R")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2 kg / s^3 A^2", global.resolveVar("R")!!.vectorQuantity.unit.toString())
        assertEquals("electrical resistance", global.resolveVar("R")!!.vectorQuantity.getDimension())
        assertEquals("electric potential difference", global.resolveVar("U")!!.vectorQuantity.getDimension())
        assertEquals("electric current", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun electrichargeTest() = testSession {
        loadSysMD(
            """
            Value I: ScalarValues::Real [A] = 1000.0 [mA].
            Value t: ScalarValues::Real [s] = 1.0 [s].
            Value Q: ScalarValues::Real [C] = t*I."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("Q")!!.aadd().getRange().min, 0.0001)
        assertEquals("s A", global.resolveVar("Q")!!.vectorQuantity.unit.toString())
        assertEquals("electric charge", global.resolveVar("Q")!!.vectorQuantity.getDimension())
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("electric current", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun electricPotentialDifferenceTest() = testSession {
        loadSysMD(
            """
            Value I: ScalarValues::Real [A] = 1000.0 [mA].
            Value P: ScalarValues::Real [W] = 1.0 [W].
            Value U: ScalarValues::Real [V] = P/I."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("U")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2 kg / s^3 A", global.resolveVar("U")!!.vectorQuantity.unit.toString())
        assertEquals("electric potential difference", global.resolveVar("U")!!.vectorQuantity.getDimension())
        assertEquals("power", global.resolveVar("P")!!.vectorQuantity.getDimension())
        assertEquals("electric current", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun energyTest() = testSession {
        +"""
            Value F: ScalarValues::Real [N] = 1000.0 [mN];
            Value l: ScalarValues::Real [m] = 1.0 [m];
            Value E: ScalarValues::Real [J] = F*l;"""
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("E")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2 kg / s^2", global.resolveVar("E")!!.vectorQuantity.unit.toString())
        assertEquals("energy", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("l")!!.vectorQuantity.getDimension())
        assertEquals("force", global.resolveVar("F")!!.vectorQuantity.getDimension())
    }

    @Test
    fun energyDensityTest() = testSession {
        loadSysMD(
            """
            Value E: ScalarValues::Real [J] = 1.0 [J].
            Value V: ScalarValues::Real [m^3] = 1.0 [m^3].
            Value ED: ScalarValues::Real [J/m^3] = E/V."""
        )

        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("ED")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / m s^2", global.resolveVar("ED")!!.vectorQuantity.unit.toString())
        assertEquals("energy density", global.resolveVar("ED")!!.vectorQuantity.getDimension())
        assertEquals("energy", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("volume", global.resolveVar("V")!!.vectorQuantity.getDimension())
    }

    @Test
    fun entropyTest() = testSession {
        loadSysMD(
            """
            Value E: ScalarValues::Real [J] = 1.0 [J].
            Value T: ScalarValues::Real [K] = 1.0 [K].
            Value S: ScalarValues::Real [J/K] = E/T."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("S")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2 kg / s^2 K", global.resolveVar("S")!!.vectorQuantity.unit.toString())
        assertEquals("entropy", global.resolveVar("S")!!.vectorQuantity.getDimension())
        assertEquals("temperature", global.resolveVar("T")!!.vectorQuantity.getDimension())
        assertEquals("energy", global.resolveVar("E")!!.vectorQuantity.getDimension())
    }

    @Test
    fun forceTest() = testSession {
        loadSysMD(
            """Value m: ScalarValues::Real [kg] = 1.0 [kg].
            Value a: ScalarValues::Real [m/s^2] = 1.0 [m/s^2].
            Value F: ScalarValues::Real [N] = m*a."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("F")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m / s^2", global.resolveVar("F")!!.vectorQuantity.unit.toString())
        assertEquals("force", global.resolveVar("F")!!.vectorQuantity.getDimension())
        assertEquals("acceleration", global.resolveVar("a")!!.vectorQuantity.getDimension())
        assertEquals("mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
    }

    @Test
    fun frequencyTest() = testSession {
        loadSysMD("""
            Value t: ScalarValues::Real [s] = 1.0 [s].
            Value f: ScalarValues::Real [1/s] = 1.0/t."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("f")!!.aadd().getRange().min, 0.0001)
        assertEquals("1 / s", global.resolveVar("f")!!.vectorQuantity.unit.toString())
        assertEquals("frequency", global.resolveVar("f")!!.vectorQuantity.getDimension())
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
    }

    @Test
    fun illuminanceTest() = testSession {
        loadSysMD("""
            Value I: ScalarValues::Real [cd] = 1.0 [cd].
            Value A: ScalarValues::Real [m^2] = 1.0 [m^2].
            Value E: ScalarValues::Real [lx] = I/A."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("E")!!.aadd().getRange().min, 0.0001)
        assertEquals("cd / m^2", global.resolveVar("E")!!.vectorQuantity.unit.toString())
        assertEquals("illuminance", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("area", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("luminous intensity", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun inductanceTest() = testSession {
        loadSysMD("""
            Value I: ScalarValues::Real [A] = 1000.0 [mA].
            Value W: ScalarValues::Real [Wb] = 1.0 [Wb].
            Value L: ScalarValues::Real [H] = W/I.""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("L")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2 kg / s^2 A^2", global.resolveVar("L")!!.vectorQuantity.unit.toString())
        assertEquals("inductance", global.resolveVar("L")!!.vectorQuantity.getDimension())
        assertEquals("electric current", global.resolveVar("I")!!.vectorQuantity.getDimension())
        assertEquals("magnetic flux", global.resolveVar("W")!!.vectorQuantity.getDimension())
    }

    @Test
    fun kinematicViscosityTest() = testSession {
        loadSysMD("""
                Value A: ScalarValues::Real [m^2] = 1.0 [m^2].
                Value t: ScalarValues::Real [s] = 1.0 [s].
                Value v: ScalarValues::Real [St] = A/t.""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("v")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2 / s", global.resolveVar("v")!!.vectorQuantity.unit.toString())
        assertEquals("kinematic viscosity", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("area", global.resolveVar("A")!!.vectorQuantity.getDimension())
    }

    @Test
    fun luminanceTest() = testSession {
        loadSysMD("""
            Value I: ScalarValues::Real [cd] = 1.0 [cd].
            Value A: ScalarValues::Real [m^2] = 1.0 [m^2].
            Value v: ScalarValues::Real [sb] = I/A.""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("I")!!.aadd().getRange().min, 0.0001)
        assertEquals("cd", global.resolveVar("I")!!.vectorQuantity.unit.toString())
        assertEquals("luminance", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("area", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("luminous intensity", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun luminousEfficacyTest() = testSession {
        loadSysMD(
            """
            Value P: ScalarValues::Real [W] = 1.0 [W].
            Value A: ScalarValues::Real [lm] = 1.0 [lm].
            Value K: ScalarValues::Real [lm/W] = A/P."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("K")!!.aadd().getRange().min, 0.0001)
        assertEquals("cd s^3 / m^2 kg", global.resolveVar("K")!!.vectorQuantity.unit.toString())
        assertEquals("luminous efficacy", global.resolveVar("K")!!.vectorQuantity.getDimension())
        assertEquals("luminous flux", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("power", global.resolveVar("P")!!.vectorQuantity.getDimension())
    }

    @Test
    fun luminousEnergyTest() = testSession {
        loadSysMD(
            """
            Value t: ScalarValues::Real [s] = 1.0 [s].
            Value A: ScalarValues::Real [lm] = 1.0 [lm].
            Value Q: ScalarValues::Real [lm s] = t*A."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("Q")!!.aadd().getRange().min, 0.0001)
        assertEquals("s cd", global.resolveVar("Q")!!.vectorQuantity.unit.toString())
        assertEquals("luminous energy", global.resolveVar("Q")!!.vectorQuantity.getDimension())
        assertEquals("luminous flux", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
    }

    @Test  //Ambiguity with cd
    fun luminousFluxTest() = testSession {
        loadSysMD("""
            Value t: ScalarValues::Real [lm] = 1.0 [lm].""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("t")!!.aadd().getRange().min, 0.0001)
        assertEquals("cd", global.resolveVar("t")!!.vectorQuantity.unit.toString())
        assertEquals("luminous flux", global.resolveVar("t")!!.vectorQuantity.getDimension())
    }

    @Test
    fun magneticFluxTest() = testSession {
        loadSysMD("""
            Value U: ScalarValues::Real [V] = 1.0 [V].
            Value t: ScalarValues::Real [s] = 1.0 [s].
            Value Phi: ScalarValues::Real [Wb] = U*t.""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("Phi")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2 kg / s^2 A", global.resolveVar("Phi")!!.vectorQuantity.unit.toString())
        assertEquals("magnetic flux", global.resolveVar("Phi")!!.vectorQuantity.getDimension())
        assertEquals("electric potential difference", global.resolveVar("U")!!.vectorQuantity.getDimension())
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
    }

    @Test
    fun magneticFluxDensityTest() = testSession {
        loadSysMD("""
            Value Phi: ScalarValues::Real [Wb] = 1.0 [Wb].
            Value t: ScalarValues::Real [m^2] = 1.0 [m^2].
            Value B: ScalarValues::Real [T] = Phi/t.""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("B")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / s^2 A", global.resolveVar("B")!!.vectorQuantity.unit.toString())
        assertEquals("magnetic flux density", global.resolveVar("B")!!.vectorQuantity.getDimension())
        assertEquals("area", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("magnetic flux", global.resolveVar("Phi")!!.vectorQuantity.getDimension())
    }

    @Test
    fun massFlowTest() = testSession {
        loadSysMD("""
            Value m: ScalarValues::Real [kg] = 1.0 [kg];
            Value t: ScalarValues::Real [s] = 1.0 [s];
            Value B: ScalarValues::Real [kg/s] = m/t;"""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("B")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / s", global.resolveVar("B")!!.vectorQuantity.unit.toString())
        assertEquals("mass flow", global.resolveVar("B")!!.vectorQuantity.getDimension())
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
    }

    @Test
    fun momentOfForceTest() = testSession {
        loadSysMD(
            """
            Value l: ScalarValues::Real [m] = 1.0 [m].
            Value F: ScalarValues::Real [N] = 1.0 [N].
            Value B: ScalarValues::Real [Nm] = l*F."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("B")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2 kg / s^2", global.resolveVar("B")!!.vectorQuantity.unit.toString())
        assertEquals("moment of force", global.resolveVar("B")!!.vectorQuantity.getDimension())
        assertEquals("force", global.resolveVar("F")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("l")!!.vectorQuantity.getDimension())
    }

    @Test
    fun momentOfInertiaTest() = testSession {
        loadSysMD(
            """
            Value m: ScalarValues::Real [kg] = 1.0 [kg].
            Value A: ScalarValues::Real [m^2] = 1.0 [m^2].
            Value I: ScalarValues::Real [kg m^2] = m*A."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("I")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m^2", global.resolveVar("I")!!.vectorQuantity.unit.toString())
        assertEquals("moment of inertia", global.resolveVar("I")!!.vectorQuantity.getDimension())
        assertEquals("area", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
    }

    @Test
    fun momentumTest() = testSession {
        loadSysMD(
            """
            Value m: ScalarValues::Real [kg] = 1.0 [kg].
            Value v: ScalarValues::Real [m/s] = 1.0 [m/s].
            Value p: ScalarValues::Real [N s] = m*v."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("p")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg m / s", global.resolveVar("p")!!.vectorQuantity.unit.toString())
        assertEquals("momentum", global.resolveVar("p")!!.vectorQuantity.getDimension())
        assertEquals("speed", global.resolveVar("v")!!.vectorQuantity.getDimension())
        assertEquals("mass", global.resolveVar("m")!!.vectorQuantity.getDimension())
    }

    //Ambiguity time, period
    @Test
    fun permittivityTest() = testSession {
        loadSysMD(
            """
            Value I: ScalarValues::Real [F] = 1.0 [F].
            Value l: ScalarValues::Real [m] = 1.0 [m].
            Value epsilon: ScalarValues::Real [A s / V m] = I/l."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("epsilon")!!.aadd().getRange().min, 0.0001)
        assertEquals("s^4 A^2 / m^3 kg", global.resolveVar("epsilon")!!.vectorQuantity.unit.toString())
        assertEquals("permittivity", global.resolveVar("epsilon")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("l")!!.vectorQuantity.getDimension())
        assertEquals("capacitance", global.resolveVar("I")!!.vectorQuantity.getDimension())
    }

    @Test
    fun powerTest() = testSession {
        loadSysMD("""
            Value E: ScalarValues::Real [J] = 1.0 [J].
            Value t: ScalarValues::Real [s] = 1.0 [s]. 
                Value P: ScalarValues::Real [W] = E/t. 
            """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("P")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^2 kg / s^3", global.resolveVar("P")!!.vectorQuantity.unit.toString())
        assertEquals("power", global.resolveVar("P")!!.vectorQuantity.getDimension())
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("energy", global.resolveVar("E")!!.vectorQuantity.getDimension())
    }

    @Test
    fun powerDensityTest() = testSession {
        loadSysMD("""
            Value P: ScalarValues::Real [W] = 1.0 [W].
            Value V: ScalarValues::Real [m^3] = 1.0 [m^3].
            Value PD: ScalarValues::Real [W/m^3] = P/V."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("PD")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / m s^3", global.resolveVar("PD")!!.vectorQuantity.unit.toString())
        assertEquals("power density", global.resolveVar("PD")!!.vectorQuantity.getDimension())
        assertEquals("volume", global.resolveVar("V")!!.vectorQuantity.getDimension())
        assertEquals("power", global.resolveVar("P")!!.vectorQuantity.getDimension())
    }

    @Test
    fun pressureTest() = testSession {
        loadSysMD("""
            Value F: ScalarValues::Real [N] = 1.0 [N].
            Value A: ScalarValues::Real [m^2] = 1.0 [m^2].
            Value p: ScalarValues::Real [Pa] = F/A.""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("p")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / m s^2", global.resolveVar("p")!!.vectorQuantity.unit.toString())
        assertEquals("pressure", global.resolveVar("p")!!.vectorQuantity.getDimension())
        assertEquals("area", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("force", global.resolveVar("F")!!.vectorQuantity.getDimension())
    }

    @Test
    fun pressureTest2() = testSession {
        loadSysMD("""
            Value F: ScalarValues::Real [N] = 1.0 [N].
            Value A: ScalarValues::Real [m^2] = 1.0 [m^2].
            Value p: ScalarValues::Real [mbar] = F/A.
            Value p2: ScalarValues::Real [Pa] = F/A.""")

        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("p2")!!.aadd().getRange().min, 0.0001)
        assertEquals(0.01, global.resolveVar("p")!!.aadd().getRange().min, 0.0001)
        assertEquals("kg / m s^2", global.resolveVar("p")!!.vectorQuantity.unit.toString())
        assertEquals("pressure", global.resolveVar("p")!!.vectorQuantity.getDimension())
        assertEquals("area", global.resolveVar("A")!!.vectorQuantity.getDimension())
        assertEquals("force", global.resolveVar("F")!!.vectorQuantity.getDimension())
    }

    @Test
    fun quantityOfDimensionOne() = testSession {
        loadSysMD(
            """
            Value E: ScalarValues::Real = 100.0.
            Value p: ScalarValues::Real [%] = E.
            Value E2: ScalarValues::Real = p.
            Value f: ScalarValues::Real [dB] = E."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(10000.0, global.resolveVar("p")!!.aadd().getRange().min, 0.0001)
        assertEquals(20.0, global.resolveVar("f")!!.aadd().getRange().min, 0.0001)
        assertEquals(100.0, global.resolveVar("E2")!!.aadd().getRange().min, 0.0001)
        assertEquals("1", global.resolveVar("p")!!.vectorQuantity.unit.toString())
        assertEquals("quantity of dimension one", global.resolveVar("p")!!.vectorQuantity.getDimension())
        assertEquals("quantity of dimension one", global.resolveVar("E")!!.vectorQuantity.getDimension())
        assertEquals("quantity of dimension one", global.resolveVar("E2")!!.vectorQuantity.getDimension())
        assertEquals("quantity of dimension one", global.resolveVar("f")!!.vectorQuantity.getDimension())
    }

    @Test
    fun speedTest() = testSession {
        loadSysMD("""
            Value l: ScalarValues::Real [m]= 10.0 [m].
            Value t: ScalarValues::Real [s] = 1.0 [s].
            Value v1: ScalarValues::Real [m/s] = l/t.
            Value v2: ScalarValues::Real [km/h] = l/t."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(10.0, global.resolveVar("v1")!!.aadd().getRange().min, 0.0001)
        assertEquals(36.0, global.resolveVar("v2")!!.aadd().getRange().min, 0.0001)
        assertEquals("m / s", global.resolveVar("v1")!!.vectorQuantity.unit.toString())
        assertEquals("speed", global.resolveVar("v1")!!.vectorQuantity.getDimension())
        assertEquals("speed", global.resolveVar("v2")!!.vectorQuantity.getDimension())
        assertEquals("time", global.resolveVar("t")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("l")!!.vectorQuantity.getDimension())
    }

    @Test
    fun volumeTest() = testSession {
        loadSysMD("""
            Value l1: ScalarValues::Real [m]= 1.0 [m].
            Value l2: ScalarValues::Real [cm]= 100.0 [cm].
            Value l3: ScalarValues::Real [dm]= 10.0 [dm].
            Value V1: ScalarValues::Real [m^3] = l1*l2*l3.
            Value V2: ScalarValues::Real [l] = l1*l2*l3."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("V1")!!.aadd().getRange().min, 0.0001)
        assertEquals(1000.0, global.resolveVar("V2")!!.aadd().getRange().min, 0.0001)
        assertEquals("m^3", global.resolveVar("V1")!!.vectorQuantity.unit.toString())
        assertEquals("volume", global.resolveVar("V1")!!.vectorQuantity.getDimension())
        assertEquals("volume", global.resolveVar("V2")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("l1")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("l2")!!.vectorQuantity.getDimension())
        assertEquals("length", global.resolveVar("l3")!!.vectorQuantity.getDimension())
    }

    @Test
    fun multipleOperationsTest() = testSession {
        loadSysMD("""
            Value percentage: ScalarValues::Real[%] = 10.0 [%];
            Value number: ScalarValues::Real = 1.0;
            Value result: ScalarValues::Real = percentage + number;
            Value ratio: ScalarValues::Real[dB] = 20.0 [dB];
            Value result2: ScalarValues::Real[1] = ln(ratio)/ln(10.0);
            Value result3: ScalarValues::Real = power2(result2).
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(0.1, global.resolveVar("percentage")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(1.1, global.resolveVar("result")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(100.0, global.resolveVar("ratio")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(2.0, global.resolveVar("result2")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(4.0, global.resolveVar("result3")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun stringToStringTest() = testSession {
        loadSysMD("""
            attribute name: ScalarValues::String = "Hallo".
            """)
        propagate()
        assertEquals("Hallo", global.resolveVar("name")!!.vectorQuantity.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun unitConversationTest() = testSession {
        loadSysMD("""
            Value t: ScalarValues::Real( - 9.81) [m/s^2].
            Value s: ScalarValues::Real = t."""
        )
        propagate()
        assertEquals("-9.81 m/s^2", global.resolveVar("s")!!.vectorQuantity.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }
}