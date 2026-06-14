package constraintnettests

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.IDD
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.math.*
import kotlin.test.*

/**
 * Tests of the pre-defined functions
 */
class FunctionsTests {

    @Test
    fun absTestReal() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::RealInRange {:>> range = "5.0 .. 5.0";}
            feature qb: Ranges::RealInRange {:>> range = "1.0 .. 5.0";} 
            feature qc: Ranges::RealInRange {:>> range = "0.0 .. 1.0";}
            feature qd: Ranges::RealInRange {:>> range = "0.0 .. 0.0";} 
            feature qe: Ranges::RealInRange {:>> range = "-1.0 .. 5.0";} 
            feature qf: Ranges::RealInRange {:>> range = "-5.0 .. 5.0";}
            feature qg: Ranges::RealInRange {:>> range = "-5.0 .. 1.0";}
            feature qh: Ranges::RealInRange {:>> range = "-5.0 .. -1.0";}
            feature qi: Ranges::RealInRange {:>> range = "-5.0 .. 0.0";}
            feature qj: Ranges::RealInRange {:>> range = "-5.0 .. -5.0";} 
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
        """)
        solver.propagate()
        assertEquals(5.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("a")!!.max(), 0.00001)
        assertEquals(1.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals(0.0, solver.getVariable("c")!!.min(), 0.00001)
        assertEquals(1.0, solver.getVariable("c")!!.max(), 0.00001)
        assertEquals(0.0, solver.getVariable("d")!!.min(), 0.00001)
        assertEquals(0.0, solver.getVariable("d")!!.max(), 0.00001)
        assertEquals(0.0, solver.getVariable("e")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("e")!!.max(), 0.00001)
        assertEquals(0.0, solver.getVariable("f")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("f")!!.max(), 0.00001)
        assertEquals(0.0, solver.getVariable("g")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("g")!!.max(), 0.00001)
        assertEquals(1.0, solver.getVariable("h")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("h")!!.max(), 0.00001)
        assertEquals(0.0, solver.getVariable("i")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("i")!!.max(), 0.00001)
        assertEquals(5.0, solver.getVariable("j")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("j")!!.max(), 0.00001)
        assertNoIssues()
    }

    @Test
    fun absTestInteger() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = "5 .. 5";}
            feature a: ScalarValues::Integer = abs(qa);
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(5L, solver.getVariable("a")!!.min())
        assertEquals(5L, solver.getVariable("a")!!.max())
    }

    @Test
    fun absTestEvalDown() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::RealInRange {:>> range = "2.0..8.0";}
            feature b: Ranges::RealInRange = abs(a) {:>> range = "6.0..6.0";}
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(2.0, result!!.min(), 0.000001)
        assertEquals(6.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun absTestIntegerNegative() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = "-3 .. -3";}
            feature a: ScalarValues::Integer = abs(qa);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(3L, solver.getVariable("a")!!.min())
        assertEquals(3L, solver.getVariable("a")!!.max())
    }

    @Test
    fun absTestInteger_negative_range() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = "-5 .. -3";}
            feature a: ScalarValues::Integer = abs(qa);
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(3, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(5, solver.getVariable("a")!!.idd().getRange().max)
    }
    @Test
    fun ceilTest_real() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::RealInRange {:>> range = "3.1 .. 3.1";}
            feature a: ScalarValues::Real = ceil(qa);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(4.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(4.0, solver.getVariable("a")!!.max(), 0.00001)
    }
    @Test
    fun ceilTest_real_range() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::RealInRange {:>> range = "3.1 .. 5.5";}
            feature a: ScalarValues::Real = ceil(qa);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(4.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(6.0, solver.getVariable("a")!!.max(), 0.00001)
    }

    @Test
    fun ceilTest_integer() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = "3 .. 3";}
            feature a: ScalarValues::Integer = ceil(qa);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(3, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(3, solver.getVariable("a")!!.idd().getRange().max)
    }

    @Test
    fun ceilTest_integer_negative() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = "-5 .. -3";}
            feature a: ScalarValues::Integer = ceil(qa);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(-4L, solver.getVariable("a")!!.min())
        assertEquals(-3L, solver.getVariable("a")!!.max())
    }

    @Test
    fun ceilTest_integer_range() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = "3 .. 5";}
            feature a: ScalarValues::Integer = ceil(qa);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(4, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(5, solver.getVariable("a")!!.idd().getRange().max)
    }

    @Test
    fun ceilTestEvalDown() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = " 5 ..  10";}
            feature a:  Ranges::IntegerInRange = ceil(qa) 
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(6, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(10, solver.getVariable("a")!!.idd().getRange().max)
    }

    @Test
    fun ceilTest_real_negative() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::RealInRange {:>> range = "-7.3 .. -4.5";}
            feature a: ScalarValues::Real = ceil(qa);
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(-7.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(-4.0, solver.getVariable("a")!!.max(), 0.00001)
    }


    /**
     * Test of the function to Real(x) : Boolean -> ScalarValues::Real.
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
        """, Runlevel.ALL)
        assertNoIssues()
        val b = solver.getVariable("b")!!
        val d = solver.getVariable("d")!!
        val f = solver.getVariable("f")!!
        assertEquals(1.0, b.aadd().min, 0.00000001)
        assertEquals(1.0, b.aadd().max, 0.00000001)
        assertEquals(0.0, d.aadd().min, 0.00000001)
        assertEquals(0.0, d.aadd().max, 0.00000001)
        assertEquals(0, d.vectorQuantity.value.height())
        assertEquals(0.0, f.aadd().min, 0.00000001)
        assertEquals(1.0, f.aadd().max, 0.00000001)
        assertEquals(1, f.vectorQuantity.value.height())
    }

    @Test @Ignore
    fun toRealEvalDownTest() = testSession("Ranges") {
        loadKerML("""
                feature a: ScalarValues::Boolean; 
                feature b: Ranges::RealInRange  = toReal(a) {:>> range = "1.0 .. 1.0";}  
        """)
        assertNoIssues()
        val a = solver.getVariable("a")
        assertTrue((a!!.vectorQuantity.value === builder.True))

    }

    /** ConstNet shall compute bottom-up with pow2 in real and model.builder.range */
    @Test
    fun evalUpWithPow2_real_range() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "1.0 .. 5.0";}
            feature b: ScalarValues::Real = power2(a);
        """, Runlevel.ALL)
        val b = solver.getVariable("b")
        assertNotNull(b)
        assertEquals(2.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(32.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with pow2 in real and value */
    @Test
    fun evalUpWithPow2_real_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = 3.0 {:>> range = "1.0 .. 5.0";}
            feature b: ScalarValues::Real = power2(a); 
        """, Runlevel.ALL)
        assertEquals(8.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(8.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun evalDownWithPow2_real_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "1.0 .. 5.0";}
            feature b: Ranges::RealInRange = power2(a) {:>> range = "8.0 .. 8.0";} """
        )
        solver.propagate()
        assertEquals(3.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(3.0, solver.getVariable("a")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with pow2 and negative values*/
    @Test
    fun evalUpWithPow2_real_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "-2.0 .. -1.0";}
            feature b: ScalarValues::Real = power2(a);
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(0.25, solver.getVariable("b")!!.aadd().min, 0.0001)
        assertEquals(0.5, solver.getVariable("b")!!.aadd().max, 0.0001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 with zero*/
    @Test
    fun evalUpWithPow2_real_zero() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "0.0 .. 0.0";}
            feature b: ScalarValues::Real = power2(a); 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("b")!!.aadd().min, 0.0001)
        assertEquals(1.0, solver.getVariable("b")!!.aadd().max, 0.0001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in int and model.builder.range*/
    @Test
    fun evalUpWithPow2_int_range() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "1 .. 5";}
            feature b: ScalarValues::Integer = power2(a); """
        )
        solver.propagate()
        assertEquals(2, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
        assertEquals(32, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with pow2 in int and model.builder.range*/
    @Test
    fun evalUpWithPow2_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange = 3 {:>> range = "1 .. 5";}
            feature b: ScalarValues::Integer = power2(a); """
        )
        assertNoIssues()
        solver.propagate()
        assertEquals(8, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
        assertEquals(8, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }
    @Test
    fun evalUpWithPow2_int_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "-3 .. -1";}
            feature b: ScalarValues::Integer = power2(a); """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(floor(0.125).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(0.5).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun evalUpWithPow2_int_negative2() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "-4 .. -2";}
            feature b: ScalarValues::Integer = power2(a); """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(floor(0.0625).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(0.25).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

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
            feature a: Ranges::RealInRange = 3.0 {:>> range = "1.0 .. 5.0";}
            feature b: ScalarValues::Real = exp(a);"""
        )
        solver.propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(Math.E.pow(3), solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(Math.E.pow(3), solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }


    /** ConstNet shall compute bottom-up with pow2 with negative value*/
    @Test
    fun evalUpWithExp_real_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "-3.0 .. -1.0";}
            feature b: ScalarValues::Real = exp(a);"""
        )
        solver.propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(Math.E.pow(-3), solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(Math.E.pow(-1), solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 and zero */
    @Test
    fun evalUpWithExp_real_zero() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "0.0 .. 0.0";}
            feature b: ScalarValues::Real = exp(a);"""
        )
        solver.propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(1.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(1.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with exp in int and model.builder.range */
    @Test
    fun evalUpWithExp_int_range() = testSession("Ranges") {
        loadKerML("feature b: ScalarValues::Integer = exp([1 .. 5]).")
        solver.propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(floor(Math.E).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(5)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute bottom-up with pow2 in int and value */
    @Test
    fun evalUpWithExp_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange  = 3 {:>> range = "1 .. 5";}
            feature b: ScalarValues::Integer = exp(a);"""
        )
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        solver.propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(floor(Math.E.pow(3)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(3)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    @Test
    fun evalDownWithExp_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange  {:>> range = "0 .. 5";}
            feature b: Ranges::IntegerInRange = exp(a) {:>> range = "1 .. 1";} """
        )

        solver.propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(0, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(0, solver.getVariable("a")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
    }

    @Test
    fun evalUpWithExp_int_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange  = -2 {:>> range = "-5 .. -1";}
            feature b: ScalarValues::Integer = exp(a);"""
        )
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        solver.propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(floor(Math.E.pow(-2)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(-2)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    @Test
    fun evalUpWithExp_int() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "3 .. 3";}
            feature b: ScalarValues::Integer = exp(a);"""
        )
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        solver.propagate()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        assertEquals(floor(Math.E.pow(3)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(3)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }


    /** ConstNet shall compute bottom-up with ln in real and model.builder.range */
    @Test
    fun evalUpWithLog_real_range() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "1.0 .. 5.0";}
            feature b: ScalarValues::Real = ln(a);"""
        )
        solver.propagate()
        assertEquals(ln(1.0), solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(ln(5.0), solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with ln in real and value */
    @Test
    fun evalUpWithLog_real_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = 3.0 {:>> range = "1.0 .. 5.0";}
            feature b: ScalarValues::Real = ln(a);
        """)
        solver.propagate()
        assertEquals(ln(3.0), solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(ln(3.0), solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with ln in real and negative numbers => not possible */
    @Test
    fun evalUpWithLog_real_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "-10.0 .. -5.0";}
            feature b: ScalarValues::Real = ln(a);
        """, Runlevel.ALL)
        val b = solver.getVariable("b")
        assertTrue(b!!.vectorQuantity.value.asAadd().isEmpty())
        assertEquals(1, status.issues.size)
    }

    /** ConstNet shall compute bottom-up with ln in int and model.builder.range */
    @Test
    fun evalUpWithLog_int_range() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "1 .. 5";}
            feature b: ScalarValues::Integer = ln(a);""")
        solver.propagate()
        assertEquals(floor(ln(1.0)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(ln(5.0)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun evalDownWithLog_int() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "1 .. 8";}
            feature b: Ranges::IntegerInRange = ln(a) {:>> range = "1 .. 2";}""")
        solver.propagate()
        assertEquals(floor(Math.E.pow(1)).toLong(), solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(ceil(Math.E.pow(2)).toLong(), solver.getVariable("a")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun evalUpWithLog_int() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "1 .. 1";}
            feature b: ScalarValues::Integer = ln(a);""")
        solver.propagate()
        assertEquals(floor(ln(1.0)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(ln(1.0)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with ln in int and value */
    @Test
    fun evalUpWithLog_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange = 3 {:>> range = "1 .. 5";}
            feature b: ScalarValues::Integer = ln(a);"""
        )
        solver.propagate()
        assertEquals(floor(ln(3.0)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(ln(3.0)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with sqrt in real and model.builder.range */
    @Test
    fun evalUpWithSqrt_real_range() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "2.0 .. 9.0";}
            feature b: ScalarValues::Real = sqrt(a);"""

        )
        solver.propagate()
        assertEquals(sqrt(2.0), solver.getVariable("b")!!.min(), 0.00001)
        //AADD returns bigger result for upper border
        assert(sqrt(9.0) <= solver.getVariable("b")!!.max<Double>())
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with sqrt in real and value */
    @Test
    fun propagateWithSqrt_real_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = 3.0 {:>> range = "2.0 .. 5.0";}
            feature b: ScalarValues::Real = sqrt(a);""")
        solver.propagate()
        assertEquals(sqrt(3.0), solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(sqrt(3.0), solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with sqrt and negative value. This should lead to an error*/
    @Test
    fun propagateWithSqrt_real_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "-10.0 .. -5.0";}
            feature b: ScalarValues::Real = sqrt( a );
            """)
        solver.propagate()
        assertEquals(1, status.issues.size, "Issues: ${status.issues}")
        assertEquals(Issue.Kind.WARN_INCONSISTENCY, status.issues.firstOrNull()?.kind)
    }

    /** ConstNet shall compute bottom-up with sqrt in int and model.builder.range */
    @Test
    fun evalUpWithSqrt_int_range() = testSession("Ranges") {
            loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "2 .. 9";}
            feature b: ScalarValues::Integer = sqrt(a);""")
            solver.propagate()
            assertEquals(floor(sqrt(2.0)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(ceil(sqrt(9.0)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
    }

    @Test
    fun evalDownWithSqrt_int_range() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "4 .. 25";}
            feature b: Ranges::IntegerInRange = sqrt(a) {:>> range = "3 .. 3";}""")
        solver.propagate()
        assertEquals(9, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(9, solver.getVariable("a")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun evalDownWithSqrt_int() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "4 .. 25";}
            feature b: Ranges::IntegerInRange = min(sqrt(a), 4) {:>> range = "3 .. 3";}""")
        solver.propagate()
        assertEquals(9, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(9, solver.getVariable("a")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun propagateWithSqr_int_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "-10 .. -5";}
            feature b: ScalarValues::Real = sqrt(a);
            """)
        solver.propagate()
        assertEquals(1, status.issues.size, "Issues: ${status.issues}")
        assertEquals(Issue.Kind.WARN_INCONSISTENCY, status.issues.firstOrNull()?.kind)
    }
    @Test
    fun evalUpWithSqr_zero() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "0 .. 0";} 
            feature b: ScalarValues::Integer = sqr(a);
            """)
        solver.propagate()
        assertEquals(0, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
        assertEquals(0, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with sqrt in int **/
    @Test
    fun evalUpWithSqrt_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange = 3 {:>> range = "2 .. 9";}
            feature b: ScalarValues::Integer = sqrt(a); """
        )
        solver.propagate()
        assertEquals(floor(sqrt(3.0)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(ceil(sqrt(3.0)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with sqr in real and model.builder.range */
    @Test
    fun evalUpWithSqr_real_range() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "2.0 .. 9.0";} 
            feature b: ScalarValues::Real = sqr(a); """
        )
        solver.propagate()
        assertEquals(4.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(81.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun evalUpWithSqr_real_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "-3.0 .. -1.0";} 
            feature b: ScalarValues::Real = sqr(a); """
        )
        solver.propagate()
        assertEquals(1.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(9.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with sqr in real and value */
    @Test
    fun evalUpWithSqr_real_value() = testSession("Ranges") {
        loadKerML("""
            feature a: ScalarValues::Real(1..5) = 3.0;  
            feature b: ScalarValues::Real = sqr(a);"""
        )
        solver.propagate()
        assertEquals(9.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(9.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with sqr in int and model.builder.range */
    @Test
    fun evalUpWithSqr_int_range() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "2 .. 9";} 
            feature b: ScalarValues::Integer = sqr(a);"""
        )
        solver.propagate()
        assertEquals(4, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
        assertEquals(81, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun evalUpWithSqr_int_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "-3 .. -2";} 
            feature b: ScalarValues::Integer = sqr(a);"""
        )
        solver.propagate()
        assertEquals(4, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
        assertEquals(9, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }


    /** ConstNet shall compute bottom-up with sqr in int and value */
    @Test
    fun evalUpWithSqr_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange = 3 {:>> range = "2 .. 9";}  
            feature b: ScalarValues::Integer = sqr(a); """
        )
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
        assertEquals(9, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
        assertEquals(9, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
    }

    @Test
    fun evalDownWithSqr_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "2 .. 9";}  
            feature b: Ranges::IntegerInRange = sqr(a) {:>> range = "8 .. 23";}  """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(2, (solver.getVariable("a")!!.idd() as IDD.Leaf).value.min)
        assertEquals(5, (solver.getVariable("a")!!.idd() as IDD.Leaf).value.max)
        assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
    }

    @Test
    fun evalUpWithPowB_real_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "3.0 .. 3.0";} 
            feature b: Ranges::RealInRange {:>> range = "4.0 .. 4.0";} 
            feature c: ScalarValues::Real = power(a, b); 
        """, Runlevel.ALL)
        val c = solver.getVariable("c") !!
        assertEquals(81.0, c.min(), 0.000001)
        assertEquals(81.0, c.max(), 0.000001)
        assertEquals("1", c.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun evalUpWithPowB_real_range() = testSession("Ranges") {
        loadKerML(
            """
            feature a: Ranges::RealInRange {:>> range = "1.5 .. 3.5";}
            feature b: Ranges::RealInRange {:>> range = "2.5 .. 4.5";}
            feature c: ScalarValues::Real = power(a, b); """
        )
        solver.propagate()
        assertEquals(1.5.pow(2.5), solver.getVariable("c")!!.min(), 0.000001)
        assertEquals(3.5.pow(4.5), solver.getVariable("c")!!.max(), 0.000001)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun evalUpWithPowB_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: ScalarValues::Integer = 3;
            feature b: ScalarValues::Integer = 3;
            feature c: ScalarValues::Integer = power(a, b);"""
        )
        solver.propagate()
        assertEquals(27, solver.getVariable("c")!!.idd().getRange().min)
        assertEquals(27, solver.getVariable("c")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }
    @Test
    fun power_int_value() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "2..3";}
            feature b: Ranges::IntegerInRange {:>> range = "4..5";}
            feature c: ScalarValues::Integer = power(a, b);"""
        )
        solver.propagate()
        assertEquals(16, solver.getVariable("c")!!.idd().getRange().min)
        assertEquals(243, solver.getVariable("c")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun power_evalDownA() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "1..2";}
            feature b: Ranges::IntegerInRange {:>> range = "1..3";}
            feature c: Ranges::IntegerInRange = power(a, b) {:>> range = "8..8";} """
        )
        solver.propagate()
        assertEquals(2L, solver.getVariable("a")!!.min())
        assertEquals(2L, solver.getVariable("a")!!.max())
        assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test @Ignore
    fun power_evalDownB() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "1..2";}
            feature b: Ranges::IntegerInRange {:>> range = "1..3";}
            feature c: Ranges::IntegerInRange = power(a, b) {:>> range = "8..8";} """
        )
        solver.propagate()
        assertEquals(8, solver.getVariable("c")!!.idd().getRange().min)
        assertEquals(8, solver.getVariable("c")!!.idd().getRange().max)
        assertEquals(2, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(2, solver.getVariable("a")!!.idd().getRange().max)
        assertEquals(3, solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(3, solver.getVariable("b")!!.idd().getRange().max)
        assertNoIssues()
    }

    @Test @Ignore
    fun power_int_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "-3..-2";}
            feature b: Ranges::IntegerInRange {:>> range = "4..5";}
            feature c: Ranges::IntegerInRange = power(a, b);"""
        )
        solver.propagate()
        assertEquals(-32, solver.getVariable("c")!!.idd().getRange().min)
        assertEquals(81, solver.getVariable("c")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("c")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test @Ignore
    fun evalUpWithPowB_int_zero() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = 0;
            feature b: ScalarValues::Integer = 3;
            feature c: ScalarValues::Integer = power(a, b);"""
        )
        solver.propagate()
        assertEquals(0, solver.getVariable("c")!!.idd().getRange().min)
        assertEquals(0, solver.getVariable("c")!!.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }


    @Test
    fun evalUpWithPow_int_range() = testSession("Ranges") {
        loadKerML("""
            feature a: ScalarValues::Integer = oneOf(2 .. 3);
            feature b: ScalarValues::Integer = oneOf(3 .. 4);
            feature c: ScalarValues::Integer = power(a, b); 
        """, Runlevel.ALL)
        val c = solver.getVariable("c") !!
        assertEquals(8, c.idd().getRange().min)
        assertEquals(81, c.idd().getRange().max)
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun evalUpPowerNegativeBase() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real = 1.0;
            feature a: ScalarValues::Real = pow(-5.0,1.0);
        """, Runlevel.ALL)
        assertEquals(-5.0, solver.getVariable("a")!!.min(), 0.00001)
        assertNoIssues()
    }

    @Test
    fun evalUpPowerNegativeBase2() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real = 1.0;
            feature a: ScalarValues::Real = pow(-1.0,2.0);
        """, Runlevel.ALL)
        assertEquals(1.0, solver.getVariable("a")!!.min(), 0.00001)
        assertNoIssues()
    }

    @Test
    fun evalUpPowerSpecialCase() = testSession("Ranges") {
        loadKerML(""" 
            feature a: ScalarValues::Real = oneOf(0.1..2.0); 
            feature b: ScalarValues::Real = pow(a, [1.0..2.0]);
        """)
        solver.propagate()
        assertNoIssues()
        val a = solver.getVariable("a")!!
        val b = solver.getVariable("b")!!
        assertEquals(0.1, a.min(), 0.0001)
        assertEquals(2.0, a.max(), 0.0001)
        assertEquals(0.01, b.min(), 0.0001)
        assertEquals(4.0, b.max(), 0.0001)
    }

    @Test
    fun sumEvalUp1() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real; 
            feature a: ScalarValues::Real = oneOf(1.0..3.0);
            feature b: ScalarValues::Real = oneOf(3.0..5.0);
            feature sum: ScalarValues::Real = sum_i( a, b, i );
        """)
        solver.propagate()
        assertEquals(15.0, solver.getVariable("sum")!!.max(), 0.00001)
        assertEquals(3.0, solver.getVariable("sum")!!.min(), 0.00001)
        assertNoIssues()
    }

    @Test
    fun sumEvalUp2() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real = oneOf(0.0..0.0);
            feature b: ScalarValues::Real = oneOf(1.0..2.0);
            feature s: ScalarValues::Real = oneOf(4.0..5.0);
            feature t: ScalarValues::Real = oneOf(2.0..2.0);
            feature sum: ScalarValues::Real = sum_i( a, b, s-t*i );
        """)
        solver.propagate()
        assertEquals(9.0, solver.getVariable("sum")!!.max(), 0.00001)
        assertEquals(6.0, solver.getVariable("sum")!!.min(), 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    @Test
    fun sumEvalDown() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: Ranges::RealInRange(1.0 .. 3.0);
            feature b: Ranges::RealInRange(1.0 .. 5.0);
            feature sum: Ranges::RealInRange = sum_i( a, b, i ) { :>> range = "3.0..10.0";}
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(3.0, solver.getVariable("sum")!!.min(), 0.00001)
        assertEquals(10.0, solver.getVariable("sum")!!.max(), 0.00001)
        assertEquals(1.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(3.0, solver.getVariable("a")!!.max(), 0.00001)
        assertEquals(1.5, solver.getVariable("b")!!.min(), 0.00001)
        // with int the borders would be 3 and 4, but rounding makes the intervals bigger
        assertEquals(4.5, solver.getVariable("b")!!.max(), 0.00001)
        assertNoIssues()
    }

    @Test
    fun sumEvalDown2() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: Ranges::RealInRange {:>> range = "0..5";} 
            feature b: Ranges::RealInRange {:>> range = "3..5";} 
            feature sum: Ranges::RealInRange = sum_i( a, b, i ) {:>> range = "3.0..14.0";}
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(3.0, solver.getVariable("sum")!!.min(), 0.00001)
        assertEquals(14.0, solver.getVariable("sum")!!.max(), 0.00001)
        assertEquals(3.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals(0.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("a")!!.max(), 0.00001)
        assertNoIssues()
    }

    //Calculations with evalDown do not work
    @Ignore
    @Test
    fun sumEvalDownWithMultiplication() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: ScalarValues::Real;
            feature s: ScalarValues::Real = 10.0;
            feature b: ScalarValues::Real = oneOf(3.0 .. 5.0);
            feature sum: Ranges::RealInRange = sum_i( a, b, s*i ) {:>> range = "30.0..140.0";}
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(30.0, solver.getVariable("sum")!!.min(), 0.00001)
        assertEquals(140.0, solver.getVariable("sum")!!.max(), 0.00001)
        assertEquals(3.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals(1.5, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(3.5, solver.getVariable("a")!!.max(), 0.00001)
        assertNoIssues()
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegative() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: Ranges::RealInRange {:>> range = "1.0..1.0";}
            feature b: Ranges::RealInRange {:>> range = "5.0..5.0";}
            feature sum: ScalarValues::Real = sum_i( a, b, pow(-1.0,i)*i );
        """)
        solver.propagate()
        assertEquals(-3.0, solver.getVariable("sum")!!.min(), 0.00001)
        assertEquals(-3.0, solver.getVariable("sum")!!.max(), 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegativeTest2() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: Ranges::RealInRange {:>> range = "-3.0..0.0";}
            feature b: Ranges::RealInRange {:>> range = "0.0..3.0";}
            feature sum: ScalarValues::Real = sum_i( a, b, pow(-1.0,i)*(1.0-sqr(i)) );
        """)
        solver.propagate()
        assertEquals(-5.0, solver.getVariable("sum")!!.min(), 0.00001)
        assertEquals(11.0, solver.getVariable("sum")!!.max(), 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegativeTest3() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: Ranges::RealInRange {:>> range = "-3.0..0.0";}
            feature b: Ranges::RealInRange {:>> range = "0.0..3.0";}
            feature sum: ScalarValues::Real = sum_i( a, b, -pow(-1.0,i)*(1.0-sqr(i)) );
        """)
        solver.propagate()
        assertEquals(-11.0, solver.getVariable("sum")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("sum")!!.max(), 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegativeTest4() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: Ranges::RealInRange {:>> range = "-3.0..0.0";}
            feature b: Ranges::RealInRange {:>> range = "0.0..3.0";}
            feature sum: ScalarValues::Real = sum_i( a, b, -pow(-1.0,i));""")
        solver.propagate()
        assertEquals(-1.0, solver.getVariable("sum")!!.min(), 0.00001)
        assertEquals(1.0, solver.getVariable("sum")!!.max(), 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    @Test
    fun sumEvalDownInt() = testSession("Ranges") {
        loadKerML("""
             feature i: ScalarValues::Integer;
             feature a: ScalarValues::Integer = oneOf(1..3);
             feature b: ScalarValues::Integer = oneOf(3..4);
             feature sum: Ranges::IntegerInRange = sum_i( a, b, i ) {:>> range = "3..10";}""")
        solver.propagate()
        assertNoIssues()
        assertEquals(3, solver.getVariable("sum")!!.idd().getRange().min)
        assertEquals(10, solver.getVariable("sum")!!.idd().getRange().max)
        assertEquals(1, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(3, solver.getVariable("a")!!.idd().getRange().max)
        assertEquals(3, solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(4, solver.getVariable("b")!!.idd().getRange().max)
    }

    @Ignore //Does not work for ScalarValues::Integer
    @Test
    fun sumEvalDownInt2() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Integer;
            feature a: ScalarValues::Integer;
            feature b: ScalarValues::Integer = oneOf(3..5);
            feature sum: Ranges::IntegerInRange = sum_i( a, b, i ) {:>> range = "3..14";}""")
        solver.propagate()
        assertNoIssues()
        assertEquals(9, solver.getVariable("sum")!!.idd().getRange().min)
        assertEquals(10, solver.getVariable("sum")!!.idd().getRange().max)
        assertEquals(2, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(3, solver.getVariable("a")!!.idd().getRange().max)
        assertEquals(3, solver.getVariable("b")!!.idd().getRange().min)
        assertEquals(5, solver.getVariable("b")!!.idd().getRange().max)
        assertNoIssues()
    }

    @Test
    fun maxTest1() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "0..1";}
            feature b: Ranges::RealInRange {:>> range = "1..2";}
            feature c: ScalarValues::Real = max(a,b);
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")!!
        assertEquals(1.0, result.min(), 0.000001)
        assertEquals(2.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun maxTest1EvalDown() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::RealInRange {:>> range = "1..7";}
            feature b: Ranges::RealInRange = max(7.0,2.0+a) {:>> range = "8.0..8.0";}
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(6.0, result!!.min(), 0.000001)
        assertEquals(6.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun maxTest1bEvalDown() = testSession("Ranges") {
        loadKerML(input = """
            feature a: ScalarValues::Real = oneOf(1.0 .. 7.0);
            feature b: Ranges::RealInRange = max(8.0, 2.0+a) {:>> range = "8.0..8.0";}
            """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(1.0, result!!.min(), 0.000001)
        assertEquals(6.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun maxTest2() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::RealInRange = sqrt(9.0) {:>> range = "0..5";}
            feature b: Ranges::RealInRange = sqrt(4.0) {:>> range = "1..6";}
            feature c: Ranges::RealInRange = max(a,b);
            """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")!!
        assertEquals(3.0, result.min(), 0.000001)
        assertEquals(3.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun maxTest2EvalDown() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = sqrt(9.0) {:>> range = "0..6";}
            feature b: Ranges::RealInRange {:>> range = "1..6";}
            feature c: Ranges::RealInRange = max(a,b) {:>> range = "4.0..4.0";}
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("b")
        assertEquals(4.0, result!!.min(), 0.000001)
        assertEquals(4.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun maxTest3() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::RealInRange = 4.0 {:>> range = "0..5";}
            feature b: Ranges::RealInRange = 6.0 {:>> range = "1..6";}
            feature c: Ranges::RealInRange = max(sqrt(9.0)+a,sqrt(4.0)+b);
            """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(8.0, result!!.min(), 0.000001)
        assertEquals(8.0, result.max(), 0.000001)
        assertNoIssues()
    }


    @Test
    fun maxTest4() = testSession("ScalarValues") {
        loadKerML(input = """
                  feature c: ScalarValues::Real = max(sqrt(9.0)+3.0,sqrt(4.0)+5.0);
            """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(7.0, result!!.min(), 0.000001)
        assertEquals(7.0, result.max(), 0.000001)
        assertNoIssues()
    }
    @Test
    fun maxTestNegative() = testSession("ScalarValues") {
        loadKerML(input = """
                  feature c: ScalarValues::Real = max(-4.2,-1.3);
            """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(-1.3, result!!.min(), 0.000001)
        assertEquals(-1.3, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParams1() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "0..1";}
            feature b: Ranges::RealInRange {:>> range = "1..2";}
            feature c: Ranges::RealInRange {:>> range = "3..4";}
            feature d: Ranges::RealInRange {:>> range = "4..5";}
            feature e: ScalarValues::Real = max(a, b, c, d);
        """, Runlevel.ALL)
        assertNoIssues()
        val result = solver.getVariable("e")
        assertEquals(4.0, result!!.min(), 0.000001)
        assertEquals(5.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParamsInt1() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::IntegerInRange {:>> range = "0..1";}
            feature b: Ranges::IntegerInRange {:>> range = "1..2";}
            feature c: Ranges::IntegerInRange {:>> range = "3..4";}
            feature d: Ranges::IntegerInRange {:>> range = "4..5";}
            feature e: ScalarValues::Integer = max(a,b,c,d);
            """
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertEquals(4, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(5, result.vectorQuantity.value.asIdd().max)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParamsReal1() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::RealInRange {:>> range = "0.67..1.96";}
            feature b: Ranges::RealInRange {:>> range = "1.34..2.5";}
            feature c: Ranges::RealInRange {:>> range = "3.49..4.99";}
            feature d: Ranges::RealInRange {:>> range = "4.32..5.45";}
            feature e: ScalarValues::Real = max(a,b,c,d);
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertEquals(4.32, result!!.min(), 0.000001)
        assertEquals(5.45, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParamsRealNegative() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::RealInRange {:>> range = "-1.67..-1.6";}
            feature b: Ranges::RealInRange {:>> range = "-2.34..-1.5";}
            feature c: Ranges::RealInRange {:>> range = "-4.49..-2.99";}
            feature d: Ranges::RealInRange {:>> range = "-6.32..-5.45";}
            feature e: ScalarValues::Real = max(a,b,c,d);
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertEquals(-1.67, result!!.min(), 0.000001)
        assertEquals(-1.5, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParamsIntegerNegative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "-2..-1";}
            feature b: Ranges::IntegerInRange {:>> range = "-4..-2";}
            feature c: Ranges::IntegerInRange {:>> range = "-5..-3";}
            feature d: Ranges::IntegerInRange {:>> range = "-7..-5";}
            feature e: Ranges::IntegerInRange = max(a,b,c,d);
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertEquals(-2, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(-1, result.vectorQuantity.value.asIdd().max)
        assertNoIssues()
    }

    @Ignore
    @Test
    fun maxTestMultipleParams2() = testSession("Ranges") {
        loadKerML(input ="""
            feature a: ScalarValues::Real {:>> range = "0..1";}
            feature b: ScalarValues::Real {:>> range = "1..2";}
            feature c: ScalarValues::Real {:>> range = "2..3";}
            feature d: ScalarValues::Real {:>> range = "3..7";}
            feature e: ScalarValues::Real = max(a,b,c,d) {:>> range = "4..4";}
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("d")
        assertEquals(4.0, result!!.min(), 0.000001)
        assertEquals(4.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParams2Integer() = testSession("Ranges") {
        loadKerML(
            input ="""
            feature a: Ranges::IntegerInRange {:>> range = "0..1";}
            feature b: Ranges::IntegerInRange {:>> range = "1..2";}
            feature c: Ranges::IntegerInRange {:>> range = "2..3";}
            feature d: Ranges::IntegerInRange {:>> range = "3..7";}
            feature e: Ranges::IntegerInRange = max(a,b,c,d) {:>> range = "4..4";}
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("d")
        assertEquals(4, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result.vectorQuantity.value.asIdd().max)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParams3() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::RealInRange {:>> range = "0..7";}
            feature b: Ranges::RealInRange {:>> range = "1..6";}
            feature c: Ranges::RealInRange {:>> range = "2..5";}
            feature d: Ranges::RealInRange {:>> range = "3..4";}
            feature e: Ranges::RealInRange = max(a,b,c,d) {:>> range = "3..4";}
            """.trimIndent(), catchExceptions = true
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(0.0, result!!.min(), 0.000001)
        assertEquals(4.0, result.max(), 0.000001)
        val result1 = solver.getVariable("b")
        assertEquals(1.0, result1!!.min(), 0.000001)
        assertEquals(4.0, result1.max(), 0.000001)
        val result2 = solver.getVariable("c")
        assertEquals(2.0, result2!!.min(), 0.000001)
        assertEquals(4.0, result2.max(), 0.000001)
        val result3 = solver.getVariable("d")
        assertEquals(3.0, result3!!.min(), 0.000001)
        assertEquals(4.0, result3.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParams3Integer() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::IntegerInRange {:>> range = "0..7";}
            feature b: Ranges::IntegerInRange {:>> range = "1..6";}
            feature c: Ranges::IntegerInRange {:>> range = "2..5";}
            feature d: Ranges::IntegerInRange {:>> range = "3..4";}
            feature e: Ranges::IntegerInRange = max(a,b,c,d) {:>> range = "3..4";}
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(0, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result.vectorQuantity.value.asIdd().max)
        val result1 = solver.getVariable("b")
        assertEquals(1, result1!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result1.vectorQuantity.value.asIdd().max)
        val result2 = solver.getVariable("c")
        assertEquals(2, result2!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result2.vectorQuantity.value.asIdd().max)
        val result3 = solver.getVariable("d")
        assertEquals(3, result3!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result3.vectorQuantity.value.asIdd().max)
        assertNoIssues()
    }

    @Test
    fun minTest1() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::RealInRange {:>> range = "0..1";}
            feature b: Ranges::RealInRange {:>> range = "1..2";}
            feature c: ScalarValues::Real = min(a,b);
            """, catchExceptions = true
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(0.0, result!!.min(), 0.000001)
        assertEquals(1.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTest1EvalDown() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::RealInRange {:>> range = "1..8";}
            feature b: Ranges::RealInRange = min(8.0,a) {:>> range = "6.0..6.0";}
            """.trimIndent(), catchExceptions = true
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(6.0, result!!.min(), 0.000001)
        assertEquals(6.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTest2() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::RealInRange = sqrt(9.0) {:>> range = "0..5";}
            feature b: Ranges::RealInRange = sqrt(4.0) {:>> range = "1..6";}
            feature c: Ranges::RealInRange = min(a,b);
            """, catchExceptions = true
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(2.0, result!!.min(), 0.000001)
        assertEquals(2.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTest2EvalDown() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::RealInRange = 3.0 {:>> range = "0..6";}
            feature b: Ranges::RealInRange {:>> range = "1..6";}
            feature c: Ranges::RealInRange = min(a,b) {:>> range = "3.0..3.0";}
            """.trimIndent(), catchExceptions = true
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("b")
        assertEquals(3.0, result!!.min(), 0.000001)
        assertEquals(6.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTest3() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::RealInRange = 4.0 {:>> range = "0..5";}
            feature b: Ranges::RealInRange = 6.0 {:>> range = "1..6";}
            feature c: Ranges::RealInRange = min(sqrt(9.0)+a,sqrt(4.0)+b);
            """, catchExceptions = true
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(7.0, result!!.min(), 0.000001)
        assertEquals(7.0, result.max(), 0.000001)
        assertNoIssues()
    }


    @Test
    fun minTest4() = testSession("ScalarValues") {
        loadKerML(input = """
            feature c: ScalarValues::Real = min(sqrt(9.0)+3.0,sqrt(4.0)+5.0);
            """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(6.0, result!!.min(), 0.000001)
        assertEquals(6.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTestMultipleParams1() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::RealInRange {:>> range = "0..1";}
            feature b: Ranges::RealInRange {:>> range = "1..2";}
            feature c: Ranges::RealInRange {:>> range = "3..4";}
            feature d: Ranges::RealInRange {:>> range = "4..5";}
            feature e: ScalarValues::Real = min(a,b,c,d);
            """, catchExceptions = true
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertEquals(0.0, result!!.min(), 0.000001)
        assertEquals(1.0, result.max(), 0.000001)
        assertNoIssues()
    }
    @Test
    fun minTestMultipleParamsRealNegative() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::RealInRange {:>> range = "-4.5..-3.0";}
            feature b: Ranges::RealInRange {:>> range = "-5.5..-4.5";}
            feature c: Ranges::RealInRange {:>> range = "-6.5..-3.5";}
            feature d: Ranges::RealInRange {:>> range = "-3.5..-2.0";}
            feature e: ScalarValues::Real = min(a,b,c,d);
            """, catchExceptions = true
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertEquals(-6.5, result!!.min(), 0.000001)
        assertEquals(-4.5, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTestMultipleParamsInt1() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::IntegerInRange {:>> range = "0..1";}
            feature b: Ranges::IntegerInRange {:>> range = "1..2";}
            feature c: Ranges::IntegerInRange {:>> range = "3..4";}
            feature d: Ranges::IntegerInRange {:>> range = "4..5";}
            feature e: ScalarValues::Integer = min(a,b,c,d);
            """, catchExceptions = true
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertEquals(0, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(1, result.vectorQuantity.value.asIdd().max)
        assertNoIssues()
    }
    @Test
    fun minTestMultipleParamsIntNegative() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::IntegerInRange {:>> range = "-3..-1";}
            feature b: Ranges::IntegerInRange {:>> range = "-4..-2";}
            feature c: Ranges::IntegerInRange {:>> range = "-7..-4";}
            feature d: Ranges::IntegerInRange {:>> range = "-8..-5";}
            feature e: ScalarValues::Integer = min(a,b,c,d);
            """, catchExceptions = true
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertEquals(-8, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(-5, result.vectorQuantity.value.asIdd().max)
        assertNoIssues()
    }

    @Test
    fun minTestMultipleParams2() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::RealInRange {:>> range = "-3..1";}
            feature b: Ranges::RealInRange {:>> range = "1..2";}
            feature c: Ranges::RealInRange {:>> range = "2..3";}
            feature d: Ranges::RealInRange {:>> range = "3..7";}
            feature e: Ranges::RealInRange = min(a,b,c,d) {:>> range = "0..0";}
            """.trimIndent(), catchExceptions = true
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(0.0, result!!.min(), 0.000001)
        assertEquals(0.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTestMultipleParams2Integer() = testSession("Ranges") {
        loadKerML(
            input ="""
            feature a: Ranges::IntegerInRange {:>> range = "-3..1";}
            feature b: Ranges::IntegerInRange {:>> range = "1..2";}
            feature c: Ranges::IntegerInRange {:>> range = "2..3";}
            feature d: Ranges::IntegerInRange {:>> range = "3..7";}
            feature e: Ranges::IntegerInRange = min(a,b,c,d) {:>> range = "0..0";}
            """.trimIndent(), catchExceptions = true
        )
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(0, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(0, result.vectorQuantity.value.asIdd().max)
        assertNoIssues()
    }

    @Test
    fun minTestMultipleParams3() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "0..7";}
            feature b: Ranges::RealInRange {:>> range = "1..6";}
            feature c: Ranges::RealInRange {:>> range = "2..5";}
            feature d: Ranges::RealInRange {:>> range = "3..4";}
            feature e: Ranges::RealInRange = min(a,b,c,d) {:>> range = "4..5";}
        """, Runlevel.VARIABLES)
        val e = solver.getVariable("e")!!
        assertEquals(4.0, e.min(), 0.000001)
        assertEquals(4.0, e.min(), 0.000001)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(4.0, result!!.min(), 0.000001)
        assertEquals(7.0, result.max(), 0.000001)
        val result1 = solver.getVariable("b")
        assertEquals(4.0, result1!!.min(), 0.000001)
        assertEquals(6.0, result1.max(), 0.000001)
        val result2 = solver.getVariable("c")
        assertEquals(4.0, result2!!.min(), 0.000001)
        assertEquals(5.0, result2.max(), 0.000001)
        val result3 = solver.getVariable("d")
        assertEquals(4.0, result3!!.min(), 0.000001)
        assertEquals(4.0, result3.max(), 0.000001)
        assertNoIssues()
    }


    @Test @Ignore
    fun minTestMultipleParams3Integer() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "0..7";}
            feature b: Ranges::IntegerInRange {:>> range = "1..6";}
            feature c: Ranges::IntegerInRange {:>> range = "2..5";}
            feature d: Ranges::IntegerInRange {:>> range = "3..4";}
            feature e: ScalarValues::Integer = min(a,b,c,d) {:>> range = "4..5";}
        """, Runlevel.SOLVED)
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(4, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(7, result.vectorQuantity.value.asIdd().max)
        val result1 = solver.getVariable("b")
        assertEquals(4, result1!!.vectorQuantity.value.asIdd().min)
        assertEquals(6, result1.vectorQuantity.value.asIdd().max)
        val result2 = solver.getVariable("c")
        assertEquals(4, result2!!.vectorQuantity.value.asIdd().min)
        assertEquals(5, result2.vectorQuantity.value.asIdd().max)
        val result3 = solver.getVariable("d")
        assertEquals(4, result3!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result3.vectorQuantity.value.asIdd().max)
        assertNoIssues()
    }



    @Test
    fun minTestOneValue1() = testSession("Ranges") {
        loadKerML(input ="""
            feature a: Ranges::RealInRange {:>> range = "0.5..1";}
            feature c: ScalarValues::Real = min(a);
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(0.5, result!!.min(), 0.000001)
        assertEquals(0.5, result.max(), 0.000001)
        assertEquals(1, result.vectorQuantity.values.size )
        assertNoIssues()
    }

    @Test
    fun maxTestOneValue1() = testSession("Ranges") {
        loadKerML(
            input ="""
            feature a: Ranges::RealInRange {:>> range = "0..1.5";}
            feature c: ScalarValues::Real = max(a);
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(1.5, result!!.min(), 0.000001)
        assertEquals(1.5, result.max(), 0.000001)
        assertEquals(1, result.vectorQuantity.values.size )
        assertNoIssues()
    }

    @Test
    fun minTestOneValueInt1() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::IntegerInRange {:>> range = "0..1";}
            feature c: ScalarValues::Integer = min(a);
        """, Runlevel.ALL)
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(0L, result!!.min())
        assertEquals(0L, result.max())
        assertEquals(1, result.vectorQuantity.values.size )
        assertNoIssues()
    }
    @Test
    fun maxTestOneValueInt1() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::IntegerInRange {:>> range = "0..1";}
            feature c: ScalarValues::Integer = max(a);
        """, Runlevel.ALL)
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(1, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(1, result.vectorQuantity.value.asIdd().max)
        assertEquals(1, result.vectorQuantity.values.size )
        assertNoIssues()
    }


    @Test
    fun byParts() = testSession("Occurrences", "Ranges") {
        loadKerML("""
            class c {
                feature a: Ranges::RealInRange {:>> range = "0..10";}
            }
            class c1 :> c {
                feature a: Ranges::RealInRange {:>> range = "0..5";}
            }

            class c2 :> c {
                feature a: Ranges::RealInRange {:>> range = "0..2";}
            }
            
            class b {
                feature cElemem1: c1; 
                feature cElemen2: c2; 
                feature a: ScalarValues::Real = byParts(a); 
            }
        """, Runlevel.ALL)
        assertNoIssues()
        val result = solver.getVariable("b::a")
        assertEquals(0.0, result!!.min(), 0.000001)
        assertEquals(5.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun assertTestStepInterpolationEvalDOwn() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
        loadSysMLv2("""
            attribute Avail : Quantities::ScalarQuantityValue {:>> unit = "%"; :>> range = "0.0..100.0";} 
            attribute level: Ranges::IntegerInRange = stepInterpolation(Avail, 0.0, 2, 0.9, 3, 0.95, 4, 1.0, 5) { :>> range = "4..4"; }
        """, Runlevel.ALL)
        assertNoIssues()
        val test2 = solver.getVariable("Avail")
        assertEquals(0.95, test2!!.vectorQuantity.value.asAadd().min,0.0001)
        assertEquals(1.0, test2.vectorQuantity.value.asAadd().max,0.0001)
    }

    @Test
    fun assertTestStepInterpolationEvalDOwn2() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
        loadSysMLv2("""
            attribute reliability: Quantities::ScalarQuantityValue {:>> unit = "%"; :>> range = "0.0..100.0";}
            attribute ASIlFromReliability: Ranges::IntegerInRange = stepInterpolation(reliability, 0.0, 1, 0.99, 2, 0.995, 3, 0.999, 4) {:>> range = "3..4";} 
        """, Runlevel.ALL)
        assertNoIssues()
        val test2 = solver.getVariable("reliability")
        assertEquals(0.995, test2!!.vectorQuantity.value.asAadd().min,0.000001)
        assertEquals(1.0, test2.vectorQuantity.value.asAadd().max,0.000001)
        // val testr = solver.getVariable("Controller1::ASIlFromReliability")
        // assertEquals(1, testr!!.vectorQuantity.value.asIdd().min)
    }

    @Test
    fun assertTestStepInterpolationEvalDOwnInteger() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
        loadSysMLv2("""
            attribute Avail : Ranges::IntegerInRange {:>> range = "0..100";} 
            attribute level: Ranges::IntegerInRange = stepInterpolation(Avail, 0, 2, 90, 3, 95, 4, 100, 5) { :>> range = "5..5"; }
        """, Runlevel.ALL)
        assertNoIssues()
        val test2 = solver.getVariable("Avail")
        assertNotNull(test2)
        assertEquals(100L, test2.min() )
        assertEquals(100L, test2.max() )
    }

    @Test
    fun evalDownUDF() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
        loadSysMLv2("""
        calc def calcASIL {
            in attribute severity:        Ranges::IntegerInRange { :>> range = "0..3";}
            in attribute exposure:        Ranges::IntegerInRange { :>> range = "0..4";}
            in attribute controllability: Ranges::IntegerInRange { :>> range = "0..3";}
            attribute    sum:             ScalarValues::Integer = severity + exposure + controllability;
            attribute    sumAdapted:      ScalarValues::Integer = if controllability == 0 ? 0 else if severity == 0 ? 0 else sum; // special case for S0 and C0 the ASIL is always QM (0)
            return       result:          ScalarValues::Integer = max(sum-6,0);   
        }  
        
        attribute severity: ScalarValues::Integer = 3;     
        attribute exposure: ScalarValues::Integer = 4;
        attribute controllability: Ranges::IntegerInRange {:>> range = "0..3";}
        attribute ASILCalculated: Ranges::IntegerInRange = calcASIL(severity,exposure,controllability) {:>> range = "4..4";}           
        """, Runlevel.ALL)
        assertNoIssues()
        val test2 = solver.getVariable("controllability")
        assertEquals(3L, test2!!.min())
    }

    @Test
    fun evalDownUDF2() = testSession("Calculations", "ISQ") {
        loadSysMLv2("""
            calc def calcASIL {
                in attribute severity:        Ranges::IntegerInRange { :>> range = "0..3"; }
                in attribute exposure:        Ranges::IntegerInRange { :>> range = "0..4"; }
                in attribute controllability: Ranges::IntegerInRange { :>> range = "0..3"; }
                attribute    sum:             ScalarValues::Integer = severity + exposure + controllability;
                attribute    sumAdapted:      ScalarValues::Integer = if controllability == 0 ? 0 else if severity == 0 ? 0 else sum; // special case for S0 and C0 the ASIL is always QM (0)
                return       result:          ScalarValues::Integer = max(sum-6,0);   
            }  
            attribute severity: Ranges::IntegerInRange {:>> range = "2..2"; }
            attribute exposure: Ranges::IntegerInRange {:>> range = "3..3"; }
            attribute controllability: Ranges::IntegerInRange {:>> range = "0..3"; }
            attribute ASILCalculated: Ranges::IntegerInRange = calcASIL(severity,exposure,controllability) {:>> range = "0..0";}
        """, Runlevel.ALL)
        assertNoIssues()
        val test2 = solver.getVariable("controllability")
        assertNotNull(test2)
        assertEquals(0L, test2.min())
        assertEquals(1L, test2.max())
    }

    @Test
    fun evalUpAndDownUDFInt() = testSession("ISQ", "Calculations") {
        loadSysMLv2(""" 
            attribute a: Ranges::IntegerInRange {:>> range = "0..4";}
            attribute ASILFromAvailability: Ranges::IntegerInRange = a {:>> range = "0..4";}
            assert constraint ASIL {ASILFromAvailability == 4}   
        """, Runlevel.ALL)
        assertNoIssues()
        val test2 = solver.getVariable("ASILFromAvailability")
        assertEquals(4, test2!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, test2.vectorQuantity.value.asIdd().max)
        //assertEquals(0.999, test2!!.vectorQuantity.value.asAadd().min)
        //assertEquals(1.0, test2.vectorQuantity.value.asAadd().max)
    }
    @Test
    fun evalUpAndDownUDFReal() = testSession("ISQ", "Calculations") {
        loadSysMLv2(""" 
            attribute a: Ranges::RealInRange {:>> range = "0.0..4.0";}
            attribute ASILFromAvailability: Ranges::RealInRange = a {:>> range = "0.0..4.0";}
            assert constraint ASIL {ASILFromAvailability == 4.0}   
        """, Runlevel.ALL)
        assertNoIssues()
        val test2 = solver.getVariable("ASILFromAvailability")
        assertEquals(4.0, test2!!.vectorQuantity.value.asAadd().min)
        assertEquals(4.0, test2.vectorQuantity.value.asAadd().max)
        //assertEquals(0.999, test2!!.vectorQuantity.value.asAadd().min)
        //assertEquals(1.0, test2.vectorQuantity.value.asAadd().max)
    }

    @Test
    fun evalDownUDF3() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
        loadSysMLv2(""" 
            calc def ASIL_from_Avail {
                in attribute Avail: Quantities::ScalarQuantityValue  { :>> unit = "%";} 
                return level: ScalarValues::Integer =  stepInterpolation(Avail, 0.0, 1, 0.99, 2, 0.995, 3, 0.999, 4);
            }  
                
            calc def ASIL_from_Reliab {
                in attribute Reliab: Quantities::ScalarQuantityValue { :>> unit = "%";} 
                return level: ScalarValues::Integer = stepInterpolation(Reliab, 0.0, 1, 0.99, 2, 0.995, 3, 0.999, 4);
            }
            attribute availability: Quantities::ScalarQuantityValue {:>> unit = "%"; :>> range = "90.0..100.0";}
            attribute reliability: Quantities::ScalarQuantityValue {:>> unit = "%"; :>> range = "99.9..100.0";}
            attribute ASILFromAvailability: Ranges::IntegerInRange = ASIL_from_Avail(availability) {:>> range = "0..4";}
            attribute ASIlFromReliability: Ranges::IntegerInRange = ASIL_from_Reliab(reliability) {:>> range = "0..4";}
            assert constraint ASIL {ASIlFromReliability == ASILFromAvailability}  
        """, Runlevel.ALL)
        assertNoIssues()
        val test2 = solver.getVariable("ASILFromAvailability")
        assertEquals(4, test2!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, test2.vectorQuantity.value.asIdd().max)
        //assertEquals(0.999, test2!!.vectorQuantity.value.asAadd().min)
        //assertEquals(1.0, test2.vectorQuantity.value.asAadd().max)
    }
}