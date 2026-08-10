package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpTests {
    /** ConstNet shall compute bottom-up with exp in real and model.builder.range */
    @Test
    fun evalUpWithExp_real_range() = testSession("ScalarValues") {
        loadKerML("feature b: ScalarValues::Real = exp([1.0 .. 5.0]);")
        solver.propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(Math.E, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(Math.E.pow(5), solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in real and value */
    @Test
    fun evalUpWithExp_real_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = 3.0 {:>> range = 1.0 .. 5.0;}
            feature b: ScalarValues::Real = exp(a);""", Runlevel.ALL)
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(Math.E.pow(3), solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(Math.E.pow(3), solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }


    /** ConstNet shall compute bottom-up with pow2 with negative value*/
    @Test
    fun evalUpWithExp_real_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -3.0 .. -1.0;}
            feature b: ScalarValues::Real = exp(a);""", Runlevel.ALL)
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(Math.E.pow(-3), solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(Math.E.pow(-1), solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 and zero */
    @Test
    fun evalUpWithExp_real_zero() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 0.0;}
            feature b: ScalarValues::Real = exp(a);""", Runlevel.ALL)
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(1.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(1.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with exp in int and model.builder.range */
    @Test
    fun evalUpWithExp_int_range() = testSession("Ranges") {
        loadKerML("feature b: ScalarValues::Integer = exp([1 .. 5]).", Runlevel.ALL)
        assertNoIssues()
        assertEquals(floor(Math.E).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(5)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in int and value */
    @Test
    fun evalUpWithExp_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange  = 3 {:>> range = 1 .. 5;}
            feature b: ScalarValues::Integer = exp(a);
        """, Runlevel.ALL)
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(floor(Math.E.pow(3)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(3)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    @Test
    fun evalDownWithExp_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange  {:>> range = 0 .. 5;}
            feature b: Ranges::IntegerInRange = exp(a) {:>> range = 1 .. 1;} 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(0, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(0, solver.getVariable("a")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
    }

    @Test
    fun evalUpWithExp_int_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange  = -2 {:>> range = -5 .. -1;}
            feature b: ScalarValues::Integer = exp(a);
        """, Runlevel.ALL
        )
        assertNoIssues()
        assertEquals(floor(Math.E.pow(-2)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(-2)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    @Test
    fun evalUpWithExp_int() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = 3 .. 3;}
            feature b: ScalarValues::Integer = exp(a);
        """, Runlevel.ALL
        )
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        solver.propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(floor(Math.E.pow(3)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(3)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }
}
