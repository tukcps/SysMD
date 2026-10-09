package constraintnettests.functionstests

import util.variable
import util.assertEmpty
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class SqrTests {

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit1() = testSession("ISQ")  {
        loadKerML("""
           feature a: ISQ::AreaValue { :>> range = 4.0..9.0 [m^2];}
           feature b: ISQ::LengthValue  = sqrt(a); 
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals("m", solver.variable("b").vectorQuantity.unit.toString())
        assertBounds(2.0 .. 3.0, solver.variable("b"))
    }

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit2() = testSession("ISQ")  {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue { :>> range = 4.0 .. 9.0 [Ohm^2];}
            feature b: ISQ::ResistanceValue = sqrt(a); 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("2..3 Ω", solver.variable("b").vectorQuantity.toString())
    }

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit3() = testSession("ISQ")  {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue {:>> range = 2..9 [Ohm^2 m^2];}
            feature b: Quantities::ScalarQuantityValue =sqrt(a){  :>> range = 2.0 .. 9.0 [Ohm m];}
        """, Runlevel.ALL)
        assertEquals("kg m^3 / A^2 s^3", solver.variable("b").vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit4() = testSession("ISQ")  {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue {:>> range = 2..9 [Pa^4 J^2 V^8 / A^6 N^2];}
            feature b: Quantities::ScalarQuantityValue = sqrt(a){:>> range = 2..9 [Pa^2 J V^4 / A^3 N];}
        """, Runlevel.ALL)
        assertEquals("kg^6 m^7 / A^7 s^16", solver.variable("b").vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** Quantity with no unit */
    @Test
    fun sqr_unit() = testSession("Ranges")  {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = 2.0E16 .. 9.0E16;}
            feature b: ScalarValues::Real = sqr(a);
        """, Runlevel.ALL)
        assertEquals("400e30..8.1e33", solver.variable("b").vectorQuantity.toString())
        assertNoIssues()
    }

    /** Quantity with sqr */
    @Test
    fun sqr_unit1() = testSession("ISQ")  {
        loadKerML("""
            feature a: ISQ::LengthValue{:>> range = 2.0 .. 9.0 [m];}
            feature b: ISQ::AreaValue = sqr(a);
        """, Runlevel.ALL)
        assertEquals("m^2", solver.variable("b").vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** Quantity with sqr */
    @Test
    fun sqr_unit2() = testSession("ISQ")  {
        loadKerML("""
            feature a: ISQ::ResistanceValue{ :>> range = 2.0 .. 9.0 [Ohm];}
            feature b: Quantities::ScalarQuantityValue = sqr(a){:>> range = (*..*) [Ohm^2];}
        """, Runlevel.ALL)
        assertEquals("kg^2 m^4 / A^4 s^6", solver.variable("b").vectorQuantity.unit.toString())
        assertEquals("Resistance", solver.variable("a").vectorQuantity.getDomain())
        assertNoIssues()
    }

    /** Quantity with sqr */
    @Test
    fun sqr_unit3() = testSession("ISQ")  {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue { :>> range = 2.0 .. 9.0 [Ohm m];}
            feature b: Quantities::ScalarQuantityValue =sqr(a){ :>> range = 2.0 .. 9.0 [Ohm^2 m^2];}
        """, Runlevel.ALL)
        assertEquals("kg^2 m^6 / A^4 s^6", solver.variable("b").vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /**
     * Quantity with sqr
     */
    @Test
    fun sqr_unit4() = testSession("ISQ")  {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue { :>> range = 2.0 .. 9.0 [Pa^2 J V^4 / A^3 N];}
            feature b: Quantities::ScalarQuantityValue = sqr(a) { :>> range = 4.0 .. 81.0 [Pa^4 J^2 V^8 / A^6 N^2];} 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("kg^12 m^14 / A^14 s^32", solver.variable("b").vectorQuantity.unit.toString())
    }

    @Test
    fun sqr_evalDown_symmetric() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -10.0 .. 10.0;}
            feature b: Ranges::RealInRange = sqr(a) {:>> range = 4.0 .. 9.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        assertBounds(-3.0 .. 3.0, a)
    }

    @Test
    fun sqr_evalDown_negative_result() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -10.0 .. 10.0;}
            feature b: Ranges::RealInRange = sqr(a) {:>> range = -9.0 .. -4.0;}
        """)
        solver.propagate()
        val b = solver.variable("b")
        assertEmpty(b)
        assert(status.issues.any { it.kind == Issue.Kind.WARN_INCONSISTENCY })
    }

    @Test
    fun sqr_evalDown_int_symmetric() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = -10 .. 10;}
            feature b: Ranges::IntegerInRange = sqr(a) {:>> range = 4 .. 9;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        assertBounds(-3L .. 3L, a)
    }

    @Test
    fun sqr_evalDown_int_negative_result() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = -10 .. 10;}
            feature b: Ranges::IntegerInRange = sqr(a) {:>> range = -9 .. -4;}
        """, Runlevel.ALL)
        val a = solver.variable("a")
        assertEmpty(a)
    }

    /**
     * Regression test for sqr evalDown:
     * inverseSqr([4..9]) produces [-3..3].
     * With 'a' initially in [-10..10], constraining with [-3..3] yields [-3..3].
     */
    @Test
    fun sqr_evalDown_constrains_existing_bounds_regression() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -10.0 .. 1.0;}
            feature b: Ranges::RealInRange = sqr(a) {:>> range = 4.0 .. 9.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        assertBounds(-3.0 .. 1.0, a)
    }

    /**
     * Integer counterpart for sqr evalDown constraint preservation.
     */
    @Test
    fun sqr_evalDown_int_constrains_existing_bounds_regression() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = -10 .. 1;}
            feature b: Ranges::IntegerInRange = sqr(a) {:>> range = 4 .. 9;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        assertBounds(-3L .. 1L, a)
    }

    /**
     * Regression test: sqr evalDown preserves unit of input parameter.
     */
    @Test
    fun sqr_evalDown_preserves_unit_regression() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue {:>> range = -10.0 .. 10.0 [m];}
            feature b: ISQ::AreaValue = sqr(a) {:>> range = 4.0 .. 9.0 [m^2];}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        assertEquals("m", a.vectorQuantity.unit.toString())
        assertBounds(-3.0 .. 3.0, a)
    }
}
