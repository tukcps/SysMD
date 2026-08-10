package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
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
        assertEquals("m", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertEquals(2.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(3.0, solver.getVariable("b")!!.max(), 0.00001)
    }

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit2() = testSession("ISQ")  {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue { :>> range = 4.0 .. 9.0 [Ohm^2];}
            feature b: ISQ::ResistanceValue = sqrt(a); 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("2..3 Ω", solver.getVariable("b")!!.vectorQuantity.toString())
    }

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit3() = testSession("ISQ")  {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue {:>> range = 2..9 [Ohm^2 m^2];}
            feature b: Quantities::ScalarQuantityValue =sqrt(a){  :>> range = 2.0 .. 9.0 [Ohm m];}
        """, Runlevel.ALL)
        assertEquals("kg m^3 / A^2 s^3", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit4() = testSession("ISQ")  {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue {:>> range = 2..9 [Pa^4 J^2 V^8 / A^6 N^2];}
            feature b: Quantities::ScalarQuantityValue = sqrt(a){:>> range = 2..9 [Pa^2 J V^4 / A^3 N];}
        """, Runlevel.ALL)
        assertEquals("kg^6 m^7 / A^7 s^16", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** Quantity with no unit */
    @Test
    fun sqr_unit() = testSession("Ranges")  {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = 2.0E16 .. 9.0E16;}
            feature b: ScalarValues::Real = sqr(a);
        """, Runlevel.ALL)
        assertEquals("400e30..8.1e33", solver.getVariable("b")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    /** Quantity with sqr */
    @Test
    fun sqr_unit1() = testSession("ISQ")  {
        loadKerML("""
            feature a: ISQ::LengthValue{:>> range = 2.0 .. 9.0 [m];}
            feature b: ISQ::AreaValue = sqr(a);
        """, Runlevel.ALL
        )
        assertEquals("m^2", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** Quantity with sqr */
    @Test
    fun sqr_unit2() = testSession("ISQ")  {
        loadKerML("""
            feature a: ISQ::ResistanceValue{ :>> range = 2.0 .. 9.0 [Ohm];}
            feature b: Quantities::ScalarQuantityValue = sqr(a){:>> range = (*..*) [Ohm^2];}
        """)
        solver.propagate()
        assertEquals("kg^2 m^4 / A^4 s^6", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertEquals("Resistance", solver.getVariable("a")!!.vectorQuantity.getDomain())
        assertNoIssues()
    }

    /** Quantity with sqr */
    @Test
    fun sqr_unit3() = testSession("ISQ")  {
        loadKerML("""
            feature a: Quantities::ScalarQuantityValue { :>> range = 2.0 .. 9.0 [Ohm m];}
            feature b: Quantities::ScalarQuantityValue =sqr(a){ :>> range = 2.0 .. 9.0 [Ohm^2 m^2];}""")
        solver.propagate()
        assertEquals("kg^2 m^6 / A^4 s^6", solver.getVariable("b")!!.vectorQuantity.unit.toString())
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
        assertEquals("kg^12 m^14 / A^14 s^32", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

}
