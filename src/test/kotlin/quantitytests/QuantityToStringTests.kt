package quantitytests

import com.github.tukcps.sysmd.quantities.Representer
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.values.bounds.DoubleBound
import util.*
import util.mockup.loadKerML
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
        assertNoIssues()
        assertEquals("4..9", solver.variable("a").vectorQuantity.toString())
        assertEquals("0..1000", solver.variable("b").vectorQuantity.toString())
    }

    @Test
    fun infiniteTest() {
        val value = DDBuilder().Reals.All
        val quantity = VectorQuantity(value, "")
        // FIXME: When adopting -*..*, change expected "*..*" to "-*..*" to better show the negative part
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
        assertNoIssues()
        val a = solver.variable("a")
        val b = solver.variable("b")
        val c = solver.variable("c")
        val d = solver.variable("d")
        // FIXME: When adopting -*..*, change expected "*..*" to "-*..*" to better show the negative part
        assertEquals("*..*", a.vectorQuantity.toString())
        assertEquals("*..*", b.vectorQuantity.toString())
        assertEquals("*..*", a.valueStr)
        assertEquals("*..*", b.valueStr)
        //assertEquals("[1..*, 4..*]", c.valueStr)
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
        assertNoIssues()
        assertEquals("100e-9", solver.variable("a").vectorQuantity.toString())
        assertEquals("1e-6", solver.variable("b").vectorQuantity.toString())
        assertEquals("10e-6", solver.variable("c").vectorQuantity.toString())
        assertEquals("100e-6", solver.variable("d").vectorQuantity.toString())
        assertEquals("0.001", solver.variable("e").vectorQuantity.toString())
        assertEquals("0.01", solver.variable("f").vectorQuantity.toString())
        assertEquals("0.1", solver.variable("g").vectorQuantity.toString())
        assertEquals("1", solver.variable("h").vectorQuantity.toString())
        assertEquals("10", solver.variable("i").vectorQuantity.toString())
        assertEquals("100", solver.variable("j").vectorQuantity.toString())
        assertEquals("1000", solver.variable("k").vectorQuantity.toString())
        assertEquals("10000", solver.variable("l").vectorQuantity.toString())
        assertEquals("100000", solver.variable("m").vectorQuantity.toString())
        assertEquals("1e6", solver.variable("n").vectorQuantity.toString())
        assertEquals("10e6", solver.variable("o").vectorQuantity.toString())
        assertEquals("100e6", solver.variable("p").vectorQuantity.toString())
        assertEquals("1e9", solver.variable("q").vectorQuantity.toString())
        assertEquals("10e9", solver.variable("r").vectorQuantity.toString())
        assertEquals("0", solver.variable("s").vectorQuantity.toString())
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
        assertNoIssues()
        assertEquals("500e-9", solver.variable("a").vectorQuantity.toString())
        assertEquals("5e-6", solver.variable("b").vectorQuantity.toString())
        assertEquals("50e-6", solver.variable("c").vectorQuantity.toString())
        assertEquals("500e-6", solver.variable("d").vectorQuantity.toString())
        assertEquals("0.005", solver.variable("e").vectorQuantity.toString())
        assertEquals("0.05", solver.variable("f").vectorQuantity.toString())
        assertEquals("0.5", solver.variable("g").vectorQuantity.toString())
        assertEquals("5", solver.variable("h").vectorQuantity.toString())
        assertEquals("50", solver.variable("i").vectorQuantity.toString())
        assertEquals("500", solver.variable("j").vectorQuantity.toString())
        assertEquals("5000", solver.variable("k").vectorQuantity.toString())
        assertEquals("50000", solver.variable("l").vectorQuantity.toString())
        assertEquals("500000", solver.variable("m").vectorQuantity.toString())
        assertEquals("5e6", solver.variable("n").vectorQuantity.toString())
        assertEquals("50e6", solver.variable("o").vectorQuantity.toString())
        assertEquals("500e6", solver.variable("p").vectorQuantity.toString())
        assertEquals("5e9", solver.variable("q").vectorQuantity.toString())
        assertEquals("50e9", solver.variable("r").vectorQuantity.toString())
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
        assertEquals("0.04..10000 N", solver.variable("result").vectorQuantity.toString())
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
        assertNoIssues()
        assertEquals("1..10000 F", solver.variable("result").vectorQuantity.toString())
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
        assertNoIssues()
        assertEquals("1 mΩ", solver.variable("result").vectorQuantity.toString())
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
        assertNoIssues()
        assertEquals("1 MS", solver.variable("result").vectorQuantity.toString())
    }

    @Test
    fun quantityToString6() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(1000000.0 .. 1000000.0 [s]);
            feature b: Quantities::ScalarQuantityValue(1000.0 .. 1000.0 [A]);
            feature result: ISQ::ElectricChargeValue = a*b; 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("1 GC", solver.variable("result").vectorQuantity.toString())
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
        assertNoIssues()
        assertEquals("4..9 TV", solver.variable("result").vectorQuantity.toString())
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
        assertNoIssues()
        assertEquals("2..6 mH", solver.variable("result").vectorQuantity.toString())
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
        assertNoIssues()
        assertEquals("2..6 kWb", solver.variable("result").vectorQuantity.toString())
    }


    @Test
    fun quantityToString10() = testSession("ISQ") {
        loadKerML("""
            feature b: ISQ::MassValue(1.0 .. 2.0 [kg]);
            feature c: Quantities::ScalarQuantityValue(10000.0 .. 10000.0 [s^2]); 
            feature d: ISQ::ElectricCurrentValue(100000.0 .. 100000.0 [A]);
            feature result: ISQ::MagneticFluxDensityValue = b/(c*d);
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("1..2 nT", solver.variable("result").vectorQuantity.toString())
    }

    @Test
    fun quantityToString11() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(20.0 .. 20.0 [m^2]); 
            feature b: ISQ::MassValue(1.0 .. 1.0 [kg]); 
            feature c: Quantities::ScalarQuantityValue(1000000.0 .. 1000000.0 [s^3]); 
            feature result: ISQ::PowerValue = a*b/c;
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("20 μW", solver.variable("result").vectorQuantity.toString())
    }

    @Test
    fun quantityToString12() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(2.0 .. 2.0 [km]);
            feature b: ISQ::DurationValue(100.0 .. 100.0 [s]);
            feature result: ISQ::SpeedValue = a/b;
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("20 m/s", solver.variable("result").vectorQuantity.toString())
    }

    @Test
    fun quantityToString13() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(2.0 .. 2.0 [km]);
            feature b: Quantities::ScalarQuantityValue(100.0 .. 100.0 [s^2])
            feature result: ISQ::AccelerationValue = a/b; 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertEquals("20 m/s^2", solver.variable("result").vectorQuantity.toString())
    }

    @Test
    fun quantityToString14() = testSession("ISQ") {
        loadKerML(
            """feature a: ISQ::LengthValue(200.0 .. 300.0 [m]);
                    feature b: ISQ::LengthValue(100.0 .. 100.0 [m]);
                    feature result: ISQ::AreaValue = a*b;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals("2..3 ha", solver.variable("result").vectorQuantity.toString())
    }

    @Test
    fun quantityToStringExplicitUnit() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(200.0 .. 300.0 [m]);
            feature b: ISQ::LengthValue(100.0 .. 100.0 [m]);
            feature resultArea: ISQ::AreaValue(* [m^2]) = a*b;
            feature c: Quantities::ScalarQuantityValue(1000000.0 .. 1000000.0 [s]);
            feature d: Quantities::ScalarQuantityValue(1000.0 .. 1000.0 [A]);
            feature resultCharge: ISQ::ElectricChargeValue(* [A s]) = c*d;
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("20000..30000 m^2", solver.variable("resultArea").vectorQuantity.toString())
        assertEquals("1e9 A s", solver.variable("resultCharge").vectorQuantity.toString())
    }

    @Test
    fun wrongUnitTypeAssignmentTest() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::ForceValue = 10.0 [N];
            feature b: ISQ::SpeedValue = a;
        """)
        solver.propagate()
        assertIssue("cannot be transferred")
    }

    @Test
    fun quantityToString15() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(0.11 .. 0.11 [m]);
            feature result: ISQ::LengthValue = a;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals("0.11 m", solver.variable("result").vectorQuantity.toString())
    }

    @Test
    fun quantityToString16() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(0.05 .. 0.05 [m]);
            feature result: ISQ::LengthValue = a;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals("0.05 m", solver.variable("result").vectorQuantity.toString())
    }

    @Test
    fun quantityToString17() = testSession("ISQ") {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue(0.05 .. 0.05 [m^3]);
            feature result: ISQ::VolumeValue = a; """)
        solver.propagate()
        assertNoIssues()
        assertEquals("0.05 m^3", solver.variable("result").vectorQuantity.toString())
    }

    @Test
    fun quantityToString18() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::SpeedValue(* [km/h]) = [0.0 .. 130.0] [km/h].
            feature b: ISQ::DurationValue= 1.0 [s];
            feature result: ISQ::LengthValue= a*b.
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("0..130 km / h", solver.variable("a").vectorQuantity.toString())
        assertEquals("1 s", solver.variable("b").vectorQuantity.toString())
        assertEquals("0..36.111 m", solver.variable("result").vectorQuantity.toString())
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
        // assertEquals("∅", solver.variable("a").vectorQuantity.toString())
        // assertEquals("∅", solver.variable("b").vectorQuantity.toString())
        assertEquals("∅ m", solver.variable("result").vectorQuantity.toString())
        assertIssue("is not satisfiable")
    }

    @Test
    fun returnInputType1() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Real (0 .. 100);
        """)
        solver.propagate()
        assertNoIssues()
        val representer = Representer()
        assertEquals("0..100", representer.represent(solver.variable("a").aadd()))
    }

    @Test
    fun returnInputType2() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Real = 33.0;
        """)
        solver.propagate()
        assertNoIssues()
        val representer = Representer()
        assertEquals("33", representer.represent(solver.variable("a").aadd()))
    }

    @Test
    fun returnInputType3() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Real (0 .. 0);
        """)
        solver.propagate()
        assertNoIssues()
        val representer = Representer()
        assertEquals("0", representer.represent(solver.variable("a").aadd()))
    }

    @Test
    fun scalarInfinity()
    {
        val rr : ClosedRange<DoubleBound> = DoubleBound.PositiveInfinity ..DoubleBound.PositiveInfinity
        assertEquals("*", Representer().represent(DDBuilder().real(rr)))
    }

    @Test
    fun integer()
    {
        val b = DDBuilder()
        assertEquals("∅", VectorQuantity(b.Reals.Empty, "1").toString())
        assertEquals("∅", VectorQuantity(b.Integers.Empty).toString())
    }
}
