package constraintnettests

import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import util.testSession
import kotlin.math.*

/**
 * Tests of the pre-defined functions
 */
class FunctionsTests {

    @Test
    fun absTestReal() = testSession("ScalarValues") {
        loadKerML("""
                feature qa: ScalarValues::Real {:>> range = "5.0 .. 5.0";}
                feature qb: ScalarValues::Real {:>> range = "1.0 .. 5.0";} 
                feature qc: ScalarValues::Real {:>> range = "0.0 .. 1.0";}
                feature qd: ScalarValues::Real {:>> range = "0.0 .. 0.0";} 
                feature qe: ScalarValues::Real {:>> range = "-1.0 .. 5.0";} 
                feature qf: ScalarValues::Real {:>> range = "-5.0 .. 5.0";}
                feature qg: ScalarValues::Real {:>> range = "-5.0 .. 1.0";}
                feature qh: ScalarValues::Real {:>> range = "-5.0 .. -1.0";}
                feature qi: ScalarValues::Real {:>> range = "-5.0 .. 0.0";}
                feature qj: ScalarValues::Real {:>> range = "-5.0 .. -5.0";} 
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
            """
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
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun absTestInteger() = testSession("ScalarValues") {
        loadKerML("""
            feature qa: ScalarValues::Integer {:>> range = "5 .. 5";}
            feature a: ScalarValues::Integer = abs(qa);
            """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(5, global.resolve<Feature>("a")!!.variable!!.idd().getRange().min)
        assertEquals(5, global.resolve<Feature>("a")!!.variable!!.idd().getRange().max)
    }


    /**
     * Test of the function toReal(x) : Boolean -> ScalarValues::Real.
     */
    @Test
    fun toRealTest() = testSession("ScalarValues") {
        loadKerML("""
                feature a: ScalarValues::Boolean = true; 
                feature b: ScalarValues::Real = toReal(a); 
                feature c: ScalarValues::Boolean = false; 
                feature d: ScalarValues::Real = toReal(c);  
                feature e: ScalarValues::Boolean;
                feature f: ScalarValues::Real = toReal(e);
        """)
        assertEquals(0, status.issues.size, status.issues.toString())
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
    fun evalUpWithPow2_real_range() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real {:>> range = "1.0 .. 5.0";}
            feature b: ScalarValues::Real = power2(a);"""
        )
        val b = global.resolveVar("b")
        assertNotNull(b)
        assertEquals(2.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(32.0, global.resolveVar("b")!!.max(), 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with pow2 in real and value */
    @Test
    fun evalUpWithPow2_real_value() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = 3.0 {:>> range = "1.0 .. 5.0";}
            feature b: ScalarValues::Real = power2(a); """
        )
        assertEquals(8.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().min, 0.00001)
        assertEquals(8.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolve<Feature>("b")!!.variable!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with pow2 and negative values*/
    @Test
    fun evalUpWithPow2_real_negative() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real {:>> range = "-2.0 .. -1.0";}
            feature b: ScalarValues::Real = power2(a);"""
        )
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0.25, global.resolveVar("b")!!.aadd().min, 0.0001)
        assertEquals(0.5, global.resolveVar("b")!!.aadd().max, 0.0001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 with zero*/
    @Test
    fun evalUpWithPow2_real_zero() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real {:>> range = "0.0 .. 0.0";}
            feature b: ScalarValues::Real = power2(a); """
        )
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("b")!!.aadd().min, 0.0001)
        assertEquals(1.0, global.resolveVar("b")!!.aadd().max, 0.0001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in int and model.builder.range*/
    @Test
    fun evalUpWithPow2_int_range() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer {:>> range = "1 .. 5";}
            feature b: ScalarValues::Integer = power2(a); """
        )
        initialize()
        propagate()
        assertEquals(2, (global.resolveVar("b")!!.idd() as IDD.Leaf).value.min)
        assertEquals(32, (global.resolveVar("b")!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with pow2 in int and model.builder.range*/
    @Test
    fun evalUpWithPow2_int_value() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = 3 {:>> range = "1 .. 5";}
            feature b: ScalarValues::Integer = power2(a); """
        )
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        propagate()
        assertEquals(8, (global.resolveVar("b")!!.idd() as IDD.Leaf).value.min)
        assertEquals(8, (global.resolveVar("b")!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with exp in real and model.builder.range */
    @Test
    fun evalUpWithExp_real_range() = testSession("ScalarValues") {
        loadKerML("feature b: ScalarValues::Real = exp([1.0 .. 5.0]);")
        propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(Math.E, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(Math.E.pow(5), global.resolveVar("b")!!.max(), 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in real and value */
    @Test
    fun evalUpWithExp_real_value() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = 3.0 {:>> range = "1.0 .. 5.0";}
            feature b: ScalarValues::Real = exp(a);"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(Math.E.pow(3), global.resolve<Feature>("b")!!.variable!!.aadd().getRange().min, 0.00001)
        assertEquals(Math.E.pow(3), global.resolve<Feature>("b")!!.variable!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolve<Feature>("b")!!.variable!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 with negative value*/
    @Test
    fun evalUpWithExp_real_negative() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real {:>> range = "-3.0 .. -1.0";}
            feature b: ScalarValues::Real = exp(a);"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(Math.E.pow(-3), global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(Math.E.pow(-1), global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 and zero */
    @Test
    fun evalUpWithExp_real_zero() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real{:>> range = "0.0 .. 0.0";}
            feature b: ScalarValues::Real = exp(a);"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with exp in int and model.builder.range */
    @Test
    fun evalUpWithExp_int_range() = testSession("ScalarValues") {
        loadKerML("feature b: ScalarValues::Integer = exp([1 .. 5]).")
        propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(floor(Math.E).toLong(), global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(5)).toLong(), global.resolveVar("b")!!.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in int and value */
    @Test
    fun evalUpWithExp_int_value() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = 3 {:>> range = "1 .. 5";}
            feature b: ScalarValues::Integer = exp(a);"""
        )
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(floor(Math.E.pow(3)).toLong(), global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(3)).toLong(), global.resolveVar("b")!!.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with ln in real and model.builder.range */
    @Test
    fun evalUpWithLog_real_range() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real {:>> range = "1.0 .. 5.0";}
            feature b: ScalarValues::Real = ln(a);"""
        )
        propagate()
        assertEquals(ln(1.0), global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(ln(5.0), global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with ln in real and value */
    @Test
    fun evalUpWithLog_real_value() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = 3.0 {:>> range = "1.0 .. 5.0";}
            feature b: ScalarValues::Real = ln(a);"""
        )
        propagate()
        assertEquals(ln(3.0), global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(ln(3.0), global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with ln in real and negative numbers => not possible */
    @Test
    fun evalUpWithLog_real_negative() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real {:>> range = "-10.0 .. -5.0";}
            feature b: ScalarValues::Real = ln(a);
        """)
        val b = global.resolveVar("b")
        propagate()
        assertTrue(b!!.vectorQuantity.value.asAadd().isEmpty())
        assertEquals(1, status.issues.size)
    }

    /** ConstNet shall compute bottom-up with ln in int and model.builder.range */
    @Test
    fun evalUpWithLog_int_range() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer {:>> range = "1 .. 5";}
            feature b: ScalarValues::Integer = ln(a);""")
        propagate()
        assertEquals(floor(ln(1.0)).toLong(), global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(ceil(ln(5.0)).toLong(), global.resolveVar("b")!!.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with ln in int and value */
    @Test
    fun evalUpWithLog_int_value() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = 3 {:>> range = "1 .. 5";}
            feature b: ScalarValues::Integer = ln(a);"""
        )
        propagate()
        assertEquals(floor(ln(3.0)).toLong(), global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(ceil(ln(3.0)).toLong(), global.resolveVar("b")!!.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with sqrt in real and model.builder.range */
    @Test
    fun evalUpWithSqrt_real_range() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real {:>> range = "2.0 .. 9.0";}
            feature b: ScalarValues::Real = sqrt(a);"""

        )
        propagate()
        assertEquals(sqrt(2.0), global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        //AADD returns bigger result for upper border
        assert(sqrt(9.0) <= global.resolveVar("b")!!.aadd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with sqrt in real and value */
    @Test
    fun propagateWithSqrt_real_value() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = 3.0 {:>> range = "2.0 .. 5.0";}
            feature b: ScalarValues::Real = sqrt(a);""")
        propagate()
        assertEquals(sqrt(3.0), global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(sqrt(3.0), global.resolveVar("b")!!.max(), 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with sqrt and negative value.This should lead to an error*/
    @Test
    fun propagateWithSqrt_real_negative() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real {:>> range = "-10.0 .. -5.0";}
            feature b: ScalarValues::Real = sqrt( a );
            """)
        propagate()
        assertEquals(1, status.issues.size, "Issues: ${status.issues}")
        assertTrue("dependency of b is not satisfiable in element b" in status.issues.first().message)
    }

    /** ConstNet shall compute bottom-up with sqrt in int and model.builder.range */
    @Test
    fun evalUpWithSqrt_int_range() = testSession("ScalarValues") {
            loadKerML("""
            feature a: ScalarValues::Integer {:>> range = "2 .. 9";}
            feature b: ScalarValues::Integer = sqrt(a);""")
            propagate()
            assertEquals(floor(sqrt(2.0)).toLong(), global.resolve<Feature>("b")!!.variable!!.idd().getRange().min)
            assertEquals(ceil(sqrt(9.0)).toLong(), global.resolve<Feature>("b")!!.variable!!.idd().getRange().max)
            assertEquals("1", global.resolve<Feature>("b")!!.variable!!.vectorQuantity.unit.toString())
            assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with sqrt in int **/
    @Test
    fun evalUpWithSqrt_int_value() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = 3 {:>> range = "2 .. 9";}
            feature b: ScalarValues::Integer = sqrt(a); """
        )
        propagate()
        assertEquals(floor(sqrt(3.0)).toLong(), global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(ceil(sqrt(3.0)).toLong(), global.resolveVar("b")!!.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with sqr in real and model.builder.range */
    @Test
    fun evalUpWithSqr_real_range() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real {:>> range = "2.0 .. 9.0";} 
            feature b: ScalarValues::Real = sqr(a); """
        )
        propagate()
        assertEquals(4.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(81.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with sqr in real and value */
    @Test
    fun evalUpWithSqr_real_value() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = 3.0 {:>> range = "1.0 .. 5.0";} 
            feature b: ScalarValues::Real = sqr(a);"""
        )
        propagate()
        assertEquals(9.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().min, 0.00001)
        assertEquals(9.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().max, 0.00001)
        assertEquals("1", global.resolve<Feature>("b")!!.variable!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with sqr in int and model.builder.range */
    @Test
    fun evalUpWithSqr_int_range() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer {:>> range = "2 .. 9";} 
            feature b: ScalarValues::Integer = sqr(a);"""
        )
        propagate()
        assertEquals(4, (global.resolve<Feature>("b")!!.variable!!.idd() as IDD.Leaf).value.min)
        assertEquals(81, (global.resolve<Feature>("b")!!.variable!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", global.resolve<Feature>("b")!!.variable!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with sqr in int and value */
    @Test
    fun evalUpWithSqr_int_value() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = 3 {:>> range = "2 .. 9";}  
            feature b: ScalarValues::Integer = sqr(a); """
        )
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(9, (global.resolve<Feature>("b")!!.variable!!.idd() as IDD.Leaf).value.min)
        assertEquals(9, (global.resolve<Feature>("b")!!.variable!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", global.resolve<Feature>("b")!!.variable!!.vectorQuantity.unit.toString())
    }

    @Test
    fun evalUpWithPowB_real_value() = testSession("ScalarValues") {
        loadKerML(
           """
            feature a: ScalarValues::Real {:>> range = "3.0 .. 3.0";} 
            feature b: ScalarValues::Real {:>> range = "4.0 .. 4.0";} 
            feature c: ScalarValues::Real = power(a, b); """
        )
        val c = global.resolveVar("c") !!
        assertEquals(81.0, c.aadd().getRange().min, 0.000001)
        assertEquals(81.0, c.aadd().getRange().max, 0.000001)
        assertEquals("1", c.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun evalUpWithPowB_real_range() = testSession("ScalarValues") {
        loadKerML(
            """
            feature a: ScalarValues::Real {:>> range = "1.5 .. 3.5";}
            feature b: ScalarValues::Real {:>> range = "2.5 .. 4.5";}
            feature c: ScalarValues::Real = power(a, b); """
        )
        propagate()
        assertEquals(1.5.pow(2.5), global.resolveVar("c")!!.min(), 0.000001)
        assertEquals(3.5.pow(4.5), global.resolveVar("c")!!.max(), 0.000001)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun evalUpWithPowB_int_value() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = 3;
            feature b: ScalarValues::Integer = 3;
            feature c: ScalarValues::Integer = power(a, b);"""
        )
        propagate()
        assertEquals(27, global.resolveVar("c")!!.idd().getRange().min)
        assertEquals(27, global.resolveVar("c")!!.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun evalUpWithPow_int_range() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = oneOf(2 .. 3);
            feature b: ScalarValues::Integer = oneOf(3 .. 4);
            feature c: ScalarValues::Integer = power(a, b); """
        )
        val c = global.resolveVar("c") !!
        assertEquals(8, c.idd().getRange().min)
        assertEquals(81, c.idd().getRange().max)
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun evalUpPowerNegativeBase() = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Real = 1.0;
            feature a: ScalarValues::Real = pow(-5.0,1.0);"""
        )
        propagate()
        assertEquals(-5.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun evalUpPowerNegativeBase2() = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Real = 1.0;
            feature a: ScalarValues::Real = pow(-1.0,2.0);""")
        propagate()
        assertEquals(1.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun evalUpPowerSpecialCase() = testSession("ScalarValues") {
        loadKerML(""" 
            feature a: ScalarValues::Real = oneOf(0.1..2.0); 
            feature b: ScalarValues::Real = pow(a, [1.0..2.0]);""")
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        assertEquals(0.1, a.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.0, a.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.01, b.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(4.0, b.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun sumEvalUp1() = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Real; 
            feature a: ScalarValues::Real = oneOf(1.0..3.0);
            feature b: ScalarValues::Real = oneOf(3.0..5.0);
            feature sum: ScalarValues::Real = sum_i( a, b, i );
            """)
        propagate()
        assertEquals(15.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        assertEquals(3.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun sumEvalUp2() = testSession("ScalarValues") {
        loadKerML(
            """
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real = oneOf(0.0..0.0);
            feature b: ScalarValues::Real = oneOf(1.0..2.0);
            feature s: ScalarValues::Real = oneOf(4.0..5.0);
            feature t: ScalarValues::Real = oneOf(2.0..2.0);
            feature sum: ScalarValues::Real = sum_i( a, b, s-t*i );""")
        propagate()
        assertEquals(9.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        assertEquals(6.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    @Test
    fun sumEvalDown() = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real = oneOf(1.0 .. 3.0);
            feature b: ScalarValues::Real = oneOf(1.0 .. 5.0);
            feature sum: ScalarValues::Real = sum_i( a, b, i ){:>> range = "3.0..10.0";}""")
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        assertEquals(3.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(10.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        assertEquals(1.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
        assertEquals(3.0, global.resolveVar("a")!!.aadd().getRange().max, 0.00001)
        assertEquals(1.5, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        // with int the borders would be 3 and 4, but rounding makes the intervals bigger
        assertEquals(4.5, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun sumEvalDown2() = testSession("ScalarValues") {
        loadKerML(
            """
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real {:>> range = "0..6";} 
            feature b: ScalarValues::Real {:>> range = "3..5";} 
            feature sum: ScalarValues::Real = sum_i( a, b, i ) {:>> range = "3.0..14.0";}
            """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(3.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(14.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        assertEquals(3.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals(0.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.5, global.resolveVar("a")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    //Calculations with evalDown do not work
    @Disabled
    @Test
    fun sumEvalDownWithMultiplication() = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real;
            feature s: ScalarValues::Real = 10.0;
            feature b: ScalarValues::Real = oneOf(3.0 .. 5.0);
            feature sum: ScalarValues::Real = sum_i( a, b, s*i ) {:>> range = "30.0..140.0";}"""
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        assertEquals(30.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(140.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        assertEquals(3.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolve<Feature>("b")!!.variable!!.aadd().getRange().max, 0.00001)
        assertEquals(1.5, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
        assertEquals(3.5, global.resolveVar("a")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegative() = testSession("ScalarValues") {
        loadKerML(
            """
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real {:>> range = "1.0..1.0";}
            feature b: ScalarValues::Real {:>> range = "5.0..5.0";}
            feature sum: ScalarValues::Real = sum_i( a, b, pow(-1.0,i)*i );""")
        propagate()
        assertEquals(-3.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(-3.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegativeTest2() = testSession("ScalarValues") {
        loadKerML(
            """
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real {:>> range = "-3.0..0.0";}
            feature b: ScalarValues::Real {:>> range = "0.0..3.0";}
            feature sum: ScalarValues::Real = sum_i( a, b, pow(-1.0,i)*(1.0-sqr(i)) );"""
        )
        propagate()
        assertEquals(-5.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(11.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegativeTest3() = testSession("ScalarValues") {
        loadKerML(
            """
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real {:>> range = "-3.0..0.0";}
            feature b: ScalarValues::Real {:>> range = "0.0..3.0";}
            feature sum: ScalarValues::Real = sum_i( a, b, -pow(-1.0,i)*(1.0-sqr(i)) );"""
        )
        propagate()
        assertEquals(-11.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegativeTest4() = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real {:>> range = "-3.0..0.0";}
            feature b: ScalarValues::Real {:>> range = "0.0..3.0";}
            feature sum: ScalarValues::Real = sum_i( a, b, -pow(-1.0,i));""")
        propagate()
        assertEquals(-1.0, global.resolveVar("sum")!!.aadd().getRange().min, 0.00001)
        assertEquals(1.0, global.resolveVar("sum")!!.aadd().getRange().max, 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    @Test
    fun sumEvalDownInt() = testSession("ScalarValues") {
        loadKerML("""
             feature i: ScalarValues::Integer;
             feature a: ScalarValues::Integer = oneOf(1..3);
             feature b: ScalarValues::Integer = oneOf(3..4);
             feature sum: ScalarValues::Integer = sum_i( a, b, i ) {:>> range = "3..10";}""")
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        assertEquals(3, global.resolveVar("sum")!!.idd().getRange().min)
        assertEquals(10, global.resolveVar("sum")!!.idd().getRange().max)
        assertEquals(1, global.resolveVar("a")!!.idd().getRange().min)
        assertEquals(3, global.resolveVar("a")!!.idd().getRange().max)
        assertEquals(3, global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(4, global.resolveVar("b")!!.idd().getRange().max)
    }

    @Disabled //Does not work for ScalarValues::Integer
    @Test
    fun sumEvalDownInt2() = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Integer;
            feature a: ScalarValues::Integer;
            feature b: ScalarValues::Integer = oneOf(3..5);
            feature sum: ScalarValues::Integer = sum_i( a, b, i ) {:>> range = "3..14";}""")
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        assertEquals(9, global.resolveVar("sum")!!.idd().getRange().min)
        assertEquals(10, global.resolveVar("sum")!!.idd().getRange().max)
        assertEquals(2, global.resolveVar("a")!!.idd().getRange().min)
        assertEquals(3, global.resolveVar("a")!!.idd().getRange().max)
        assertEquals(3, global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(5, global.resolveVar("b")!!.idd().getRange().max)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun maxTest1() = testSession("ScalarValues") {
        loadKerML(
            input = """
            feature a: ScalarValues::Real {:>> range = "0..1";}
            feature b: ScalarValues::Real {:>> range = "1..2";}
            feature c: ScalarValues::Real = max(a,b);
            """, catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("c")
        assertEquals(1.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(2.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun maxTest1EvalDown() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Real {:>> range = "1..7";}
            feature b: ScalarValues::Real = max(7.0,2.0+a) {:>> range = "8.0..8.0";}
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("a")
        assertEquals(6.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(6.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun maxTest1bEvalDown() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Real = oneOf(1.0 .. 7.0);
            feature b: ScalarValues::Real = max(8.0, 2.0+a) {:>> range = "8.0..8.0";}
            """.trimIndent(), catchExceptions = true)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("a")
        assertEquals(1.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(6.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun maxTest2() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Real = sqrt(9.0) {:>> range = "0..5";}
            feature b: ScalarValues::Real = sqrt(4.0) {:>> range = "1..6";}
            feature c: ScalarValues::Real = max(a,b);
            """)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("c")
        assertEquals(3.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(3.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun maxTest2EvalDown() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Real = sqrt(9.0) {:>> range = "0..6";}
            feature b: ScalarValues::Real {:>> range = "1..6";}
            feature c: ScalarValues::Real = max(a,b) {:>> range = "4.0..4.0";}
            """.trimIndent())
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("b")
        assertEquals(4.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(4.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun maxTest3() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Real = 4.0 {:>> range = "0..5";}
            feature b: ScalarValues::Real = 6.0 {:>> range = "1..6";}
            feature c: ScalarValues::Real = max(sqrt(9.0)+a,sqrt(4.0)+b);
            """)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("c")
        assertEquals(8.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(8.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }


    @Test
    fun maxTest4() = testSession("ScalarValues") {
        loadKerML(input = """
                  feature c: ScalarValues::Real = max(sqrt(9.0)+3.0,sqrt(4.0)+5.0);
            """)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("c")
        assertEquals(7.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(7.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun maxTestMultipleParams1() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real {:>> range = "0..1";}
            feature b: ScalarValues::Real {:>> range = "1..2";}
            feature c: ScalarValues::Real {:>> range = "3..4";}
            feature d: ScalarValues::Real {:>> range = "4..5";}
            feature e: ScalarValues::Real = max(a, b, c, d);
        """)
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("e")
        assertEquals(4.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(5.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun maxTestMultipleParamsInt1() = testSession("ScalarValues") {
        loadKerML(
            input = """
            feature a: ScalarValues::Integer {:>> range = "0..1";}
            feature b: ScalarValues::Integer {:>> range = "1..2";}
            feature c: ScalarValues::Integer {:>> range = "3..4";}
            feature d: ScalarValues::Integer {:>> range = "4..5";}
            feature e: ScalarValues::Integer = max(a,b,c,d);
            """, catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("e")
        assertEquals(4, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(5, result.vectorQuantity.value.asIdd().max)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Disabled
    @Test
    fun maxTestMultipleParams2() = testSession("ScalarValues") {
        loadKerML(input ="""
            feature a: ScalarValues::Real {:>> range = "0..1";}
            feature b: ScalarValues::Real {:>> range = "1..2";}
            feature c: ScalarValues::Real {:>> range = "2..3";}
            feature d: ScalarValues::Real {:>> range = "3..7";}
            feature e: ScalarValues::Real = max(a,b,c,d) {:>> range = "4..4";}
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("d")
        assertEquals(4.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(4.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun maxTestMultipleParams2Integer() = testSession("ScalarValues") {
        loadKerML(
            input ="""
            feature a: ScalarValues::Integer {:>> range = "0..1";}
            feature b: ScalarValues::Integer {:>> range = "1..2";}
            feature c: ScalarValues::Integer {:>> range = "2..3";}
            feature d: ScalarValues::Integer {:>> range = "3..7";}
            feature e: ScalarValues::Integer= max(a,b,c,d) {:>> range = "4..4";}
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("d")
        assertEquals(4, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result.vectorQuantity.value.asIdd().max)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun maxTestMultipleParams3() = testSession("ScalarValues") {
        loadKerML(
            input = """
            feature a: ScalarValues::Real {:>> range = "0..7";}
            feature b: ScalarValues::Real {:>> range = "1..6";}
            feature c: ScalarValues::Real {:>> range = "2..5";}
            feature d: ScalarValues::Real {:>> range = "3..4";}
            feature e: ScalarValues::Real = max(a,b,c,d) {:>> range = "3..4";}
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("a")
        assertEquals(0.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(4.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        val result1 = global.resolveVar("b")
        assertEquals(1.0, result1!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(4.0, result1.vectorQuantity.getMaxAsDouble(), 0.000001)
        val result2 = global.resolveVar("c")
        assertEquals(2.0, result2!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(4.0, result2.vectorQuantity.getMaxAsDouble(), 0.000001)
        val result3 = global.resolveVar("d")
        assertEquals(3.0, result3!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(4.0, result3.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun maxTestMultipleParams3Integer() = testSession("ScalarValues") {
        loadKerML(
            input = """
            feature a: ScalarValues::Integer {:>> range = "0..7";}
            feature b: ScalarValues::Integer {:>> range = "1..6";}
            feature c: ScalarValues::Integer {:>> range = "2..5";}
            feature d: ScalarValues::Integer {:>> range = "3..4";}
            feature e: ScalarValues::Integer = max(a,b,c,d) {:>> range = "3..4";}
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("a")
        assertEquals(0, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result.vectorQuantity.value.asIdd().max)
        val result1 = global.resolveVar("b")
        assertEquals(1, result1!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result1.vectorQuantity.value.asIdd().max)
        val result2 = global.resolveVar("c")
        assertEquals(2, result2!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result2.vectorQuantity.value.asIdd().max)
        val result3 = global.resolveVar("d")
        assertEquals(3, result3!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result3.vectorQuantity.value.asIdd().max)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun minTest1() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Real {:>> range = "0..1";}
            feature b: ScalarValues::Real {:>> range = "1..2";}
            feature c: ScalarValues::Real = min(a,b);
            """, catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("c")
        assertEquals(0.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(1.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun minTest1EvalDown() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Real {:>> range = "1..8";}
            feature b: ScalarValues::Real = min(8.0,a) {:>> range = "6.0..6.0";}
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("a")
        assertEquals(6.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(6.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun minTest2() = testSession("ScalarValues") {
        loadKerML(
            input = """
            feature a: ScalarValues::Real = sqrt(9.0) {:>> range = "0..5";}
            feature b: ScalarValues::Real = sqrt(4.0) {:>> range = "1..6";}
            feature c: ScalarValues::Real = min(a,b);
            """, catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("c")
        assertEquals(2.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(2.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun minTest2EvalDown() = testSession("ScalarValues") {
        loadKerML(
            input = """
            feature a: ScalarValues::Real = 3.0 {:>> range = "0..6";}
            feature b: ScalarValues::Real {:>> range = "1..6";}
            feature c: ScalarValues::Real = min(a,b) {:>> range = "3.0..3.0";}
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("b")
        assertEquals(3.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(6.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun minTest3() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Real = 4.0 {:>> range = "0..5";}
            feature b: ScalarValues::Real = 6.0 {:>> range = "1..6";}
            feature c: ScalarValues::Real = min(sqrt(9.0)+a,sqrt(4.0)+b);
            """, catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("c")
        assertEquals(7.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(7.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }


    @Test
    fun minTest4() = testSession("ScalarValues") {
        loadKerML(input = """
            feature c: ScalarValues::Real = min(sqrt(9.0)+3.0,sqrt(4.0)+5.0);
            """)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("c")
        assertEquals(6.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(6.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun minTestMultipleParams1() = testSession("ScalarValues") {
        loadKerML(
            input = """
            feature a: ScalarValues::Real {:>> range = "0..1";}
            feature b: ScalarValues::Real {:>> range = "1..2";}
            feature c: ScalarValues::Real {:>> range = "3..4";}
            feature d: ScalarValues::Real {:>> range = "4..5";}
            feature e: ScalarValues::Real = min(a,b,c,d);
            """, catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("e")
        assertEquals(0.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(1.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun minTestMultipleParamsInt1() = testSession("ScalarValues") {
        loadKerML(
            input = """
            feature a: ScalarValues::Integer {:>> range = "0..1";}
            feature b: ScalarValues::Integer {:>> range = "1..2";}
            feature c: ScalarValues::Integer {:>> range = "3..4";}
            feature d: ScalarValues::Integer {:>> range = "4..5";}
            feature e: ScalarValues::Integer = min(a,b,c,d);
            """, catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("e")
        assertEquals(0, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(1, result.vectorQuantity.value.asIdd().max)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun minTestMultipleParams2() = testSession("ScalarValues") {
        loadKerML(
            input = """
            feature a: ScalarValues::Real {:>> range = "-3..1";}
            feature b: ScalarValues::Real {:>> range = "1..2";}
            feature c: ScalarValues::Real {:>> range = "2..3";}
            feature d: ScalarValues::Real {:>> range = "3..7";}
            feature e: ScalarValues::Real = min(a,b,c,d) {:>> range = "0..0";}
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("a")
        assertEquals(0.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(0.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun minTestMultipleParams2Integer() = testSession("ScalarValues") {
        loadKerML(
            input ="""
            feature a: ScalarValues::Integer {:>> range = "-3..1";}
            feature b: ScalarValues::Integer {:>> range = "1..2";}
            feature c: ScalarValues::Integer {:>> range = "2..3";}
            feature d: ScalarValues::Integer {:>> range = "3..7";}
            feature e: ScalarValues::Integer = min(a,b,c,d) {:>> range = "0..0";}
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("a")
        assertEquals(0, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(0, result.vectorQuantity.value.asIdd().max)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun minTestMultipleParams3() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real {:>> range = "0..7";}
            feature b: ScalarValues::Real {:>> range = "1..6";}
            feature c: ScalarValues::Real {:>> range = "2..5";}
            feature d: ScalarValues::Real {:>> range = "3..4";}
            feature e: ScalarValues::Real = min(a,b,c,d) {:>> range = "4..5";}
        """)
        val e = global.resolveVar("e")!!
        assertEquals(4.0, e.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(4.0, e.vectorQuantity.getMinAsDouble(), 0.000001)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("a")
        assertEquals(4.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(7.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        val result1 = global.resolveVar("b")
        assertEquals(4.0, result1!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(6.0, result1.vectorQuantity.getMaxAsDouble(), 0.000001)
        val result2 = global.resolveVar("c")
        assertEquals(4.0, result2!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(5.0, result2.vectorQuantity.getMaxAsDouble(), 0.000001)
        val result3 = global.resolveVar("d")
        assertEquals(4.0, result3!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(4.0, result3.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }


    @Test @Disabled
    fun minTestMultipleParams3Integer() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Integer {:>> range = "0..7";}
            feature b: ScalarValues::Integer {:>> range = "1..6";}
            feature c: ScalarValues::Integer {:>> range = "2..5";}
            feature d: ScalarValues::Integer {:>> range = "3..4";}
            feature e: ScalarValues::Integer = min(a,b,c,d) {:>> range = "4..5";}
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("a")
        assertEquals(4, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(7, result.vectorQuantity.value.asIdd().max)
        val result1 = global.resolveVar("b")
        assertEquals(4, result1!!.vectorQuantity.value.asIdd().min)
        assertEquals(6, result1.vectorQuantity.value.asIdd().max)
        val result2 = global.resolveVar("c")
        assertEquals(4, result2!!.vectorQuantity.value.asIdd().min)
        assertEquals(5, result2.vectorQuantity.value.asIdd().max)
        val result3 = global.resolveVar("d")
        assertEquals(4, result3!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result3.vectorQuantity.value.asIdd().max)
        assertEquals(0, status.issues.size, status.issues.toString())
    }



    @Test
    fun minTestOneValue1() = testSession("ScalarValues") {
        loadKerML(input ="""
            feature a: ScalarValues::Real {:>> range = "0.5..1";}
            feature c: ScalarValues::Real = min(a);
            """, catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("c")
        assertEquals(0.5, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(0.5, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(1, result.vectorQuantity.values.size )
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun maxTestOneValue1() = testSession("ScalarValues") {
        loadKerML(
            input ="""
            feature a: ScalarValues::Real {:>> range = "0..1.5";}
            feature c: ScalarValues::Real = max(a);
            """, catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("c")
        assertEquals(1.5, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(1.5, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(1, result.vectorQuantity.values.size )
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun minTestOneValueInt1() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Integer {:>> range = "0..1";}
            feature c: ScalarValues::Integer = min(a);
            """, catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("c")
        assertEquals(0, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(0, result.vectorQuantity.value.asIdd().max)
        assertEquals(1, result.vectorQuantity.values.size )
        assertEquals(0, status.issues.size, status.issues.toString())
    }
    @Test
    fun maxTestOneValueInt1() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Integer {:>> range = "0..1";}
            feature c: ScalarValues::Integer = max(a);
            """, catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("c")
        assertEquals(1, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(1, result.vectorQuantity.value.asIdd().max)
        assertEquals(1, result.vectorQuantity.values.size )
        assertEquals(0, status.issues.size, status.issues.toString())
    }


    @Test
    fun byParts() = testSession("Occurrences") {
        loadKerML(input =
        """
            class c {
                feature a: ScalarValues::Real {:>> range = "0..10";}
            }
            class c1 :> c {
                feature a: ScalarValues::Real {:>> range = "0..5";}
            }

            class c2 :> c {
                feature a: ScalarValues::Real {:>> range = "0..2";}
            }
            
            class b {
                feature cElemem1: c1; 
                feature cElemen2: c2; 
                feature a:ScalarValues::Real = byParts(a); 
            }
            """)
        assertEquals(0, status.issues.size, status.issues.toString())
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("b::a")
        assertEquals(0.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(5.0, result.vectorQuantity.getMaxAsDouble(), 0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun assertTestStepInterpolationEvalDOwn() = testSession("Calculations", "SI", "Parts") {
        loadSysMLv2("""
        attribute Avail : SI::Quantity {:>> unit = "%"; :>> range = "0.0..100.0";} 
        attribute level: ScalarValues::Integer = stepInterpolation(Avail, 0.0, 2, 0.9, 3, 0.95, 4, 1.0, 5) { :>> range = "4..4"; }
        """
        )
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test2 = global.resolveVar("Avail")
        assertEquals(0.95, test2!!.vectorQuantity.value.asAadd().min,0.0001)
        assertEquals(1.0, test2.vectorQuantity.value.asAadd().max,0.0001)
    }
    @Test
    fun assertTestStepInterpolationEvalDOwn2() = testSession("Calculations", "SI", "Parts") {
        loadSysMLv2(
            """
        attribute reliability: SI::Quantity {:>> unit = "%"; :>> range = "0.0..100.0";}
        attribute ASIlFromReliability: ScalarValues::Integer = stepInterpolation(reliability, 0.0, 1, 0.99, 2, 0.995, 3, 0.999, 4) {:>> range = "3..4";} 
        """
        )
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test2 = global.resolveVar("reliability")
        assertEquals(0.995, test2!!.vectorQuantity.value.asAadd().min,0.000001)
        assertEquals(1.0, test2.vectorQuantity.value.asAadd().max,0.000001)
        // val testr = global.resolveVar("Controller1::ASIlFromReliability")
        // assertEquals(1, testr!!.vectorQuantity.value.asIdd().min)
    }

    @Test
    fun assertTestStepInterpolationEvalDOwnInteger() = testSession("Calculations", "SI", "Parts") {
        loadSysMLv2("""
        attribute Avail : ScalarValues::Integer {:>> range = "0..100";} 
        attribute level: ScalarValues::Integer = stepInterpolation(Avail, 0, 2, 90, 3, 95, 4, 100, 5) { :>> range = "5..5"; }
        """
        )
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test2 = global.resolveVar("Avail")
        assertEquals(100, test2!!.vectorQuantity.value.asIdd().min)
        assertEquals(100, test2.vectorQuantity.value.asIdd().max)
    }
    @Test
    fun evalDownUDF() = testSession("Calculations", "SI", "Parts") {
        loadSysMLv2("""
        calc def calcASIL {
            in attribute severity:        ScalarValues::Integer { :>> range = "0..3";}
            in attribute exposure:        ScalarValues::Integer { :>> range = "0..4";}
            in attribute controllability: ScalarValues::Integer { :>> range = "0..3";}
            attribute    sum:             ScalarValues::Integer = severity + exposure + controllability;
            attribute    sumAdapted:      ScalarValues::Integer = if controllability == 0 ? 0 else if severity == 0 ? 0 else sum; // special case for S0 and C0 the ASIL is always QM (0)
            return       result:          ScalarValues::Integer = max(sum-6,0);   
        }  
        
        attribute severity: ScalarValues::Integer = 3;     
        attribute exposure: ScalarValues::Integer = 4;
        attribute controllability: ScalarValues::Integer {:>> range = "0..3";}
        attribute ASILCalculated: ScalarValues::Integer = calcASIL(severity,exposure,controllability) {:>> range = "4..4";}           
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test2 = global.resolveVar("controllability")
        assertEquals(3, test2!!.vectorQuantity.value.asIdd().min)
    }

    @Test
    fun evalDownUDF2() = testSession("Calculations", "SI") {
        loadSysMLv2("""
        calc def calcASIL {
            in attribute severity:        ScalarValues::Integer { :>> range = "0..3";}
            in attribute exposure:        ScalarValues::Integer { :>> range = "0..4";}
            in attribute controllability: ScalarValues::Integer { :>> range = "0..3";}
            attribute    sum:             ScalarValues::Integer = severity + exposure + controllability;
            attribute    sumAdapted:      ScalarValues::Integer = if controllability == 0 ? 0 else if severity == 0 ? 0 else sum; // special case for S0 and C0 the ASIL is always QM (0)
            return       result:          ScalarValues::Integer = max(sum-6,0);   
        }  
        attribute severity: ScalarValues::Integer {:>> range = "2..2";}       
        attribute exposure: ScalarValues::Integer {:>> range = "3..3";}
        attribute controllability: ScalarValues::Integer {:>> range = "0..3";}
        attribute ASILCalculated: ScalarValues::Integer = calcASIL(severity,exposure,controllability) {:>> range = "0..0";}
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test2 = global.resolveVar("controllability")
        assertEquals(0, test2!!.vectorQuantity.value.asIdd().min)
        assertEquals(1, test2.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun evalUpAndDownUDFInt() = testSession("Calculations", "SI", "Parts") {
        loadSysMLv2(""" 
            attribute a: ScalarValues::Integer {:>> range = "0..4";}
            attribute ASILFromAvailability: ScalarValues::Integer = a {:>> range = "0..4";}
            assert ASIL {ASILFromAvailability == 4}   
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test2 = global.resolveVar("ASILFromAvailability")
        assertEquals(4, test2!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, test2.vectorQuantity.value.asIdd().max)
        //assertEquals(0.999, test2!!.vectorQuantity.value.asAadd().min)
        //assertEquals(1.0, test2.vectorQuantity.value.asAadd().max)
    }
    @Test
    fun evalUpAndDownUDFReal() = testSession("Calculations", "SI", "Parts") {
        loadSysMLv2(""" 
            attribute a: ScalarValues::Real {:>> range = "0.0..4.0";}
            attribute ASILFromAvailability: ScalarValues::Real = a {:>> range = "0.0..4.0";}
            assert ASIL {ASILFromAvailability == 4.0}   
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test2 = global.resolveVar("ASILFromAvailability")
        assertEquals(4.0, test2!!.vectorQuantity.value.asAadd().min)
        assertEquals(4.0, test2.vectorQuantity.value.asAadd().max)
        //assertEquals(0.999, test2!!.vectorQuantity.value.asAadd().min)
        //assertEquals(1.0, test2.vectorQuantity.value.asAadd().max)
    }

    @Test
    fun evalDownUDF3() = testSession("Calculations", "SI", "Parts") {
        loadSysMLv2(""" 
            calc def ASIL_from_Avail {
                in attribute Avail: SI::Quantity  { :>> unit = "%";} 
                return level: ScalarValues::Integer =  stepInterpolation(Avail, 0.0, 1, 0.99, 2, 0.995, 3, 0.999, 4);
            }  
                
            calc def ASIL_from_Reliab {
                in attribute Reliab: SI::Quantity { :>> unit = "%";} 
                return level: ScalarValues::Integer = stepInterpolation(Reliab, 0.0, 1, 0.99, 2, 0.995, 3, 0.999, 4);
            }
            attribute availability: SI::Quantity {:>> unit = "%"; :>> range = "90.0..100.0";}
            attribute reliability: SI::Quantity {:>> unit = "%"; :>> range = "99.9..100.0";}
            attribute ASILFromAvailability: ScalarValues::Integer = ASIL_from_Avail(availability) {:>> range = "0..4";}
            attribute ASIlFromReliability: ScalarValues::Integer = ASIL_from_Reliab(reliability) {:>> range = "0..4";}
            assert ASIL {ASIlFromReliability == ASILFromAvailability}  
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test2 = global.resolveVar("ASILFromAvailability")
        assertEquals(4, test2!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, test2.vectorQuantity.value.asIdd().max)
        //assertEquals(0.999, test2!!.vectorQuantity.value.asAadd().min)
        //assertEquals(1.0, test2.vectorQuantity.value.asAadd().max)
    }
}