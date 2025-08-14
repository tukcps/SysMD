package quantitytests

import io.github.tukcps.aadd.DDBuilder
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SysMDInfo
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.Representer
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue



/**
 * Testcases for the 'representer' and the roString function for quantities
 */
class QuantityToStringTests {
    @Test
    fun simpleRanges() = testSession("SI") {
        loadKerML("""
            feature a: SI::Quantity(4.0 .. 9.0);
            feature b: SI::Quantity(0.0 .. 1000.0);""")
        propagate()
        assertEquals("4..9", global.resolveVar("a")!!.vectorQuantity.toString())
        assertEquals("0..1000", global.resolveVar("b")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun infiniteTest() {
        val value = DDBuilder().Reals
        val quantity = Quantity(value, "")
        assertEquals("*..*", quantity.toString())
    }

    @Test
    fun singleValues3() = testSession("SI") {
        loadKerML("""
                    feature a: SI::Quantity(0.0000001).
                    feature b: SI::Quantity(0.000001).
                    feature c: SI::Quantity(0.00001).
                    feature d: SI::Quantity(0.0001).
                    feature e: SI::Quantity(0.001).
                    feature f: SI::Quantity(0.01).
                    feature g: SI::Quantity(0.1).
                    feature h: SI::Quantity(1.0).
                    feature i: SI::Quantity(10.0).
                    feature j: SI::Quantity(100.0).
                    feature k: SI::Quantity(1000.0).
                    feature l: SI::Quantity(10000.0).
                    feature m: SI::Quantity(100000.0).
                    feature n: SI::Quantity(1000000.0).
                    feature o: SI::Quantity(10000000.0).
                    feature p: SI::Quantity(100000000.0).
                    feature q: SI::Quantity(1000000000.0).
                    feature r: SI::Quantity(10000000000.0).
                    feature s: SI::Quantity(0.0)."""
        )
        propagate()
        assertEquals("100e-9", global.resolveVar("a")!!.vectorQuantity.toString())
        assertEquals("1e-6", global.resolveVar("b")!!.vectorQuantity.toString())
        assertEquals("10e-6", global.resolveVar("c")!!.vectorQuantity.toString())
        assertEquals("100e-6", global.resolveVar("d")!!.vectorQuantity.toString())
        assertEquals("0.001", global.resolveVar("e")!!.vectorQuantity.toString())
        assertEquals("0.01", global.resolveVar("f")!!.vectorQuantity.toString())
        assertEquals("0.1", global.resolveVar("g")!!.vectorQuantity.toString())
        assertEquals("1", global.resolveVar("h")!!.vectorQuantity.toString())
        assertEquals("10", global.resolveVar("i")!!.vectorQuantity.toString())
        assertEquals("100", global.resolveVar("j")!!.vectorQuantity.toString())
        assertEquals("1000", global.resolveVar("k")!!.vectorQuantity.toString())
        assertEquals("10000", global.resolveVar("l")!!.vectorQuantity.toString())
        assertEquals("100000", global.resolveVar("m")!!.vectorQuantity.toString())
        assertEquals("1e6", global.resolveVar("n")!!.vectorQuantity.toString())
        assertEquals("10e6", global.resolveVar("o")!!.vectorQuantity.toString())
        assertEquals("100e6", global.resolveVar("p")!!.vectorQuantity.toString())
        assertEquals("1e9", global.resolveVar("q")!!.vectorQuantity.toString())
        assertEquals("10e9", global.resolveVar("r")!!.vectorQuantity.toString())
        assertEquals("0", global.resolveVar("s")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun singleValues4() = testSession("SI") {
        loadKerML(
            """feature a: SI::Quantity(0.0000005).
                    feature b: SI::Quantity(0.000005).
                    feature c: SI::Quantity(0.00005).
                    feature d: SI::Quantity(0.0005).
                    feature e: SI::Quantity(0.005).
                    feature f: SI::Quantity(0.05).
                    feature g: SI::Quantity(0.5).
                    feature h: SI::Quantity(5.0).
                    feature i: SI::Quantity(50.0).
                    feature j: SI::Quantity(500.0).
                    feature k: SI::Quantity(5000.0).
                    feature l: SI::Quantity(50000.0).
                    feature m: SI::Quantity(500000.0).
                    feature n: SI::Quantity(5000000.0).
                    feature o: SI::Quantity(50000000.0).
                    feature p: SI::Quantity(500000000.0).
                    feature q: SI::Quantity(5000000000.0).
                    feature r: SI::Quantity(50000000000.0)."""
        )
        propagate()
        assertEquals("500e-9", global.resolveVar("a")!!.vectorQuantity.toString())
        assertEquals("5e-6", global.resolveVar("b")!!.vectorQuantity.toString())
        assertEquals("50e-6", global.resolveVar("c")!!.vectorQuantity.toString())
        assertEquals("500e-6", global.resolveVar("d")!!.vectorQuantity.toString())
        assertEquals("0.005", global.resolveVar("e")!!.vectorQuantity.toString())
        assertEquals("0.05", global.resolveVar("f")!!.vectorQuantity.toString())
        assertEquals("0.5", global.resolveVar("g")!!.vectorQuantity.toString())
        assertEquals("5", global.resolveVar("h")!!.vectorQuantity.toString())
        assertEquals("50", global.resolveVar("i")!!.vectorQuantity.toString())
        assertEquals("500", global.resolveVar("j")!!.vectorQuantity.toString())
        assertEquals("5000", global.resolveVar("k")!!.vectorQuantity.toString())
        assertEquals("50000", global.resolveVar("l")!!.vectorQuantity.toString())
        assertEquals("500000", global.resolveVar("m")!!.vectorQuantity.toString())
        assertEquals("5e6", global.resolveVar("n")!!.vectorQuantity.toString())
        assertEquals("50e6", global.resolveVar("o")!!.vectorQuantity.toString())
        assertEquals("500e6", global.resolveVar("p")!!.vectorQuantity.toString())
        assertEquals("5e9", global.resolveVar("q")!!.vectorQuantity.toString())
        assertEquals("50e9", global.resolveVar("r")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }


    @Test
    fun quantityToString1() = testSession("SI") {
        loadKerML(
            """feature a: SI::Length(4.0 .. 1000.0) [m];
                    feature b: SI::Mass(1.0 .. 1000.0) [kg];
                    feature c: SI::Time(10.0 .. 10.0) [s];
                    feature result: SI::Force = a*b/(c*c)."""
        )
        initialize()
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals("0.04..10000 N", global.resolveVar("result")!!.vectorQuantity.toString())
    }

    @Test
    fun quantityToString2() = testSession("SI") {
        loadKerML("""
            feature a: SI::Quantity(10.0 .. 1000.0) [A^2];
            feature b: SI::Quantity(10.0 .. 1000.0) [s^4];
            feature c: SI::Area(10.0 .. 10.0) [m^2];
            feature d: SI::Mass(10.0 .. 10.0) [kg];
            feature result: SI::Capacitance = a*b/(c*d);
        """)
        propagate()
        assertEquals("1..10000 F", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun quantityToString3() = testSession("SI") {
        loadKerML(
            """feature a: SI::AmountOfSubstance(4.0 .. 1000.0) [mol].
                    feature b: SI::Time(1.0 .. 1.0) [s];
                    feature result: SI::CatalyticActivity = a/b."""
        )
        initialize()
        propagate()
        assertEquals("4..1000 kat", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }


    @Test
    fun quantityToString4() = testSession("SI") {
        loadKerML(
            """feature a: SI::Quantity(1.0 .. 1.0) [m^2].
                    feature b: SI::Mass(1.0 .. 1.0) [kg].
                    feature c: SI::Quantity(100.0 .. 100.0) [s^3].
                    feature d: SI::Quantity(10.0 .. 10.0) [A^2].
                    feature result: SI::ElectricalResistance = a*b/(c*d)."""
        )
        initialize()
        propagate()
        assertEquals("1 mΩ", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun quantityToString5() = testSession("SI") {
        loadKerML(
            """feature a: SI::Quantity(1000.0 .. 1000.0) [s^3].
                    feature b: SI::Quantity(1000.0 .. 1000.0) [A^2].
                    feature c: SI::Quantity(1.0 .. 1.0) [m^2].
                    feature d: SI::Quantity(1.0 .. 1.0) [kg^1].
                    feature result: SI::ElectricalConductance = a*b/(c*d)."""
        )
        initialize()
        propagate()
        assertEquals("1 MS", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun quantityToString6() = testSession("SI") {
        loadKerML(
            """feature a: SI::Quantity(1000000.0 .. 1000000.0) [s];
                    feature b: SI::Quantity(1000.0 .. 1000.0) [A].
                    feature result: SI::ElectricCharge = a*b."""
        )
        initialize()
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals("1 GC", global.resolveVar("result")!!.vectorQuantity.toString())
    }


    @Test
    fun quantityToString7() = testSession("SI") {
        loadKerML(
            """feature a: SI::Quantity(2000000.0 .. 3000000.0) [m^2].
                    feature b: SI::Mass(2000000.0 .. 3000000.0) [kg].
                    feature c: SI::Quantity(1.0 .. 1.0) [s^3].
                    feature d: SI::Quantity(1.0 .. 1.0) [A^1].
                    feature result: SI::Voltage = a*b/(c*d)."""
        )
        initialize()
        propagate()
        assertEquals("4..9 TV", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun quantityToString8() = testSession("SI") {
        loadKerML(
            """feature a: SI::Quantity(2.0 .. 3.0) [m^2].
                    feature b: SI::Quantity(1.0 .. 2.0) [kg].
                    feature c: SI::Quantity(1000.0 .. 1000.0) [s^2].
                    feature d: SI::Quantity(1.0 .. 1.0) [A^2].
                    feature result: SI::Inductance = a*b/(c*d)."""
        )
        initialize()
        propagate()
        assertEquals("2..6 mH", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun quantityToString9() = testSession("SI") {
        loadKerML(
            """feature a: SI::Quantity(200.0 .. 300.0) [m^2].
                    feature b: SI::Quantity(10.0 .. 20.0) [kg].
                    feature c: SI::Quantity(1.0 .. 1.0) [s^2].
                    feature d: SI::Quantity(1.0 .. 1.0) [A].
                    feature result: SI::MagneticFlux = a*b/(c*d)."""
        )
        initialize()
        propagate()
        assertEquals("2..6 kWb", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }


    @Test
    fun quantityToString10() = testSession("SI") {
        loadKerML(
            """feature b: SI::Mass(1.0 .. 2.0) [kg].
                    feature c: SI::Quantity(10000.0 .. 10000.0) [s^2].
                    feature d: SI::ElectricCurrent(100000.0 .. 100000.0) [A].
                    feature result: SI::MagneticFluxDensity = b/(c*d)."""
        )
        initialize()
        propagate()
        assertEquals("1..2 nT", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun quantityToString11() = testSession("SI") {
        loadKerML(
            """feature a: SI::Quantity(20.0 .. 20.0) [m^2].
                    feature b: SI::Mass(1.0 .. 1.0) [kg].
                    feature c: SI::Quantity(1000000.0 .. 1000000.0) [s^3].
                    feature result: SI::Power = a*b/c."""
        )
        propagate()
        assertEquals("20 μW", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun quantityToString12() = testSession("SI") {
        loadKerML(
            """feature a: SI::Length(2.0 .. 2.0) [km];
                    feature b: SI::Time(100.0 .. 100.0) [s];
                    feature result: SI::Speed = a/b."""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals("20 m/s", global.resolveVar("result")!!.vectorQuantity.toString())
    }

    @Test
    fun quantityToString13() = testSession("SI") {
        loadKerML(
            """feature a: SI::Length(2.0 .. 2.0) [km];
                    feature b: SI::Quantity(100.0 .. 100.0) [s^2].
                    feature result: SI::Acceleration = a/b."""
        )
        propagate()
        assertEquals("20 m/s^2", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun quantityToString14() = testSession("SI") {
        loadKerML(
            """feature a: SI::Length(200.0 .. 300.0) [m];
                    feature b: SI::Length(100.0 .. 100.0) [m];
                    feature result: SI::Area = a*b."""
        )
        propagate()
        assertEquals("2..3 ha", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun quantityToString15() = testSession("SI") {
        loadKerML(
            """feature a: SI::Length(0.11 .. 0.11) [m];
                    feature result: SI::Length = a."""
        )
        propagate()
        assertEquals("11 cm", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun quantityToString16() = testSession("SI") {
        loadKerML("""
            feature a: SI::Length(0.05 .. 0.05) [m];
            feature result: SI::Length = a."""
        )
        propagate()
        assertEquals("5 cm", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun quantityToString17() = testSession("SI") {
        loadKerML("""
            feature a: SI::Quantity(0.05 .. 0.05) [m^3].
            feature result: SI::Volume = a.""")
        propagate()
        assertEquals("50 l", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun quantityToString18() = testSession("SI") {
        loadKerML("""
            feature a: SI::Speed [km/h] = [0.0 .. 130.0] [km/h].
            feature b: SI::Time= 1.0 [s];
            feature result: SI::Length= a*b.
            """)
        propagate()
        assertEquals("0..130 km / h", global.resolveVar("a")!!.vectorQuantity.toString())
        assertEquals("1 s", global.resolveVar("b")!!.vectorQuantity.toString())
        assertEquals("0..36.11111 m", global.resolveVar("result")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test //not satisfiable
    fun quantityToString19() = testSession("SI") {
        loadKerML("""
            feature a: SI::Speed [km/h] = [0.0 .. 10.0] [m/s];
            feature b: SI::Time= 1.0 [s];
            feature result: SI::Length(20..30) [m] = a*b;
            """)
        propagate()
        assertEquals("∅", global.resolveVar("a")!!.vectorQuantity.toString())
        assertEquals("∅", global.resolveVar("b")!!.vectorQuantity.toString())
        assertEquals("∅", global.resolveVar("result")!!.vectorQuantity.toString())
        assertTrue(status.issues.any { it.kind.ordinal >= Issue.Kind.WARN.ordinal }, status.issues.toString())
    }

    @Test
    fun returnInputType1() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Real (0 .. 100);
            """)
        propagate()
        val representer = Representer()
        assertEquals(Representer.InputType.NormalNumbers, representer.returnInputType(global.resolveVar("a")!!.vectorQuantity.values[0].asAadd()))
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun returnInputType2() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Real = 33.0;
            """)
        propagate()
        val representer = Representer()
        assertEquals(Representer.InputType.CloseRange, representer.returnInputType(global.resolveVar("a")!!.vectorQuantity.values[0].asAadd()))
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun returnInputType3() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Real (0 .. 0);
            """)
        propagate()
        val representer = Representer()
        assertEquals(Representer.InputType.CloseRange, representer.returnInputType(global.resolveVar("a")!!.vectorQuantity.values[0].asAadd()))
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}
