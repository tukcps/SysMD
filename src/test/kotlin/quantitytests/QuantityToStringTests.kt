package quantitytests

import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.Representer
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.DDBuilder
import util.assertIssue
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Testcases for the 'representer' and the roString function for quantities
 */
class QuantityToStringTests {
    @Test
    fun simpleRanges() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(4.0 .. 9.0);
            feature b: Quantities::ScalarQuantityValue(0.0 .. 1000.0);
        """, Runlevel.ALL)
        assertEquals("4..9", solver.getVariable("a")!!.vectorQuantity.toString())
        assertEquals("0..1000", solver.getVariable("b")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun infiniteTest() {
        val value = DDBuilder().Reals
        val quantity = Quantity(value, "")
        assertEquals("*..*", quantity.toString())
    }

    @Test
    fun undefinedQuantityRepresentationTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer;
            feature b: ScalarValues::Real;
            feature c: ScalarValues::Integer[2] = (1..2147483647, 4..2147483647);
            feature d: ScalarValues::Integer[2];
        """)
        solver.propagate()
        val a = solver.getVariable("a")!!
        val b = solver.getVariable("b")!!
        val c = solver.getVariable("c")!!
        val d = solver.getVariable("d")!!
        assertEquals("*..*", a.vectorQuantity.toString())
        assertEquals("*..*", b.vectorQuantity.toString())
        assertEquals("*..*", a.valueStr)
        assertEquals("*..*", b.valueStr)
        assertEquals("[1..*, 4..*]", c.valueStr)
        assertEquals("*..*", d.valueStr)
    }

    @Test
    fun singleValues3() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(0.0000001); 
            feature b: Quantities::ScalarQuantityValue(0.000001);
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
            feature s: Quantities::ScalarQuantityValue(0.0); 
        """, Runlevel.ALL)
        assertEquals("100e-9", solver.getVariable("a")!!.vectorQuantity.toString())
        assertEquals("1e-6", solver.getVariable("b")!!.vectorQuantity.toString())
        assertEquals("10e-6", solver.getVariable("c")!!.vectorQuantity.toString())
        assertEquals("100e-6", solver.getVariable("d")!!.vectorQuantity.toString())
        assertEquals("0.001", solver.getVariable("e")!!.vectorQuantity.toString())
        assertEquals("0.01", solver.getVariable("f")!!.vectorQuantity.toString())
        assertEquals("0.1", solver.getVariable("g")!!.vectorQuantity.toString())
        assertEquals("1", solver.getVariable("h")!!.vectorQuantity.toString())
        assertEquals("10", solver.getVariable("i")!!.vectorQuantity.toString())
        assertEquals("100", solver.getVariable("j")!!.vectorQuantity.toString())
        assertEquals("1000", solver.getVariable("k")!!.vectorQuantity.toString())
        assertEquals("10000", solver.getVariable("l")!!.vectorQuantity.toString())
        assertEquals("100000", solver.getVariable("m")!!.vectorQuantity.toString())
        assertEquals("1e6", solver.getVariable("n")!!.vectorQuantity.toString())
        assertEquals("10e6", solver.getVariable("o")!!.vectorQuantity.toString())
        assertEquals("100e6", solver.getVariable("p")!!.vectorQuantity.toString())
        assertEquals("1e9", solver.getVariable("q")!!.vectorQuantity.toString())
        assertEquals("10e9", solver.getVariable("r")!!.vectorQuantity.toString())
        assertEquals("0", solver.getVariable("s")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun singleValues4() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(0.0000005);
            feature b: Quantities::ScalarQuantityValue(0.000005);
            feature c: Quantities::ScalarQuantityValue(0.00005);
            feature d: Quantities::ScalarQuantityValue(0.0005);
            feature e: Quantities::ScalarQuantityValue(0.005);
            feature f: Quantities::ScalarQuantityValue(0.05);
            feature g: Quantities::ScalarQuantityValue(0.5);
            feature h: Quantities::ScalarQuantityValue(5.0);
            feature i: Quantities::ScalarQuantityValue(50.0);
            feature j: Quantities::ScalarQuantityValue(500.0);
            feature k: Quantities::ScalarQuantityValue(5000.0);
            feature l: Quantities::ScalarQuantityValue(50000.0);
            feature m: Quantities::ScalarQuantityValue(500000.0);
            feature n: Quantities::ScalarQuantityValue(5000000.0);
            feature o: Quantities::ScalarQuantityValue(50000000.0);
            feature p: Quantities::ScalarQuantityValue(500000000.0);
            feature q: Quantities::ScalarQuantityValue(5000000000.0);
            feature r: Quantities::ScalarQuantityValue(50000000000.0);
        """, Runlevel.ALL)
        assertEquals("500e-9", solver.getVariable("a")!!.vectorQuantity.toString())
        assertEquals("5e-6", solver.getVariable("b")!!.vectorQuantity.toString())
        assertEquals("50e-6", solver.getVariable("c")!!.vectorQuantity.toString())
        assertEquals("500e-6", solver.getVariable("d")!!.vectorQuantity.toString())
        assertEquals("0.005", solver.getVariable("e")!!.vectorQuantity.toString())
        assertEquals("0.05", solver.getVariable("f")!!.vectorQuantity.toString())
        assertEquals("0.5", solver.getVariable("g")!!.vectorQuantity.toString())
        assertEquals("5", solver.getVariable("h")!!.vectorQuantity.toString())
        assertEquals("50", solver.getVariable("i")!!.vectorQuantity.toString())
        assertEquals("500", solver.getVariable("j")!!.vectorQuantity.toString())
        assertEquals("5000", solver.getVariable("k")!!.vectorQuantity.toString())
        assertEquals("50000", solver.getVariable("l")!!.vectorQuantity.toString())
        assertEquals("500000", solver.getVariable("m")!!.vectorQuantity.toString())
        assertEquals("5e6", solver.getVariable("n")!!.vectorQuantity.toString())
        assertEquals("50e6", solver.getVariable("o")!!.vectorQuantity.toString())
        assertEquals("500e6", solver.getVariable("p")!!.vectorQuantity.toString())
        assertEquals("5e9", solver.getVariable("q")!!.vectorQuantity.toString())
        assertEquals("50e9", solver.getVariable("r")!!.vectorQuantity.toString())
        assertNoIssues()
    }


    @Test
    fun quantityToString1() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(4.0 .. 1000.0 [m]);
            feature b: ISQ::MassValue(1.0 .. 1000.0 [kg]);
            feature c: ISQ::DurationValue(10.0 .. 10.0 [s]);
            feature result: ISQ::ForceValue = a*b/(c*c);
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("0.04..10000 N", solver.getVariable("result")!!.vectorQuantity.toString())
    }

    @Test
    fun quantityToString2() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(10.0 .. 1000.0 [A^2]);
            feature b: Quantities::ScalarQuantityValue(10.0 .. 1000.0 [s^4]);
            feature c: ISQ::AreaValue(10.0 .. 10.0 [m^2]);
            feature d: ISQ::MassValue(10.0 .. 10.0 [kg]);
            feature result: ISQ::CapacitanceValue = a*b/(c*d);
        """, Runlevel.ALL)
        assertEquals("1..10000 A s / V", solver.getVariable("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }


    @Test
    fun quantityToString4() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(1.0 .. 1.0 [m^2]);
            feature b: ISQ::MassValue(1.0 .. 1.0 [kg]);
            feature c: Quantities::ScalarQuantityValue(100.0 .. 100.0 [s^3]);
            feature d: Quantities::ScalarQuantityValue(10.0 .. 10.0 [A^2]);
            feature result: ISQ::ResistanceValue = a*b/(c*d); 
        """, Runlevel.ALL)
        assertEquals("1e-3 Ω", solver.getVariable("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString5() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(1000.0 .. 1000.0 [s^3]);
            feature b: Quantities::ScalarQuantityValue(1000.0 .. 1000.0 [A^2]);
            feature c: Quantities::ScalarQuantityValue(1.0 .. 1.0 [m^2]);
            feature d: Quantities::ScalarQuantityValue(1.0 .. 1.0 [kg^1]);
            feature result: ISQ::ConductanceValue = a*b/(c*d); 
        """, Runlevel.ALL)
        solver.propagate()
        assertEquals("1000000 S", solver.getVariable("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString6() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(1000000.0 .. 1000000.0 [s]);
            feature b: Quantities::ScalarQuantityValue(1000.0 .. 1000.0 [A]);
            feature result: ISQ::ElectricChargeValue = a*b; 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("1e9 A s", solver.getVariable("result")!!.vectorQuantity.toString())
    }


    @Test
    fun quantityToString7() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(2000000.0 .. 3000000.0 [m^2]);
            feature b: ISQ::MassValue(2000000.0 .. 3000000.0 [kg]);
            feature c: Quantities::ScalarQuantityValue(1.0 .. 1.0 [s^3]);
            feature d: Quantities::ScalarQuantityValue(1.0 .. 1.0 [A^1]);
            feature result: ISQ::ElectricPotentialDifferenceValue = a*b/(c*d); 
        """, Runlevel.ALL)
        assertEquals("4e12..9e12 V", solver.getVariable("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString8() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(2.0 .. 3.0 [m^2]);
            feature b: Quantities::ScalarQuantityValue(1.0 .. 2.0 [kg]);
            feature c: Quantities::ScalarQuantityValue(1000.0 .. 1000.0 [s^2]);
            feature d: Quantities::ScalarQuantityValue(1.0 .. 1.0 [A^2]);
            feature result: ISQ::InductanceValue = a*b/(c*d); 
        """, Runlevel.ALL)
        assertEquals("0.002..0.006 H", solver.getVariable("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString9() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(200.0 .. 300.0 [m^2]);
            feature b: Quantities::ScalarQuantityValue(10.0 .. 20.0 [kg]);
            feature c: Quantities::ScalarQuantityValue(1.0 .. 1.0 [s^2]); 
            feature d: Quantities::ScalarQuantityValue(1.0 .. 1.0 [A]);
            feature result: ISQ::MagneticFluxValue = a*b/(c*d);"""
        )
        solver.propagate()
        assertEquals("2000..6000 Wb", solver.getVariable("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }


    @Test
    fun quantityToString10() = testSession("ISQ") {
        loadKerML("""
            feature b: ISQ::MassValue(1.0 .. 2.0 [kg]);
            feature c: Quantities::ScalarQuantityValue(10000.0 .. 10000.0 [s^2]); 
            feature d: ISQ::ElectricCurrentValue(100000.0 .. 100000.0 [A]);
            feature result: ISQ::MagneticFluxDensityValue = b/(c*d);
        """, Runlevel.ALL)
        assertEquals("1e-9 T", solver.getVariable("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString11() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(20.0 .. 20.0 [m^2]); 
            feature b: ISQ::MassValue(1.0 .. 1.0 [kg]); 
            feature c: Quantities::ScalarQuantityValue(1000000.0 .. 1000000.0 [s^3]); 
            feature result: ISQ::PowerValue = a*b/c;
        """, Runlevel.ALL)
        assertEquals("20e-6 W", solver.getVariable("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString12() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(2.0 .. 2.0 [km]);
            feature b: ISQ::DurationValue(100.0 .. 100.0 [s]);
            feature result: ISQ::SpeedValue = a/b;
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("20 m/s", solver.getVariable("result")!!.vectorQuantity.toString())
    }

    @Test
    fun quantityToString13() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(2.0 .. 2.0 [km]);
            feature b: Quantities::ScalarQuantityValue(100.0 .. 100.0 [s^2])
            feature result: ISQ::AccelerationValue = a/b; 
        """, Runlevel.ALL)
        solver.propagate()
        assertEquals("20 m/s^2", solver.getVariable("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString14() = testSession("ISQ") {
        loadKerML(
            """feature a: ISQ::LengthValue(200.0 .. 300.0 [m]);
                    feature b: ISQ::LengthValue(100.0 .. 100.0 [m]);
                    feature result: ISQ::AreaValue = a*b;"""
        )
        solver.propagate()
        assertEquals("20000..30000 m^2", solver.getVariable("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString15() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(0.11 .. 0.11 [m]);
            feature result: ISQ::LengthValue = a;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals("0.11 m", solver.getVariable("result")!!.vectorQuantity.toString())
    }

    @Test
    fun quantityToString16() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(0.05 .. 0.05 [m]);
            feature result: ISQ::LengthValue = a;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals("0.05 m", solver.getVariable("result")!!.vectorQuantity.toString())
    }

    @Test
    fun quantityToString17() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(0.05 .. 0.05 [m^3]);
            feature result: ISQ::VolumeValue = a; """)
        solver.propagate()
        assertEquals("0.05 m^3", solver.getVariable("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun quantityToString18() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::SpeedValue(* [km/h]) = [0.0 .. 130.0] [km/h].
            feature b: ISQ::DurationValue= 1.0 [s];
            feature result: ISQ::LengthValue= a*b.
        """, Runlevel.ALL)
        assertEquals("0..130 km / h", solver.getVariable("a")!!.vectorQuantity.toString())
        assertEquals("1 s", solver.getVariable("b")!!.vectorQuantity.toString())
        assertEquals("0..36.11111 m", solver.getVariable("result")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test //not satisfiable
    fun quantityToString19() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::SpeedValue (* [km/h]) = [0.0 .. 10.0] [m/s];
            feature b: ISQ::DurationValue         = 1.0 [s];
            feature result: ISQ::LengthValue(20..30 [m]) = a*b;
        """)
        solver.propagate()
        // Following may or may not be correct depending on order of constraint propagation
        // assertEquals("∅", solver.getVariable("a")!!.vectorQuantity.toString())
        // assertEquals("∅", solver.getVariable("b")!!.vectorQuantity.toString())
        assertEquals("∅", solver.getVariable("result")!!.vectorQuantity.toString())
        assertIssue("is not satisfiable")
    }

    @Test
    fun returnInputType1() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Real (0 .. 100);
        """)
        solver.propagate()
        val representer = Representer()
        assertEquals(Representer.InputType.NormalNumbers, representer.returnInputType(solver.getVariable("a")!!.vectorQuantity.values[0].asAadd()))
        assertNoIssues()
    }

    @Test
    fun returnInputType2() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Real = 33.0;
        """)
        solver.propagate()
        val representer = Representer()
        assertEquals(Representer.InputType.ClosedRange, representer.returnInputType(solver.getVariable("a")!!.vectorQuantity.values[0].asAadd()))
        assertNoIssues()
    }

    @Test
    fun returnInputType3() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Real (0 .. 0);
        """)
        solver.propagate()
        val representer = Representer()
        assertEquals(Representer.InputType.ClosedRange, representer.returnInputType(solver.getVariable("a")!!.vectorQuantity.values[0].asAadd()))
        assertNoIssues()
    }
}
