package constraintnettests.functionstests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
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
        assertBounds(0.0 .. 1.0, result!!)
        assertNoIssues()
    }

    @Test
    fun minTest1EvalDown() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1..8;}
            feature b: Ranges::RealInRange = min(8.0,a) {:>> range = 6.0..6.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertBounds(6.0 .. 6.0, result!!)
        assertNoIssues()
    }

    @Test
    fun minTest2() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = sqrt(9.0) {:>> range = 0..5;}
            feature b: Ranges::RealInRange = sqrt(4.0) {:>> range = 1.. 6;}
            feature c: Ranges::RealInRange = min(a,b);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertBounds(2.0 .. 2.0, result!!)
        assertNoIssues()
    }

    @Test
    fun minTest2EvalDown() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = 3.0 {:>> range = 0.. 6;}
            feature b: Ranges::RealInRange {:>> range = 1.. 6;}
            feature c: Ranges::RealInRange = min(a,b) {:>> range = 3.0..3.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("b")
        assertBounds(3.0 .. 6.0, result!!)
        assertNoIssues()
    }

    @Test
    fun minTest3() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = 4.0 {:>> range = 0..5;}
            feature b: Ranges::RealInRange = 6.0 {:>> range = 1.. 6;}
            feature c: Ranges::RealInRange = min(sqrt(9.0)+a,sqrt(4.0)+b);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertBounds(7.0 .. 7.0, result!!)
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
        assertBounds(6.0 .. 6.0, result!!)
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
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertBounds(0.0 .. 1.0, result!!)
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
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertBounds(-6.5 .. -4.5, result!!)
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
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertBounds(0L .. 1L, result!!)
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
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertBounds(-8L .. -5L, result!!)
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
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertBounds(0.0 .. 0.0, result!!)
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
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertBounds(0L .. 0L, result!!)
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
        val e = solver.variable("e")
        solver.propagate()
        assertNoIssues()
        assertBounds(4.0, e)
        val result = solver.getVariable("a")
        assertBounds(4.0 .. 7.0, result!!)
        val result1 = solver.getVariable("b")
        assertBounds(4.0 .. 6.0, result1!!)
        val result2 = solver.getVariable("c")
        assertBounds(4.0 .. 5.0, result2!!)
        val result3 = solver.getVariable("d")
        assertBounds(4.0 .. 4.0, result3!!)
        assertNoIssues()
    }


    @Test
    fun minTestMultipleParams3Integer() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = 0.. 7;}
            feature b: Ranges::IntegerInRange {:>> range = 1.. 6;}
            feature c: Ranges::IntegerInRange {:>> range = 2..5;}
            feature d: Ranges::IntegerInRange {:>> range = 3.. 4;}
            feature e: Ranges::IntegerInRange = min(a,b,c,d) {:>> range = 4..5;}
        """, Runlevel.SOLVED)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertBounds(4L .. 7L, result!!)
        val result1 = solver.getVariable("b")
        assertBounds(4L .. 6L, result1!!)
        val result2 = solver.getVariable("c")
        assertBounds(4L .. 5L, result2!!)
        val result3 = solver.getVariable("d")
        assertBounds(4L .. 4L, result3!!)
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
        assertBounds(0.5 .. 0.5, result!!)
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
        assertBounds(1.5 .. 1.5, result!!)
        assertEquals(1, result.vectorQuantity.values.size )
        assertNoIssues()
    }

    @Test
    fun minTestOneValueInt1() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::IntegerInRange {:>> range = 0.. 1;}
            feature c: ScalarValues::Integer = min(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertBounds(0L .. 0L, result!!)
        assertEquals(1, result.vectorQuantity.values.size )
        assertNoIssues()
    }

    /**
     * Regression test for min evalDown:
     * When min(a, b) in [5.0..6.0], both 'a' and 'b' lower bounds are constrained to >= 5.0.
     */
    @Test
    fun minEvalDownConstrainsLowerBoundsRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 10.0;}
            feature b: Ranges::RealInRange {:>> range = 2.0 .. 8.0;}
            feature m: Ranges::RealInRange = min(a, b) {:>> range = 5.0 .. 6.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        val b = solver.variable("b")
        assertBounds(5.0 .. 10.0, a)
        assertBounds(5.0 .. 8.0, b)
    }

    @Test
    fun minEvalDownIntConstrainsLowerBoundsRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = 0 .. 10;}
            feature b: Ranges::IntegerInRange {:>> range = 2 .. 8;}
            feature m: Ranges::IntegerInRange = min(a, b) {:>> range = 5 .. 6;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        val b = solver.variable("b")
        assertBounds(5L .. 10L, a)
        assertBounds(5L .. 8L, b)
    }
}
