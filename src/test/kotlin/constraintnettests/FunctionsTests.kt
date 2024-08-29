package constraintnettests

import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.math.*

/**
 * Tests of the pre-defined functions
 */
class FunctionsTests {

    @Test
    fun absTestReal() = testSession {
        loadSysMD("""
                feature qa: ScalarValues::Real(5.0 .. 5.0); 
                feature qb: ScalarValues::Real(1.0 .. 5.0); 
                feature qc: ScalarValues::Real(0.0 .. 1.0); 
                feature qd: ScalarValues::Real(0.0 .. 0.0); 
                feature qe: ScalarValues::Real(-1.0 .. 5.0); 
                feature qf: ScalarValues::Real(-5.0 .. 5.0); 
                feature qg: ScalarValues::Real(-5.0 .. 1.0); 
                feature qh: ScalarValues::Real(-5.0 .. -1.0); 
                feature qi: ScalarValues::Real(-5.0 .. 0.0); 
                feature qj: ScalarValues::Real(-5.0 .. -5.0); 
                feature a: ScalarValues::Real = abs(qa); 
                feature b: ScalarValues::Real = abs(qb); 
                feature c: ScalarValues::Real = abs(qc); 
                feature d: ScalarValues::Real = abs(qd); 
                feature e: ScalarValues::Real = abs(qe); 
                feature f: ScalarValues::Real = abs(qf); 
                feature g: ScalarValues::Real = abs(qg); 
                feature h: ScalarValues::Real = abs(qh); 
                feature i: ScalarValues::Real = abs(qi); 
                feature j: ScalarValues::Real = abs(qj);
            """.trimIndent()
        )
        propagate()
        assertEquals(5.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("a")!!.aadd().getRange().max, 0.00001)
        assertEquals(1.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().max, 0.00001)
        assertEquals(0.0, global.resolveVar("c")!!.aadd().getRange().min, 0.00001)
        assertEquals(1.0, global.resolveVar("c")!!.aadd().getRange().max, 0.00001)
        assertEquals(0.0, global.resolveVar("d")!!.aadd().getRange().min, 0.00001)
        assertEquals(0.0, global.resolveVar("d")!!.aadd().getRange().max, 0.00001)
        assertEquals(0.0, global.resolveVar("e")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("e")!!.aadd().getRange().max, 0.00001)
        assertEquals(0.0, global.resolveVar("f")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("f")!!.aadd().getRange().max, 0.00001)
        assertEquals(0.0, global.resolveVar("g")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("g")!!.aadd().getRange().max, 0.00001)
        assertEquals(1.0, global.resolveVar("h")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("h")!!.aadd().getRange().max, 0.00001)
        assertEquals(0.0, global.resolveVar("i")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("i")!!.aadd().getRange().max, 0.00001)
        assertEquals(5.0, global.resolveVar("j")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("j")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun absTestInteger() = testSession {
        loadSysMD("""
            feature qa: ScalarValues::Integer(5 .. 5);
            feature a: ScalarValues::Integer = abs(qa);
            """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(5, global.resolve<Feature>("a")!!.variable!!.idd().getRange().min)
        assertEquals(5, global.resolve<Feature>("a")!!.variable!!.idd().getRange().max)
    }


    /**
     * Test of the function toReal(x) : Boolean -> ScalarValues::Real.
     */
    @Test
    fun toRealTest() = testSession {
        loadSysMD("""
                feature a: ScalarValues::Boolean = true; 
                feature b: ScalarValues::Real = toReal(a); 
                feature c: ScalarValues::Boolean = false; 
                feature d: ScalarValues::Real = toReal(c);  
                feature e: ScalarValues::Boolean;
                feature f: ScalarValues::Real = toReal(e);
        """.trimIndent())
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val b = global.resolve<Feature>("b")!!.variable!!
        val d = global.resolve<Feature>("d")!!.variable!!
        val f = global.resolve<Feature>("f")!!.variable!!
        assertEquals(1.0, b.aadd().min, 0.00000001)
        assertEquals(1.0, b.aadd().max, 0.00000001)
        assertEquals(0.0, d.aadd().min, 0.00000001)
        assertEquals(0.0, d.aadd().max, 0.00000001)
        assertEquals(0, d.vectorQuantity.value.height())
        assertEquals(0.0, f.aadd().min, 0.00000001)
        assertEquals(1.0, f.aadd().max, 0.00000001)
        assertEquals(1, f.vectorQuantity.value.height())
    }

    /** ConstNet shall compute bottom-up with pow2 in real and model.builder.range */
    @Test
    fun evalUpWithPow2_real_range() = testSession {
        loadSysMD("""
            feature a: ScalarValues::Real(1.0 .. 5.0);
            feature b: ScalarValues::Real = power2(a);"""
        )
        val b = global.resolveVar("b")
        assertNotNull(b)
        assertEquals(2.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(32.0, global.resolveVar("b")!!.max(), 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with pow2 in real and value */
    @Test
    fun evalUpWithPow2_real_value() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(1.0 .. 5.0) = 3.0;
            feature b: ScalarValues::Real = power2(a); """
        )
        assertEquals(8.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().min, 0.00001)
        assertEquals(8.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolve<Feature>("b")!!.variable!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with pow2 and negative values*/
    @Test
    fun evalUpWithPow2_real_negative() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(-2.0 .. -1.0);
            feature b: ScalarValues::Real = power2(a);"""
        )
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0.25, global.resolveVar("b")!!.aadd().min, 0.0001)
        assertEquals(0.5, global.resolveVar("b")!!.aadd().max, 0.0001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 with zero*/
    @Test
    fun evalUpWithPow2_real_zero() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(0.0 .. 0.0);
            feature b: ScalarValues::Real = power2(a); """
        )
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("b")!!.aadd().min, 0.0001)
        assertEquals(1.0, global.resolveVar("b")!!.aadd().max, 0.0001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in int and model.builder.range*/
    @Test
    fun evalUpWithPow2_int_range() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Integer(1 .. 5);
            feature b: ScalarValues::Integer = power2(a); """
        )
        initialize()
        propagate()
        assertEquals(2, (global.resolveVar("b")!!.idd() as IDD.Leaf).value.min)
        assertEquals(32, (global.resolveVar("b")!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with pow2 in int and model.builder.range*/
    @Test
    fun evalUpWithPow2_int_value() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Integer(1 .. 5) = 3;
            feature b: ScalarValues::Integer = power2(a); """
        )
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        propagate()
        assertEquals(8, (global.resolveVar("b")!!.idd() as IDD.Leaf).value.min)
        assertEquals(8, (global.resolveVar("b")!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with exp in real and model.builder.range */
    @Test
    fun evalUpWithExp_real_range() = testSession {
        loadSysMD("feature b: ScalarValues::Real = exp([1.0 .. 5.0]);")
        propagate()
        assertEquals(0, status.exceptions.size, "Error message: ${status.exceptions}")
        assertEquals(Math.E, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(Math.E.pow(5), global.resolveVar("b")!!.max(), 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in real and value */
    @Test
    fun evalUpWithExp_real_value() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(1.0 .. 5.0) = 3.0;
            feature b: ScalarValues::Real = exp(a);"""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error message: ${status.exceptions}")
        assertEquals(Math.E.pow(3), global.resolve<Feature>("b")!!.variable!!.aadd().getRange().min, 0.00001)
        assertEquals(Math.E.pow(3), global.resolve<Feature>("b")!!.variable!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolve<Feature>("b")!!.variable!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 with negative value*/
    @Test
    fun evalUpWithExp_real_negative() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(-3.0 .. -1.0);
            feature b: ScalarValues::Real = exp(a);"""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error message: ${status.exceptions}")
        assertEquals(Math.E.pow(-3), global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(Math.E.pow(-1), global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 and zero */
    @Test
    fun evalUpWithExp_real_zero() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(0.0 .. 0.0);
            feature b: ScalarValues::Real = exp(a);"""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error message: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with exp in int and model.builder.range */
    @Test
    fun evalUpWithExp_int_range() = testSession {
        loadSysMD("feature b: ScalarValues::Integer = exp([1 .. 5]).")
        propagate()
        assertEquals(0, status.exceptions.size, "Error message: ${status.exceptions}")
        assertEquals(floor(Math.E).toLong(), global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(5)).toLong(), global.resolveVar("b")!!.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in int and value */
    @Test
    fun evalUpWithExp_int_value() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Integer(1 .. 5) = 3;
            feature b: ScalarValues::Integer = exp(a);"""
        )
        assertEquals(0, status.exceptions.size, "Error message: ${status.exceptions}")
        propagate()
        assertEquals(0, status.exceptions.size, "Error message: ${status.exceptions}")
        assertEquals(floor(Math.E.pow(3)).toLong(), global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(3)).toLong(), global.resolveVar("b")!!.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with ln in real and model.builder.range */
    @Test
    fun evalUpWithLog_real_range() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(1.0 .. 5.0).
            feature b: ScalarValues::Real = ln(a)."""
        )
        propagate()
        assertEquals(ln(1.0), global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(ln(5.0), global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with ln in real and value */
    @Test
    fun evalUpWithLog_real_value() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(1.0 .. 5.0) = 3.0.
            feature b: ScalarValues::Real = ln(a)."""
        )
        propagate()
        assertEquals(ln(3.0), global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(ln(3.0), global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with ln in real and negative numbers => not possible */
    @Test @Disabled //must be deactivated because of evalDown of Power function
    fun evalUpWithLog_real_negative() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(-10.0 .. -5.0).
            feature b: ScalarValues::Real = ln(a)."""
        )
        propagate()
        assertEquals(1, status.exceptions.size, "Error messages: ${status.exceptions}")
        assert("Log only possible for values greater than zero" in status.exceptions.elementAt(0).toString())
    }

    /** ConstNet shall compute bottom-up with ln in int and model.builder.range */
    @Test
    fun evalUpWithLog_int_range() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Integer(1 .. 5).
            feature b: ScalarValues::Integer = ln(a)."""
        )
        propagate()
        assertEquals(floor(ln(1.0)).toLong(), global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(ceil(ln(5.0)).toLong(), global.resolveVar("b")!!.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with ln in int and value */
    @Test
    fun evalUpWithLog_int_value() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Integer(1 .. 5) = 3;
            feature b: ScalarValues::Integer = ln(a);"""
        )
        propagate()
        assertEquals(floor(ln(3.0)).toLong(), global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(ceil(ln(3.0)).toLong(), global.resolveVar("b")!!.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with sqrt in real and model.builder.range */
    @Test
    fun evalUpWithSqrt_real_range() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(2.0 .. 9.0);
            feature b: ScalarValues::Real = sqrt(a);"""
        )
        propagate()
        assertEquals(sqrt(2.0), global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        //AADD returns bigger result for upper border
        assert(sqrt(9.0) <= global.resolveVar("b")!!.aadd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with sqrt in real and value */
    @Test
    fun propagateWithSqrt_real_value() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(2.0 .. 5.0) = 3.0;
            feature b: ScalarValues::Real = sqrt(a);"""
        )
        propagate()
        assertEquals(sqrt(3.0), global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(sqrt(3.0), global.resolveVar("b")!!.max(), 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with sqrt and negative value.This should lead to an error*/
    @Test
    fun propagateWithSqrt_real_negative() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(-10.0 .. -5.0);
            feature b: ScalarValues::Real = sqrt( a );"""
        )
        propagate()
        assertEquals(1, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertTrue("Sqrt only possible for values greater or equal than zero" in status.exceptions.toString())
    }

    /** ConstNet shall compute bottom-up with sqrt in int and model.builder.range */
    @Test
    fun evalUpWithSqrt_int_range() = testSession {
            loadSysMD("""
            feature a: ScalarValues::Integer(2 .. 9);
            feature b: ScalarValues::Integer = sqrt(a);"""
            )
            propagate()
            assertEquals(floor(sqrt(2.0)).toLong(), global.resolve<Feature>("b")!!.variable!!.idd().getRange().min)
            assertEquals(ceil(sqrt(9.0)).toLong(), global.resolve<Feature>("b")!!.variable!!.idd().getRange().max)
            assertEquals("1", global.resolve<Feature>("b")!!.variable!!.vectorQuantity.unit.toString())
            assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with sqrt in int **/
    @Test
    fun evalUpWithSqrt_int_value() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Integer(2 .. 9) = 3.
            feature b: ScalarValues::Integer = sqrt(a)."""
        )
        propagate()
        assertEquals(floor(sqrt(3.0)).toLong(), global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(ceil(sqrt(3.0)).toLong(), global.resolveVar("b")!!.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with sqr in real and model.builder.range */
    @Test
    fun evalUpWithSqr_real_range() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(2.0 .. 9.0).
            feature b: ScalarValues::Real = sqr(a)."""
        )
        propagate()
        assertEquals(4.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(81.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with sqr in real and value */
    @Test
    fun evalUpWithSqr_real_value() = testSession {
        loadSysMD("""
            feature a: ScalarValues::Real(1.0 .. 5.0) = 3.0.
            feature b: ScalarValues::Real = sqr(a)."""
        )
        propagate()
        assertEquals(9.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().min, 0.00001)
        assertEquals(9.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolve<Feature>("b")!!.variable!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with sqr in int and model.builder.range */
    @Test
    fun evalUpWithSqr_int_range() = testSession {
        loadSysMD("""
            feature a: ScalarValues::Integer(2 .. 9).
            feature b: ScalarValues::Integer = sqr(a)."""
        )
        propagate()
        assertEquals(4, (global.resolve<Feature>("b")!!.variable!!.idd() as IDD.Leaf).value.min)
        assertEquals(81, (global.resolve<Feature>("b")!!.variable!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", global.resolve<Feature>("b")!!.variable!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute bottom-up with sqr in int and value */
    @Test
    fun evalUpWithSqr_int_value() = testSession {
        loadSysMD("""
            feature a: ScalarValues::Integer(2 .. 9) = 3 .
            feature b: ScalarValues::Integer = sqr(a)."""
        )
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(9, (global.resolve<Feature>("b")!!.variable!!.idd() as IDD.Leaf).value.min)
        assertEquals(9, (global.resolve<Feature>("b")!!.variable!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", global.resolve<Feature>("b")!!.variable!!.vectorQuantity.unit.toString())
    }

    @Test
    fun evalUpWithPowB_real_value() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(3.0 .. 3.0).
            feature b: ScalarValues::Real(4.0 .. 4.0).
            feature c: ScalarValues::Real = power(a, b)."""
        )
        val c = global.resolveVar("c") !!
        assertEquals(81.0, c.aadd().getRange().min, 0.000001)
        assertEquals(81.0, c.aadd().getRange().max, 0.000001)
        assertEquals("1", c.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun evalUpWithPowB_real_range() = testSession {
        loadSysMD(
            """
            feature a: ScalarValues::Real(1.5 .. 3.5).
            feature b: ScalarValues::Real(2.5 .. 4.5).
            feature c: ScalarValues::Real = power(a, b)."""
        )
        propagate()
        assertEquals(1.5.pow(2.5), global.resolveVar("c")!!.min(), 0.000001)
        assertEquals(3.5.pow(4.5), global.resolveVar("c")!!.max(), 0.000001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun evalUpWithPowB_int_value() = testSession {
        loadSysMD("""
            feature a: ScalarValues::Integer = 3.
            feature b: ScalarValues::Integer = 3.
            feature c: ScalarValues::Integer = power(a, b)."""
        )
        propagate()
        assertEquals(27, global.resolveVar("c")!!.idd().getRange().min)
        assertEquals(27, global.resolveVar("c")!!.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun evalUpWithPow_int_range() = testSession {
        loadSysMD("""
            feature a: ScalarValues::Integer(2 .. 3).
            feature b: ScalarValues::Integer(3 .. 4).
            feature c: ScalarValues::Integer = power(a, b)."""
        )
        val c = global.resolveVar("c") !!
        assertEquals(8, c.idd().getRange().min)
        assertEquals(81, c.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun evalUpPowerNegativeBase() = testSession {
        loadSysMD("""
            feature i: ScalarValues::Real = 1.0.
            feature a: ScalarValues::Real = pow(-5.0,1.0)."""
        )
        propagate()
        assertEquals(-5.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun evalUpPowerNegativeBase2() = testSession {
        loadSysMD("""
            feature i: ScalarValues::Real = 1.0.
            feature a: ScalarValues::Real = pow(-1.0,2.0)."""
        )
        propagate()
        assertEquals(1.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun evalUpPowerSpecialCase() = testSession {
        loadSysMD(""" 
            feature a: ScalarValues::Real(0.1..2.0); 
            feature b: ScalarValues::Real = pow(a, [1.0..2.0]).""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        assertEquals(0.1, a.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.0, a.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.01, b.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(4.0, b.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun sumEvalUp1() = testSession {
        loadSysMD("""
            feature i: ScalarValues::Real.
            feature a: ScalarValues::Real(1.0..3.0).
            feature b: ScalarValues::Real(3.0..5.0).
            feature sum: ScalarValues::Real = sum_i( a, b, i ).
            """
        )
        propagate()
        assertEquals(15.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        assertEquals(3.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun sumEvalUp2() = testSession {
        loadSysMD(
            """
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real(0.0..0.0);
            feature b: ScalarValues::Real(1.0..2.0);
            feature s: ScalarValues::Real(4.0..5.0);
            feature t: ScalarValues::Real(2.0..2.0);
            feature sum: ScalarValues::Real = sum_i( a, b, s-t*i );""".trimIndent()
        )
        propagate()
        assertEquals(9.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        assertEquals(6.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    @Test
    fun sumEvalDown() = testSession {
        loadSysMD("""
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real(1..3);
            feature b: ScalarValues::Real(1..5);
            feature sum: ScalarValues::Real(3.0..10.0) = sum_i( a, b, i );"""
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(3.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(10.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        assertEquals(1.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
        assertEquals(3.0, global.resolveVar("a")!!.aadd().getRange().max, 0.00001)
        assertEquals(1.5, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        // with int the borders would be 3 and 4, but rounding makes the intervals bigger
        assertEquals(4.5, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun sumEvalDown2() = testSession {
        loadSysMD(
            """
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real(0..6);
            feature b: ScalarValues::Real(3..5);
            feature sum: ScalarValues::Real(3.0..14.0) = sum_i( a, b, i );"""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(3.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(14.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        assertEquals(3.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals(0.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.5, global.resolveVar("a")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    //Calculations with evalDown do not work
    @Disabled
    @Test
    fun sumEvalDownWithMultiplication() = testSession {
        loadSysMD(
            """
            feature i: ScalarValues::Real.
            feature a: ScalarValues::Real.
            feature s: ScalarValues::Real = 10.0.
            feature b: ScalarValues::Real(3.0..5.0).
            feature sum: ScalarValues::Real(30.0..140.0) = sum_i( a, b, s*i )."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(30.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(140.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        assertEquals(3.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().max, 0.00001)
        assertEquals(1.5, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
        assertEquals(3.5, global.resolveVar("a")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegative() = testSession {
        loadSysMD(
            """
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real(1.0..1.0);
            feature b: ScalarValues::Real(5.0..5.0);
            feature sum: ScalarValues::Real = sum_i( a, b, pow(-1.0,i)*i );"""
        )
        propagate()
        assertEquals(-3.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(-3.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegativeTest2() = testSession {
        loadSysMD(
            """
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real(-3.0..0.0);
            feature b: ScalarValues::Real(0.0..3.0);
            feature sum: ScalarValues::Real = sum_i( a, b, pow(-1.0,i)*(1.0-sqr(i)) );"""
        )
        propagate()
        assertEquals(-5.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(11.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegativeTest3() = testSession {
        loadSysMD(
            """
            feature i: ScalarValues::Real.
            feature a: ScalarValues::Real(-3.0..0.0).
            feature b: ScalarValues::Real(0.0..3.0).
            feature sum: ScalarValues::Real = sum_i( a, b, -pow(-1.0,i)*(1.0-sqr(i)) )."""
        )
        propagate()
        assertEquals(-11.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegativeTest4() = testSession {
        loadSysMD(
            """
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real(-3.0..0.0);
            feature b: ScalarValues::Real(0.0..3.0);
            feature sum: ScalarValues::Real = sum_i( a, b, -pow(-1.0,i));"""
        )

        propagate()
        assertEquals(-1.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(1.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    @Test
    fun sumEvalDownInt() = testSession {
        loadSysMD(
            """
             feature i: ScalarValues::Integer;
             feature a: ScalarValues::Integer(1..3);
             feature b: ScalarValues::Integer(3..4);
             feature sum: ScalarValues::Integer(3..10) = sum_i( a, b, i );"""
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(3, global.resolveVar("sum")!!.idd().getRange().min)
        assertEquals(10, global.resolveVar("sum")!!.idd().getRange().max)
        assertEquals(1, global.resolveVar("a")!!.idd().getRange().min)
        assertEquals(3, global.resolveVar("a")!!.idd().getRange().max)
        assertEquals(3, global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(4, global.resolveVar("b")!!.idd().getRange().max)
    }

    @Disabled //Does not work for ScalarValues::Integer
    @Test
    fun sumEvalDownInt2() = testSession {
        loadSysMD(
            """
            feature i: ScalarValues::Integer;
            feature a: ScalarValues::Integer;
            feature b: ScalarValues::Integer(3..5);
            feature sum: ScalarValues::Integer(3..14) = sum_i( a, b, i );"""
        )

        initialize()
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(9, global.resolveVar("sum")!!.idd().getRange().min)
        assertEquals(10, global.resolveVar("sum")!!.idd().getRange().max)
        assertEquals(2, global.resolveVar("a")!!.idd().getRange().min)
        assertEquals(3, global.resolveVar("a")!!.idd().getRange().max)
        assertEquals(3, global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(5, global.resolveVar("b")!!.idd().getRange().max)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun maxTest1() = testSession {
        loadSysMD(
            input = """
            feature a: ScalarValues::Real(0..1);
            feature b: ScalarValues::Real(1..2);
            feature c: ScalarValues::Real = max(a,b);
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("c")
        assertEquals(1.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(2.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun maxTest1EvalDown() = testSession {
        loadSysMD(input = """
            feature a: ScalarValues::Real(1..7);
            feature b: ScalarValues::Real(8.0..8.0) = max(7.0,2.0+a);
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("a")
        assertEquals(6.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(6.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun maxTest1bEvalDown() = testSession {
        loadSysMD(
            input = """
            feature a: ScalarValues::Real(1..7);
            feature b: ScalarValues::Real(8.0..8.0) = max(8.0,2.0+a);
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("a")
        assertEquals(1.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(6.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun maxTest2() = testSession {
        loadSysMD(input = """
            feature a: ScalarValues::Real(0..5) = sqrt(9.0);
            feature b: ScalarValues::Real(1..6) = sqrt(4.0);
            feature c: ScalarValues::Real = max(a,b);
            """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("c")
        assertEquals(3.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(3.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun maxTest2EvalDown() = testSession {
        loadSysMD(input = """
            feature a: ScalarValues::Real(0..6) = sqrt(9.0);
            feature b: ScalarValues::Real(1..6);
            feature c: ScalarValues::Real(4.0..4.0) = max(a,b);
            """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("b")
        assertEquals(4.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(4.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun maxTest3() = testSession {
        loadSysMD(input = """
            feature a: ScalarValues::Real(0..5) = 4.0;
            feature b: ScalarValues::Real(1..6) = 6.0;
            feature c: ScalarValues::Real = max(sqrt(9.0)+a,sqrt(4.0)+b);
            """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("c")
        assertEquals(8.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(8.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }


    @Test
    fun maxTest4() = testSession {
        loadSysMD(input = """
                  feature c: ScalarValues::Real = max(sqrt(9.0)+3.0,sqrt(4.0)+5.0);
            """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("c")
        assertEquals(7.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(7.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun minTest1() = testSession {
        loadSysMD(
            input = """
            feature a: ScalarValues::Real(0..1);
            feature b: ScalarValues::Real(1..2);
            feature c: ScalarValues::Real = min(a,b);
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("c")
        assertEquals(0.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(1.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun minTest1EvalDown() = testSession {
        loadSysMD(
            input = """
            feature a: ScalarValues::Real(1..8);
            feature b: ScalarValues::Real(6.0..6.0) = min(8.0,a);
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("a")
        assertEquals(6.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(6.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun minTest2() = testSession {
        loadSysMD(
            input = """
            feature a: ScalarValues::Real(0..5) = sqrt(9.0);
            feature b: ScalarValues::Real(1..6) = sqrt(4.0);
            feature c: ScalarValues::Real = min(a,b);
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("c")
        assertEquals(2.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(2.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun minTest2EvalDown() = testSession {
        loadSysMD(
            input = """
            feature a: ScalarValues::Real(0..6) = 3.0;
            feature b: ScalarValues::Real(1..6);
            feature c: ScalarValues::Real(3.0..3.0) = min(a,b);
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("b")
        assertEquals(3.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(6.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun minTest3() = testSession {
        loadSysMD(
            input = """
            feature a: ScalarValues::Real(0..5) = 4.0;
            feature b: ScalarValues::Real(1..6) = 6.0;
            feature c: ScalarValues::Real = min(sqrt(9.0)+a,sqrt(4.0)+b);
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("c")
        assertEquals(7.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(7.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }


    @Test
    fun minTest4() = testSession {
        loadSysMD(input = """
            feature c: ScalarValues::Real = min(sqrt(9.0)+3.0,sqrt(4.0)+5.0);
            """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("c")
        assertEquals(6.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(6.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }
    @Test
    fun minTestOneValue1() = testSession {
        loadSysMD(
            input = """
            feature a: ScalarValues::Real(0.5..1);
            feature c: ScalarValues::Real = min(a);
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("c")
        assertEquals(0.5, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(0.5, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(1, result.vectorQuantity.values.size )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun maxTestOneValue1() = testSession {
        loadSysMD(
            input = """
            feature a: ScalarValues::Real(0..1.5);
            feature c: ScalarValues::Real = max(a);
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("c")
        assertEquals(1.5, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(1.5, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(1, result.vectorQuantity.values.size )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun minTestOneValueInt1() = testSession {
        loadSysMD(input = """
            feature a: ScalarValues::Integer(0..1);
            feature c: ScalarValues::Integer = min(a);
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("c")
        assertEquals(0, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(0, result.vectorQuantity.value.asIdd().max)
        assertEquals(1, result.vectorQuantity.values.size )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }
    @Test
    fun maxTestOneValueInt1() = testSession {
        loadSysMD(
            input = """
            feature a: ScalarValues::Integer(0..1);
            feature c: ScalarValues::Integer = max(a);
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("c")
        assertEquals(1, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(1, result.vectorQuantity.value.asIdd().max)
        assertEquals(1, result.vectorQuantity.values.size )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun byParts() = testSession(loadKerML = false) {
        loadSysMD(input = """
            package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; datatype Real :> ScalarValue; }    
            class c {
                feature a: ScalarValues::Real(0..10); 
            }
            class c1 :> c {
                feature a: ScalarValues::Real(0..5); 
            }

            class c2 :> c {
                feature a: ScalarValues::Real(0..2); 
            }
            
            class b {
                feature cElemem1: c1; 
                feature cElemen2: c2; 
                feature a:ScalarValues::Real = byParts(a); 
            }
            """.trimIndent(), catchExceptions = true
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("b::a")
        assertEquals(0.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(5.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }
}