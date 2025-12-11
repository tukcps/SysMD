package quantitytests

import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.Representer
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.DDBuilder
import util.assertIssue
import util.assertNoIssues
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
    fun simpleRanges() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(4.0 .. 9.0);
            feature b: Quantities::ScalarQuantityValue(0.0 .. 1000.0);""")
        solver.propagate()
        assertEquals("4..9", global.resolveVar("a")!!.vectorQuantity.toString())
        assertEquals("0..1000", global.resolveVar("b")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun infiniteTest() {
        val value = DDBuilder().Reals
        val quantity = Quantity(value, "")
        assertEquals("*..*", quantity.toString())
    }

    @Test
    fun singleValues3() = testSession("ISQ") {
        loadKerML("""
                    feature a: Quantities::ScalarQuantityValue(0.0000001).
                    feature b: Quantities::ScalarQuantityValue(0.000001).
                    feature c: Quantities::ScalarQuantityValue(0.00001);
                    feature d: Quantities::ScalarQuantityValue(0.0001);
                    feature e: Quantities::ScalarQuantityValue(0.001);
                    feature f: Quantities::ScalarQuantityValue(0.01);
                    feature g: Quantities::ScalarQuantityValue(0.1);
                    feature h: Quantities::ScalarQuantityValue(1.0);
                    feature i: Quantities::ScalarQuantityValue(10.0);
                    feature j: Quantities::ScalarQuantityValue(100.0);
                    feature k: Quantities::ScalarQuantityValue(1000.0);
                    feature l: Quantities::ScalarQuantityValue(10000.0);
                    feature m: Quantities::ScalarQuantityValue(100000.0);
                    feature n: Quantities::ScalarQuantityValue(1000000.0);
                    feature o: Quantities::ScalarQuantityValue(10000000.0);
                    feature p: Quantities::ScalarQuantityValue(100000000.0);
                    feature q: Quantities::ScalarQuantityValue(1000000000.0);
                    feature r: Quantities::ScalarQuantityValue(10000000000.0);
                    feature s: Quantities::ScalarQuantityValue(0.0); """
        )
        solver.propagate()
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
        assertNoIssues()
    }

    @Test
    fun singleValues4() = testSession("ISQ") {
        loadKerML(
            """feature a: Quantities::ScalarQuantityValue(0.0000005).
                    feature b: Quantities::ScalarQuantityValue(0.000005).
                    feature c: Quantities::ScalarQuantityValue(0.00005).
                    feature d: Quantities::ScalarQuantityValue(0.0005).
                    feature e: Quantities::ScalarQuantityValue(0.005).
                    feature f: Quantities::ScalarQuantityValue(0.05).
                    feature g: Quantities::ScalarQuantityValue(0.5).
                    feature h: Quantities::ScalarQuantityValue(5.0).
                    feature i: Quantities::ScalarQuantityValue(50.0).
                    feature j: Quantities::ScalarQuantityValue(500.0).
                    feature k: Quantities::ScalarQuantityValue(5000.0).
                    feature l: Quantities::ScalarQuantityValue(50000.0).
                    feature m: Quantities::ScalarQuantityValue(500000.0).
                    feature n: Quantities::ScalarQuantityValue(5000000.0).
                    feature o: Quantities::ScalarQuantityValue(50000000.0).
                    feature p: Quantities::ScalarQuantityValue(500000000.0).
                    feature q: Quantities::ScalarQuantityValue(5000000000.0).
                    feature r: Quantities::ScalarQuantityValue(50000000000.0)."""
        )
        solver.propagate()
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
        assertNoIssues()
    }


    @Test
    fun quantityToString1() = testSession("ISQ") {
        loadKerML(
            """feature a: ISQ::LengthValue(4.0 .. 1000.0) [m];
                    feature b: ISQ::MassValue(1.0 .. 1000.0) [kg];
                    feature c: ISQ::DurationValue(10.0 .. 10.0) [s];
                    feature result: ISQ::ForceValue = a*b/(c*c);"""
        )
        initialize()
        solver.propagate()
        assertNoIssues()
        assertEquals("0.04..10000 N", global.resolveVar("result")!!.vectorQuantity.toString())
    }

    @Test
    fun quantityToString2() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(10.0 .. 1000.0) [A^2];
            feature b: Quantities::ScalarQuantityValue(10.0 .. 1000.0) [s^4];
            feature c: ISQ::AreaValue(10.0 .. 10.0) [m^2];
            feature d: ISQ::MassValue(10.0 .. 10.0) [kg];
            feature result: ISQ::CapacitanceValue = a*b/(c*d);
        """)
        solver.propagate()
        assertEquals("1..10000 A s / V", global.resolveVar("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }


    @Test
    fun quantityToString4() = testSession("ISQ") {
        loadKerML(
            """feature a: Quantities::ScalarQuantityValue(1.0 .. 1.0) [m^2].
                    feature b: ISQ::MassValue(1.0 .. 1.0) [kg].
                    feature c: Quantities::ScalarQuantityValue(100.0 .. 100.0) [s^3].
                    feature d: Quantities::ScalarQuantityValue(10.0 .. 10.0) [A^2].
                    feature result: ISQ::ResistanceValue = a*b/(c*d)."""
        )
        initialize()
        solver.propagate()
        assertEquals("1e-3 Ω", global.resolveVar("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString5() = testSession("ISQ") {
        loadKerML(
            """feature a: Quantities::ScalarQuantityValue(1000.0 .. 1000.0) [s^3].
                    feature b: Quantities::ScalarQuantityValue(1000.0 .. 1000.0) [A^2].
                    feature c: Quantities::ScalarQuantityValue(1.0 .. 1.0) [m^2].
                    feature d: Quantities::ScalarQuantityValue(1.0 .. 1.0) [kg^1].
                    feature result: ISQ::ConductanceValue = a*b/(c*d)."""
        )
        initialize()
        solver.propagate()
        assertEquals("1000000 S", global.resolveVar("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString6() = testSession("ISQ") {
        loadKerML(
            """feature a: Quantities::ScalarQuantityValue(1000000.0 .. 1000000.0) [s];
                    feature b: Quantities::ScalarQuantityValue(1000.0 .. 1000.0) [A].
                    feature result: ISQ::ElectricChargeValue = a*b."""
        )
        initialize()
        solver.propagate()
        assertNoIssues()
        assertEquals("1e9 A s", global.resolveVar("result")!!.vectorQuantity.toString())
    }


    @Test
    fun quantityToString7() = testSession("ISQ") {
        loadKerML(
            """feature a: Quantities::ScalarQuantityValue(2000000.0 .. 3000000.0) [m^2].
                    feature b: ISQ::MassValue(2000000.0 .. 3000000.0) [kg].
                    feature c: Quantities::ScalarQuantityValue(1.0 .. 1.0) [s^3].
                    feature d: Quantities::ScalarQuantityValue(1.0 .. 1.0) [A^1].
                    feature result: ISQ::VoltageValue = a*b/(c*d)."""
        )
        initialize()
        solver.propagate()
        assertEquals("4e12..9e12 V", global.resolveVar("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString8() = testSession("ISQ") {
        loadKerML(
            """feature a: Quantities::ScalarQuantityValue(2.0 .. 3.0) [m^2].
                    feature b: Quantities::ScalarQuantityValue(1.0 .. 2.0) [kg].
                    feature c: Quantities::ScalarQuantityValue(1000.0 .. 1000.0) [s^2].
                    feature d: Quantities::ScalarQuantityValue(1.0 .. 1.0) [A^2].
                    feature result: ISQ::InductanceValue = a*b/(c*d)."""
        )
        initialize()
        solver.propagate()
        assertEquals("0.002..0.006 H", global.resolveVar("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString9() = testSession("ISQ") {
        loadKerML(
            """feature a: Quantities::ScalarQuantityValue(200.0 .. 300.0) [m^2].
                    feature b: Quantities::ScalarQuantityValue(10.0 .. 20.0) [kg].
                    feature c: Quantities::ScalarQuantityValue(1.0 .. 1.0) [s^2].
                    feature d: Quantities::ScalarQuantityValue(1.0 .. 1.0) [A].
                    feature result: ISQ::MagneticFluxValue = a*b/(c*d)."""
        )
        initialize()
        solver.propagate()
        assertEquals("2000..6000 Wb", global.resolveVar("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }


    @Test
    fun quantityToString10() = testSession("ISQ") {
        loadKerML(
            """feature b: ISQ::MassValue(1.0 .. 2.0) [kg].
                    feature c: Quantities::ScalarQuantityValue(10000.0 .. 10000.0) [s^2].
                    feature d: ISQ::ElectricCurrentValue(100000.0 .. 100000.0) [A].
                    feature result: ISQ::MagneticFluxDensityValue = b/(c*d)."""
        )
        initialize()
        solver.propagate()
        assertEquals("1e-9 T", global.resolveVar("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString11() = testSession("ISQ") {
        loadKerML(
            """feature a: Quantities::ScalarQuantityValue(20.0 .. 20.0) [m^2].
                    feature b: ISQ::MassValue(1.0 .. 1.0) [kg].
                    feature c: Quantities::ScalarQuantityValue(1000000.0 .. 1000000.0) [s^3].
                    feature result: ISQ::PowerValue = a*b/c."""
        )
        solver.propagate()
        assertEquals("20e-6 W", global.resolveVar("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString12() = testSession("ISQ") {
        loadKerML(
            """feature a: ISQ::LengthValue(2.0 .. 2.0) [km];
                    feature b: ISQ::DurationValue(100.0 .. 100.0) [s];
                    feature result: ISQ::SpeedValue = a/b."""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals("20 m/s", global.resolveVar("result")!!.vectorQuantity.toString())
    }

    @Test
    fun quantityToString13() = testSession("ISQ") {
        loadKerML(
            """feature a: ISQ::LengthValue(2.0 .. 2.0) [km];
                    feature b: Quantities::ScalarQuantityValue(100.0 .. 100.0) [s^2].
                    feature result: ISQ::AccelerationValue = a/b."""
        )
        solver.propagate()
        assertEquals("20 m/s^2", global.resolveVar("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString14() = testSession("ISQ") {
        loadKerML(
            """feature a: ISQ::LengthValue(200.0 .. 300.0) [m];
                    feature b: ISQ::LengthValue(100.0 .. 100.0) [m];
                    feature result: ISQ::AreaValue = a*b;"""
        )
        solver.propagate()
        assertEquals("20000..30000 m^2", global.resolveVar("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString15() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(0.11 .. 0.11) [m];
            feature result: ISQ::LengthValue = a;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals("0.11 m", global.resolveVar("result")!!.vectorQuantity.toString())
    }

    @Test
    fun quantityToString16() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(0.05 .. 0.05) [m];
            feature result: ISQ::LengthValue = a;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals("0.05 m", global.resolveVar("result")!!.vectorQuantity.toString())
    }

    @Test
    fun quantityToString17() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(0.05 .. 0.05) [m^3].
            feature result: ISQ::VolumeValue = a.""")
        solver.propagate()
        assertEquals("0.05 m^3", global.resolveVar("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString18() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::SpeedValue [km/h] = [0.0 .. 130.0] [km/h].
            feature b: ISQ::DurationValue= 1.0 [s];
            feature result: ISQ::LengthValue= a*b.
            """)
        solver.propagate()
        assertEquals("0..130 km / h", global.resolveVar("a")!!.vectorQuantity.toString())
        assertEquals("1 s", global.resolveVar("b")!!.vectorQuantity.toString())
        assertEquals("0..36.11111 m", global.resolveVar("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test //not satisfiable
    fun quantityToString19() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::SpeedValue [km/h] = [0.0 .. 10.0] [m/s];
            feature b: ISQ::DurationValue= 1.0 [s];
            feature result: ISQ::LengthValue(20..30) [m] = a*b;
        """)
        solver.propagate()
        // Following may or may not be correct depending on order of constraint propagation
        // assertEquals("∅", global.resolveVar("a")!!.vectorQuantity.toString())
        // assertEquals("∅", global.resolveVar("b")!!.vectorQuantity.toString())
        assertEquals("∅", global.resolveVar("result")!!.vectorQuantity.toString())
        assertIssue("is not satisfiable")
    }

    @Test
    fun returnInputType1() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Real (0 .. 100);
        """)
        solver.propagate()
        val representer = Representer()
        assertEquals(Representer.InputType.NormalNumbers, representer.returnInputType(global.resolveVar("a")!!.vectorQuantity.values[0].asAadd()))
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun returnInputType2() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Real = 33.0;
        """)
        solver.propagate()
        val representer = Representer()
        assertEquals(Representer.InputType.ClosedRange, representer.returnInputType(global.resolveVar("a")!!.vectorQuantity.values[0].asAadd()))
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun returnInputType3() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Real (0 .. 0);
        """)
        solver.propagate()
        val representer = Representer()
        assertEquals(Representer.InputType.ClosedRange, representer.returnInputType(global.resolveVar("a")!!.vectorQuantity.values[0].asAadd()))
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}
