package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class MinTests {

    @Test
    fun minTest1() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::RealInRange {:>> range = 0.. 1;}
            feature b: Ranges::RealInRange {:>> range = 1.. 2;}
            feature c: ScalarValues::Real = min(a,b);
            """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(0.0, result!!.min(), 0.000001)
        assertEquals(1.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTest1EvalDown() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1..8;}
            feature b: Ranges::RealInRange = min(8.0,a) {:>> range = 6.0..6.0;}
        """, Runlevel.ALL)
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(6.0, result!!.min(), 0.000001)
        assertEquals(6.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTest2() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = sqrt(9.0) {:>> range = 0..5;}
            feature b: Ranges::RealInRange = sqrt(4.0) {:>> range = 1.. 6;}
            feature c: Ranges::RealInRange = min(a,b);
            """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(2.0, result!!.min(), 0.000001)
        assertEquals(2.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTest2EvalDown() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = 3.0 {:>> range = 0.. 6;}
            feature b: Ranges::RealInRange {:>> range = 1.. 6;}
            feature c: Ranges::RealInRange = min(a,b) {:>> range = 3.0..3.0;}
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("b")
        assertEquals(3.0, result!!.min(), 0.000001)
        assertEquals(6.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTest3() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = 4.0 {:>> range = 0..5;}
            feature b: Ranges::RealInRange = 6.0 {:>> range = 1.. 6;}
            feature c: Ranges::RealInRange = min(sqrt(9.0)+a,sqrt(4.0)+b);
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertEquals(7.0, result!!.min(), 0.000001)
        assertEquals(7.0, result.max(), 0.000001)
        assertNoIssues()
    }


    @Test
    fun minTest4() = testSession("ScalarValues") {
        loadKerML("""
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
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = 0..1;}
            feature b: Ranges::RealInRange { :>> range = 1..2;}
            feature c: Ranges::RealInRange { :>> range = 3..4;}
            feature d: Ranges::RealInRange { :>> range = 4..5;}
            feature e: ScalarValues::Real = min(a,b,c,d);
        """, Runlevel.ALL)
        assertNoIssues()
        val result = solver.getVariable("e")
        assertEquals(0.0, result!!.min(), 0.000001)
        assertEquals(1.0, result.max(), 0.000001)
        assertNoIssues()
    }
    @Test
    fun minTestMultipleParamsRealNegative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -4.5..-3.0;}
            feature b: Ranges::RealInRange {:>> range = -5.5..-4.5;}
            feature c: Ranges::RealInRange {:>> range = -6.5..-3.5;}
            feature d: Ranges::RealInRange {:>> range = -3.5..-2.0;}
            feature e: ScalarValues::Real = min(a,b,c,d);
        """, Runlevel.ALL)
        assertNoIssues()
        val result = solver.getVariable("e")
        assertEquals(-6.5, result!!.min(), 0.000001)
        assertEquals(-4.5, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTestMultipleParamsInt1() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = 0 .. 1; }
            feature b: Ranges::IntegerInRange {:>> range = 1 .. 2; }
            feature c: Ranges::IntegerInRange {:>> range = 3 .. 4; }
            feature d: Ranges::IntegerInRange {:>> range = 4 ..5; }
            feature e: ScalarValues::Integer = min(a,b,c,d);
            """, Runlevel.ALL)
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
            feature a: Ranges::IntegerInRange {:>> range = -3..-1;}
            feature b: Ranges::IntegerInRange {:>> range = -4..-2;}
            feature c: Ranges::IntegerInRange {:>> range = -7..-4;}
            feature d: Ranges::IntegerInRange {:>> range = -8..-5;}
            feature e: ScalarValues::Integer = min(a,b,c,d);
            """, Runlevel.ALL
        )
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
            feature a: Ranges::RealInRange {:>> range = -3..1;}
            feature b: Ranges::RealInRange {:>> range = 1.. 2;}
            feature c: Ranges::RealInRange {:>> range = 2.. 3;}
            feature d: Ranges::RealInRange {:>> range = 3.. 7;}
            feature e: Ranges::RealInRange = min(a,b,c,d) {:>> range = 0..0;}
        """, Runlevel.ALL)
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(0.0, result!!.min(), 0.000001)
        assertEquals(0.0, result.max(), 0.000001)
        assertNoIssues()
    }

    @Test
    fun minTestMultipleParams2Integer() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = -3..1;}
            feature b: Ranges::IntegerInRange {:>> range = 1..2;}
            feature c: Ranges::IntegerInRange {:>> range = 2..3;}
            feature d: Ranges::IntegerInRange {:>> range = 3..7;}
            feature e: Ranges::IntegerInRange = min(a,b,c,d) {:>> range = 0..0;}
        """, Runlevel.ALL)
        assertNoIssues()
        val result = solver.getVariable("a")
        assertEquals(0, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(0, result.vectorQuantity.value.asIdd().max)
        assertNoIssues()
    }

    @Test
    fun minTestMultipleParams3() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.. 7;}
            feature b: Ranges::RealInRange {:>> range = 1.. 6;}
            feature c: Ranges::RealInRange {:>> range = 2..5;}
            feature d: Ranges::RealInRange {:>> range = 3.. 4;}
            feature e: Ranges::RealInRange = min(a,b,c,d) {:>> range = 4..5;}
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
            feature a: Ranges::IntegerInRange {:>> range = 0.. 7;}
            feature b: Ranges::IntegerInRange {:>> range = 1.. 6;}
            feature c: Ranges::IntegerInRange {:>> range = 2..5;}
            feature d: Ranges::IntegerInRange {:>> range = 3.. 4;}
            feature e: ScalarValues::Integer = min(a,b,c,d) {:>> range = 4..5;}
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
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.5.. 1;}
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
            feature a: Ranges::RealInRange {:>> range = 0..1.5;}
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


    /** Min on Real scalars */
    @Test
    fun min_quantity() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = 1.0;
            feature b: ScalarValues::Real = 2.0;
            feature y: ScalarValues::Real = min(a, b);
        """, Runlevel.ALL)
        val y = solver.getVariable("y")!!
        assertEquals(1.0, y.min(), 0.0001)
        assertEquals(1.0, y.max(), 0.0001)
        assertNoIssues()
    }

    /** Min on Integer scalars */
    @Test
    fun min_quantity2() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = 1;
            feature b: ScalarValues::Integer = 2;
            feature y: ScalarValues::Integer = min(a, b);
        """, Runlevel.ALL)
        val y = solver.getVariable("y")!!
        assertEquals(1.0, y.min(), 0.0001)
        assertEquals(1.0, y.max(), 0.0001)
        assertNoIssues()
    }
}
