package sysmdtests

import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Connector
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("UNUSED_VARIABLE")
class PredefinedFunctionsTests {

    val tol = 0.0001

    // The operations exp, log, pow2, sqrt, ln ... are supported
    // also to test: ITE function
    @Test
    fun operationsTest() = testSession("ScalarValues") {
        loadKerML("""
                feature test1: ScalarValues::Real = ln(5.0);
                feature test2: ScalarValues::Real = sqrt(5.0);
                feature test3: ScalarValues::Real = exp(5.0);
                feature test4: ScalarValues::Real = power2(5.0);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(ln(5.0), global.resolveVar("test1")!!.min(), 0.00001)
        assertEquals(sqrt(5.0), global.resolveVar("test2")!!.min(), 0.00001)
        assertEquals(exp(5.0), global.resolveVar("test3")!!.min(), 0.00001)
        assertEquals(32.0, global.resolveVar("test4")!!.min(), 0.00001)
    }

    @Test
    fun rangeOperatorTest() = testSession("ScalarValues") {
        loadKerML("""
            feature p: ScalarValues::Real = [1.0 .. 2.0] + 2.0;
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(3.0, global.resolveVar("p")!!.min(), 0.001)
    }



    @Test
    fun linearFunctionTest() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 2005.0;
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 10.0, 2010.0, 20.0);
                """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertTrue(15.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
        assertTrue(p.min<Double>() > 14.9)
        assertTrue(p.vectorQuantity.getMaxAsDouble() < 15.1)
    }

    @Test
    fun linearFunctionTestDecreasing() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 1995.0;
            feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 20.0, 2010.0, 10.0);
        """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertTrue(20.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
        assertTrue(p.min<Double>() > 19.9)
        assertTrue(p.vectorQuantity.getMaxAsDouble() < 20.1)
    }

    @Test
    fun linearFunctionTestSame() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 2015.0;
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 20.0, 2010.0, 10.0);
                """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertTrue(10.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
        assertTrue(p.min<Double>() > 9.9)
        assertTrue(p.vectorQuantity.getMaxAsDouble() < 10.1)
    }

    @Test
    fun linearFunctionTest_Reverse() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
                feature T: ScalarValues::Real;
                feature p: Ranges::RealInRange = linearInterpolation(T, 2000.0, 10.0, 2010.0, 20.0) {:>> range = "15.0..15.0";}
                """)
        solver.propagate()
        assertNoIssues()
        val t = global.resolveVar("T")
        assertTrue(2005.0 in t?.vectorQuantity?.aadd()?.getRange()!!)
        assertTrue(t.min<Double>() > 2004.9)
        assertTrue(t.vectorQuantity.getMaxAsDouble() < 2005.1)
    }

    @Test
    fun linearFunctionTest7_1() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 2015.0;
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 10.0, 2010.0, 20.0, 2020.0, 0.0);
                """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(p!!.min(), 10.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 10.0, 0.000001)
    }

    @Test
    fun linearFunctionTest7_2() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 2005.0;
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 10.0, 2010.0, 20.0, 2020.0, 0.0);
                """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(p!!.min(), 15.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 15.0, 0.000001)
    }

    // y0 < y1 and y2 between y0 and y1
    @Test
    fun linearFunctionTest7_3() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 2005.0;
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 10.0, 2010.0, 20.0, 2015.0, 0.0);
                """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(p!!.min(), 15.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 15.0, 0.000001)
    }

    // y1 < y0 and y2 > y1
    @Test
    fun linearFunctionTest7_4() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 2015.0;
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 20.0, 2010.0, 10.0, 2020.0, 30.0);
                """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(p!!.min(), 20.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 20.0, 0.000001)
    }

    // y = y0 and y1 > y0 and y2 > y0
    @Test
    fun linearFunctionTest7_5() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 2000.0.
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 20.0, 2010.0, 30.0, 2020.0, 40.0).
                """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(p!!.min(), 20.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 20.0, 0.000001)
    }

    // y = y0 and y1 > y0 and y2 < y0
    @Test
    fun linearFunctionTest7_6() = testSession("ScalarValues") {
        loadKerML(catchExceptions = false, input = """
                feature T: ScalarValues::Real = 1995.0.
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 20.0, 2010.0, 30.0, 2020.0, 10.0).
                """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(p!!.min(), 20.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 20.0, 0.000001)
    }

    // y = y2 and y1 > y2 and y0 > y2
    @Test
    fun linearFunctionTest7_7() = testSession("ScalarValues") {
        loadKerML(catchExceptions = false, input = """
                feature T: ScalarValues::Real = 2020.0.
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 30.0, 2010.0, 20.0, 2020.0, 10.0).
                """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(p!!.min(), 10.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 10.0, 0.000001)
    }

    // y = y2 and y1 > y2 and y0 < y2
    @Test
    fun linearFunctionTest7_8() = testSession("ScalarValues") {
        loadKerML(catchExceptions = false, input = """
                feature T: ScalarValues::Real = 2025.0.
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 0.0, 2010.0, 20.0, 2020.0, 10.0).
                """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(p!!.min(), 10.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 10.0, 0.000001)
    }

    @Test
    fun linearFunctionTest7_8_reverse() = testSession("ScalarValues", "Ranges") {
        loadKerML(catchExceptions = false, input = """
                feature T: ScalarValues::Real;
                feature p: Ranges::RealInRange = linearInterpolation(T, 2000.0, 0.0, 2010.0, 20.0, 2020.0, 10.0) {:>> range = "10.0";}
                """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("T")
        assertEquals(p!!.min(), 2005.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), Double.MAX_VALUE, 0.000001)
    }

    @Test
    fun linearFunctionTest7_9_reverse() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
                feature T: ScalarValues::Real;
                feature p: Ranges::RealInRange = linearInterpolation(T, 2000.0, 0.0, 2010.0, 20.0, 2020.0, 0.0) {:>> range = "10.0";}
                """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("T")
        assertEquals(p!!.min(), 2005.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 2015.0, 0.000001)
    }


    /**
     * Test of Floor.
     */
    @Test
    fun testFloorFxnA() {
        testSession("ScalarValues", "Ranges") {
            loadKerML("feature p: Ranges::RealInRange {:>> range = \"1.0 .. 1.0\";}") // padding, 0 = NOT enabled, 1 = enabled
            loadKerML("feature C_wb_s_floor_arg: Ranges::RealInRange {:>> range = \"2.75 .. 2.75\";}")
            loadKerML("feature a_pb: ScalarValues::Real = p * floor(C_wb_s_floor_arg);")
            assertEquals(2.0, global.resolveVar( "a_pb")!!.vectorQuantity.value.asAadd().getRange().min, tol)
            assertEquals(2.0, global.resolveVar("a_pb")!!.vectorQuantity.value.asAadd().getRange().max, tol)
        }
    }

    /**
     * Test of Floor.
     */
    @Test
    fun testFloorFxnB() = testSession("ScalarValues", "Ranges") {
            loadKerML("feature C_wb_s_floor_arg: Ranges::RealInRange {:>> range = \"2.75 .. 2.75\";}")
            loadKerML("feature a_pb: ScalarValues::Real = floor(C_wb_s_floor_arg);")
            assertEquals(2.0, global.resolveVar("a_pb")!!.vectorQuantity.value.asAadd().getRange().min, tol)
            assertEquals(2.0, global.resolveVar("a_pb")!!.vectorQuantity.value.asAadd().getRange().max, tol)
    }

    /**
     * Test of Floor.
     */
    @Test
    fun testFloorFxnC() = testSession("ScalarValues", "Ranges") {
        loadKerML("feature C_wb_s_floor_arg: Ranges::RealInRange {:>> range = \"2.75 .. 2.75\";} feature a_pb: ScalarValues::Real = floor(C_wb_s_floor_arg) - 1.0.")
        assertEquals(1.0, global.resolveVar("a_pb")!!.vectorQuantity.value.asAadd().getRange().min, 0.001)
        assertEquals(1.0, global.resolveVar("a_pb")!!.vectorQuantity.value.asAadd().getRange().max, 0.001)
    }


    /**
     * Test of Floor.
     */
    @Test
    fun testFloorFxnD() {
        testSession("ScalarValues", "Ranges") {
            loadKerML("feature C_wb_s_floor_arg: Ranges::RealInRange {:>> range = \"2.75 .. 2.75\";}")
            loadKerML("feature a_pb: ScalarValues::Real = 3.0 - floor(C_wb_s_floor_arg);")
            assertEquals(1.0, global.resolveVar("a_pb")!!.vectorQuantity.value.asAadd().getRange().min, tol)
            assertEquals(1.0, global.resolveVar("a_pb")!!.vectorQuantity.value.asAadd().getRange().max, tol)
        }
    }

    /**
     * stress test number 7 --> Poles and zeroes ...
     * see: the stress tests for AADD data types in AADDTests.kt in the jAADD
     * 9x^4 - y^4 + 2 y^2 = 1
     */
    @Test
    fun testAgainstRumpEquation7() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = "2910.99 .. 2911.001";}
            feature y: Ranges::RealInRange {:>> range = "5041.999 .. 5042.001";}
            feature z: ScalarValues::Real = 9.0 * x^4.0 - y^4.0 + 2.0 * y^2.0
            """)
        val z = global.resolveVar("z")!!.vectorQuantity.value.asAadd()
        assertTrue(1.0 in z)
        // assertEquals(-1.7976931348623157E308, resolveName<Expression>(global, "z")!!.quantity.value.asAadd().getRange().min, tol)
        // assertEquals(1.7976931348623157E308, resolveName<Expression>(global, "z")!!.quantity.value.asAadd().getRange().max, tol)
    }

    @Test
    fun linearRangeRealTest() = testSession("ISQ") {
        loadKerML("""
                 feature DataRate: ISQ::BitRateValue = linearInterpolation(Month("2025-01"), Month("2021-01"), [20.0 .. 80.0] [MB/s], Month("2030-01"), [100.0 .. 800.0] [MB/s]).
                """)
        solver.propagate()
        assertNoIssues()
        val t = global.resolveVar("DataRate")
        assertEquals(28.889564952844047, t!!.vectorQuantity.valuesIn("MB/s")[0].asAadd().getRange().min, tol)
        assertEquals(426.69303316094675, t.vectorQuantity.valuesIn("MB/s")[0].asAadd().getRange().max, tol)
    }


    @Test
    fun stepFunctionTest() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 2005.0;
            feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0);
        """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertTrue(10.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
        assertTrue(p.min<Double>() > 9.9)
        assertTrue(p.vectorQuantity.getMaxAsDouble() < 10.1)
    }

    @Test
    fun stepFunctionTest2() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 1990.0;
            feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0);
        """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertTrue(10.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
        assertTrue(p.min<Double>() > 9.9)
        assertTrue(p.vectorQuantity.getMaxAsDouble() < 10.1)
    }

    @Test
    fun stepFunctionTest3() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 2011.0;
            feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0);
        """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertTrue(20.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
        assertTrue(p.min<Double>() > 19.9)
        assertTrue(p.vectorQuantity.getMaxAsDouble() < 20.1)
    }

    @Test
    fun stepFunctionTest5() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 2011.0;
            feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0, 2020.0, 0.0);
        """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertTrue(20.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
        assertTrue(p.min<Double>() > 19.9)
        assertTrue(p.vectorQuantity.getMaxAsDouble() < 20.1)
    }

    @Test
    fun stepFunctionTest6() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 2020.0;
            feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0, 2020.0, 0.0);
        """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertTrue(0.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
        assertTrue(p.min<Double>() > -0.1)
        assertTrue(p.vectorQuantity.getMaxAsDouble() < 0.1)
    }

    @Test
    fun stepFunctionTestInt() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Integer = 2020;
            feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
        """)
        initialize()
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(0L, p?.min())
        assertEquals(0L, p?.max())
    }

    @Test
    fun stepFunctionTestInt2() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Integer = 1990;
            feature p: ScalarValues::Integer = stepInterpolation(T, 1980, 10, 2010, 20, 2020, 0);
        """)
        initialize()
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(10L, p?.min())
        assertEquals(10L, p?.max())
    }

    @Test
    fun stepFunctionTestInt3() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Integer = 2000;
            feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
        """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(10L, p?.min())
        assertEquals(10L, p?.max())
    }

    @Test
    fun stepFunctionTestInt4() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Integer = 2005;
                feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
        """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(10L, p?.min())
        assertEquals(10L, p?.max())
    }

    @Test
    fun stepFunctionTestInt5() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Integer = 2015;
            feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
        """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(20L, p?.min())
        assertEquals(20L, p?.max())
    }

    @Test
    fun stepFunctionTestInt6() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Integer = 2015;
            feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
        """)
        solver.propagate()
        assertNoIssues()
        val p = global.resolveVar("p")
        assertEquals(20L, p?.min())
        assertEquals(20L, p?.max())
    }

    @Test
    fun realFunctionTest() = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Integer = [2 .. 3].
            feature r: ScalarValues::Real = ToReal(i).
            """)
        solver.propagate()
        val r = global.resolveVar("r")!!
        assertNoIssues()
        assertEquals(2.0, r.min(), 0.00001)
        assertEquals(3.0, r.vectorQuantity.getMaxAsDouble(), 0.00001)
    }

    @Test
    fun integerFunctionTest() = testSession("ScalarValues") {
        loadKerML("""
            private import ScalarValues::*; 
            feature i: Real = [2.0 .. 3.0]; 
            feature r: Integer = ToInteger(i); 
            """)
        solver.propagate()
        val i = global.resolveVar("i")
        assertNotNull(i)
        val r = global.resolveVar("r")!!
        assertNoIssues()
        assertEquals(2, r.vectorQuantity.value.asIdd().min)
        assertEquals(3, r.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun byImplementsTest() = testSession("ScalarValues", "Links") {
        loadKerML("""
            package ISO26262 {
                assoc implements {
                   end feature 'from': Base::Anything redefines source;
                   end feature 'to':  Base::Anything redefines target;  
                }
            }
            feature c {
                feature x: ScalarValues::Real = 1.0; 
            } 
            feature f {
                feature x: ScalarValues::Real = byImplements(x). 
            }
            connector r : ISO26262::implements from c to f; 
        """)
        solver.propagate()
        val impl = global.resolve("ISO26262::implements")?.member<Association>()
        assertNoIssues()
        assertNotNull(impl)
        val r = global.resolve("r")?.member<Connector>()
        assertNotNull(r)
        val fx = global.resolveVar("f::x")!!
        assertEquals(1.0, fx.min(), 0.0001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }
}
