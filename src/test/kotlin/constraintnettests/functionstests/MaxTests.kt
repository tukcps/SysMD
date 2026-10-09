package constraintnettests.functionstests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test

class MaxTests {

    @Test
    fun maxTest1() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.. 1;}
            feature b: Ranges::RealInRange {:>> range = 1.. 2;}
            feature c: ScalarValues::Real = max(a,b);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val result = solver.variable("c")
        assertBounds(1.0 .. 2.0, result)
        assertNoIssues()
    }

    @Test
    fun maxTest1EvalDown() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::RealInRange {:>> range = 1.. 7;}
            feature b: Ranges::RealInRange = max(7.0,2.0+a) {:>> range = 8.0..8.0;}
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertBounds(6.0 .. 6.0, result!!)
        assertNoIssues()
    }

    @Test
    fun maxTest1bEvalDown() = testSession("Ranges") {
        loadKerML(input = """
            feature a: ScalarValues::Real = oneOf(1.0 .. 7.0);
            feature b: Ranges::RealInRange = max(8.0, 2.0+a) {:>> range = 8.0..8.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertBounds(1.0 .. 6.0, result!!)
        assertNoIssues()
    }

    @Test
    fun maxTest2() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::RealInRange = sqrt(9.0) {:>> range = 0..5;}
            feature b: Ranges::RealInRange = sqrt(4.0) {:>> range = 1.. 6;}
            feature c: Ranges::RealInRange = max(a,b);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val result = solver.variable("c")
        assertBounds(3.0 .. 3.0, result)
        assertNoIssues()
    }

    @Test
    fun maxTest2EvalDown() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange = sqrt(9.0) {:>> range = 0.. 6;}
            feature b: Ranges::RealInRange {:>> range = 1.. 6;}
            feature c: Ranges::RealInRange = max(a,b) {:>> range = 4.0..4.0;}
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("b")
        assertBounds(4.0 .. 4.0, result!!)
        assertNoIssues()
    }

    @Test
    fun maxTest3() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::RealInRange = 4.0 {:>> range = 0..5;}
            feature b: Ranges::RealInRange = 6.0 {:>> range = 1.. 6;}
            feature c: Ranges::RealInRange = max(sqrt(9.0)+a,sqrt(4.0)+b);
            """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("c")
        assertBounds(8.0 .. 8.0, result!!)
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
        assertBounds(7.0 .. 7.0, result!!)
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
        assertBounds(-1.3 .. -1.3, result!!)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParams1() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0..1;}
            feature b: Ranges::RealInRange {:>> range = 1.. 2;}
            feature c: Ranges::RealInRange {:>> range = 3.. 4;}
            feature d: Ranges::RealInRange {:>> range = 4..5;}
            feature e: ScalarValues::Real = max(a, b, c, d);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertBounds(4.0 .. 5.0, result!!)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParamsInt1() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::IntegerInRange {:>> range = 0..1;}
            feature b: Ranges::IntegerInRange {:>> range = 1.. 2;}
            feature c: Ranges::IntegerInRange {:>> range = 3.. 4;}
            feature d: Ranges::IntegerInRange {:>> range = 4..5;}
            feature e: ScalarValues::Integer = max(a,b,c,d);
            """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertBounds(4L .. 5L, result!!)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParamsReal1() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::RealInRange {:>> range = 0.67..1.96;}
            feature b: Ranges::RealInRange {:>> range = 1.34..2.5;}
            feature c: Ranges::RealInRange {:>> range = 3.49..4.99;}
            feature d: Ranges::RealInRange {:>> range = 4.32..5.45;}
            feature e: ScalarValues::Real = max(a,b,c,d);
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertBounds(4.32 .. 5.45, result!!)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParamsRealNegative() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::RealInRange {:>> range = -1.67..-1.6;}
            feature b: Ranges::RealInRange {:>> range = -2.34..-1.5;}
            feature c: Ranges::RealInRange {:>> range = -4.49..-2.99;}
            feature d: Ranges::RealInRange {:>> range = -6.32..-5.45;}
            feature e: ScalarValues::Real = max(a,b,c,d);
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertBounds(-1.67 .. -1.5, result!!)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParamsIntegerNegative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = -2..-1;}
            feature b: Ranges::IntegerInRange {:>> range = -4..-2;}
            feature c: Ranges::IntegerInRange {:>> range = -5..-3;}
            feature d: Ranges::IntegerInRange {:>> range = -7..-5;}
            feature e: Ranges::IntegerInRange = max(a,b,c,d);
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("e")
        assertBounds(-2L .. -1L, result!!)
        assertNoIssues()
    }

    @Ignore
    @Test
    fun maxTestMultipleParams2() = testSession("Ranges") {
        loadKerML(input ="""
            feature a: ScalarValues::Real {:>> range = 0.. 1;}
            feature b: ScalarValues::Real {:>> range = 1.. 2;}
            feature c: ScalarValues::Real {:>> range = 2.. 3;}
            feature d: ScalarValues::Real {:>> range = 3.. 7;}
            feature e: ScalarValues::Real = max(a,b,c,d) {:>> range = 4.. 4;}
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("d")
        assertBounds(4.0 .. 4.0, result!!)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParams2Integer() = testSession("Ranges") {
        loadKerML(
            input ="""
            feature a: Ranges::IntegerInRange {:>> range = 0.. 1;}
            feature b: Ranges::IntegerInRange {:>> range = 1.. 2;}
            feature c: Ranges::IntegerInRange {:>> range = 2.. 3;}
            feature d: Ranges::IntegerInRange {:>> range = 3.. 7;}
            feature e: Ranges::IntegerInRange = max(a,b,c,d) {:>> range = 4.. 4;}
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("d")
        assertBounds(4L .. 4L, result!!)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParams3() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::RealInRange {:>> range = 0.. 7;}
            feature b: Ranges::RealInRange {:>> range = 1.. 6;}
            feature c: Ranges::RealInRange {:>> range = 2..5;}
            feature d: Ranges::RealInRange {:>> range = 3.. 4;}
            feature e: Ranges::RealInRange = max(a,b,c,d) {:>> range = 3.. 4;}
            """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertBounds(0.0 .. 4.0, result!!)
        val result1 = solver.getVariable("b")
        assertBounds(1.0 .. 4.0, result1!!)
        val result2 = solver.getVariable("c")
        assertBounds(2.0 .. 4.0, result2!!)
        val result3 = solver.getVariable("d")
        assertBounds(3.0 .. 4.0, result3!!)
        assertNoIssues()
    }

    @Test
    fun maxTestMultipleParams3Integer() = testSession("Ranges") {
        loadKerML(
            input = """
            feature a: Ranges::IntegerInRange {:>> range = 0.. 7;}
            feature b: Ranges::IntegerInRange {:>> range = 1.. 6;}
            feature c: Ranges::IntegerInRange {:>> range = 2..5;}
            feature d: Ranges::IntegerInRange {:>> range = 3.. 4;}
            feature e: Ranges::IntegerInRange = max(a,b,c,d) {:>> range = 3.. 4;}
        """)
        solver.propagate()
        assertNoIssues()
        val result = solver.getVariable("a")
        assertBounds(0L .. 4L, result!!)
        val result1 = solver.getVariable("b")
        assertBounds(1L .. 4L, result1!!)
        val result2 = solver.getVariable("c")
        assertBounds(2L .. 4L, result2!!)
        val result3 = solver.getVariable("d")
        assertBounds(3L .. 4L, result3!!)
        assertNoIssues()
    }

    /**
     * Regression test for max evalDown:
     * When max(a, b) in [3.0..4.0], both 'a' and 'b' upper bounds are constrained to <= 4.0.
     */
    @Test
    fun maxEvalDownConstrainsUpperBoundsRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 10.0;}
            feature b: Ranges::RealInRange {:>> range = 0.0 .. 8.0;}
            feature m: Ranges::RealInRange = max(a, b) {:>> range = 3.0 .. 4.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        val b = solver.variable("b")
        assertBounds(0.0 .. 4.0, a)
        assertBounds(0.0 .. 4.0, b)
    }

    @Test
    fun maxEvalDownIntConstrainsUpperBoundsRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = 0 .. 10;}
            feature b: Ranges::IntegerInRange {:>> range = 0 .. 8;}
            feature m: Ranges::IntegerInRange = max(a, b) {:>> range = 3 .. 4;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        val b = solver.variable("b")
        assertBounds(0L .. 4L, a)
        assertBounds(0L .. 4L, b)
    }

    /**
     * Regression test: only parameter b can reach the lower bound of the result, so b must be inside the result's
     * range while the existing range of a stays untouched (not overwritten).
     */
    @Test
    fun maxEvalDownSingleCandidateRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 3.0;}
            feature b: Ranges::RealInRange {:>> range = 0.0 .. 10.0;}
            feature m: Ranges::RealInRange = max(a, b) {:>> range = 5.0 .. 6.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.0 .. 3.0, solver.variable("a"))
        assertBounds(5.0 .. 6.0, solver.variable("b"))
    }

    /** Regression test: three parameters, two of them can still reach the result, so no lower bound is forced. */
    @Test
    fun maxEvalDownTwoCandidatesRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 3.0;}
            feature b: Ranges::RealInRange {:>> range = 0.0 .. 10.0;}
            feature c: Ranges::RealInRange {:>> range = 0.0 .. 10.0;}
            feature m: Ranges::RealInRange = max(a, b, c) {:>> range = 5.0 .. 6.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.0 .. 3.0, solver.variable("a"))
        assertBounds(0.0 .. 6.0, solver.variable("b"))
        assertBounds(0.0 .. 6.0, solver.variable("c"))
    }

    @Test
    fun maxEvalDownIntSingleCandidateRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = 0 .. 3;}
            feature b: Ranges::IntegerInRange {:>> range = 0 .. 10;}
            feature m: Ranges::IntegerInRange = max(a, b) {:>> range = 5 .. 6;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0L .. 3L, solver.variable("a"))
        assertBounds(5L .. 6L, solver.variable("b"))
    }
}
