package constraintnettests.functionstests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test

class MinEvalDownTests {

    /** Only b can be small enough, so b must be inside the result's range while a keeps its range. */
    @Test
    fun minEvalDownSingleCandidateRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 5.0 .. 10.0;}
            feature b: Ranges::RealInRange {:>> range = 0.0 .. 10.0;}
            feature m: Ranges::RealInRange = min(a, b) {:>> range = 1.0 .. 2.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(5.0 .. 10.0, solver.variable("a"))
        assertBounds(1.0 .. 2.0, solver.variable("b"))
    }

    @Test
    fun minEvalDownThreeParamsRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 5.0 .. 10.0;}
            feature b: Ranges::RealInRange {:>> range = 0.0 .. 10.0;}
            feature c: Ranges::RealInRange {:>> range = 0.0 .. 10.0;}
            feature m: Ranges::RealInRange = min(a, b, c) {:>> range = 1.0 .. 2.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(5.0 .. 10.0, solver.variable("a"))
        assertBounds(1.0 .. 10.0, solver.variable("b"))
        assertBounds(1.0 .. 10.0, solver.variable("c"))
    }

    @Test
    fun minEvalDownIntSingleCandidateRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = 5 .. 10;}
            feature b: Ranges::IntegerInRange {:>> range = 0 .. 10;}
            feature m: Ranges::IntegerInRange = min(a, b) {:>> range = 1 .. 2;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(5L .. 10L, solver.variable("a"))
        assertBounds(1L .. 2L, solver.variable("b"))
    }

    /** Regression test for the IDD branch of the single-parameter-bound case: the parameter's own upper bound is kept. */
    @Test
    fun minEvalDownIntKeepsParameterUpperBoundRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = 0 .. 10;}
            feature b: Ranges::IntegerInRange {:>> range = 0 .. 8;}
            feature m: Ranges::IntegerInRange = min(a, b) {:>> range = 3 .. 4;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(3L .. 10L, solver.variable("a"))
        assertBounds(3L .. 8L, solver.variable("b"))
    }
}
