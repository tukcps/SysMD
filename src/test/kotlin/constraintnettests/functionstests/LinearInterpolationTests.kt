package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.*
import util.mockup.loadKerML
import kotlin.test.Test

class LinearInterpolationTests {


    @Test
    fun linearFunctionTest() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 2005.0;
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 10.0, 2010.0, 20.0);
                """)
        solver.propagate()
        assertNoIssues()
        val p = solver.variable("p")
        assertBounds(15.0, p)
    }

    @Test
    fun linearFunctionTestDecreasing() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 1995.0;
            feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 20.0, 2010.0, 10.0);
        """)
        solver.propagate()
        assertNoIssues()
        val p = solver.variable("p")
        assertBounds(20.0, p)
    }

    @Test
    fun linearFunctionTestSame() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 2015.0;
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 20.0, 2010.0, 10.0);
                """)
        solver.propagate()
        assertNoIssues()
        val p = solver.variable("p")
        assertBounds(10.0, p)
    }

    @Test
    fun linearFunctionTest_Reverse() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
                feature T: ScalarValues::Real;
                feature p: Ranges::RealInRange = linearInterpolation(T, 2000.0, 10.0, 2010.0, 20.0) {:>> range = 15.0;}
                """)
        solver.propagate()
        assertNoIssues()
        val t = solver.variable("T")
        assertBounds(2005.0, t)
    }

    @Test
    fun linearFunctionTest7_1() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 2015.0;
            feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 10.0, 2010.0, 20.0, 2020.0, 0.0);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(10.0 .. 10.0, solver.variable("p"))
    }

    @Test
    fun linearFunctionTest7_2() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 2005.0;
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 10.0, 2010.0, 20.0, 2020.0, 0.0);
                """)
        solver.propagate()
        assertNoIssues()
        assertBounds(15.0 .. 15.0, solver.variable("p"))
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
        assertBounds(15.0 .. 15.0, solver.variable("p"))
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
        assertBounds(20.0 .. 20.0, solver.variable("p"))
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
        assertBounds(20.0 .. 20.0, solver.variable("p"))
    }

    // y = y0 and y1 > y0 and y2 < y0
    @Test
    fun linearFunctionTest7_6() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 1995.0.
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 20.0, 2010.0, 30.0, 2020.0, 10.0).
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(20.0 .. 20.0, solver.variable("p"))
    }

    // y = y2 and y1 > y2 and y0 > y2
    @Test
    fun linearFunctionTest7_7() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 2020.0.
            feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 30.0, 2010.0, 20.0, 2020.0, 10.0).
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(10.0 .. 10.0, solver.variable("p"))
    }

    // y = y2 and y1 > y2 and y0 < y2
    @Test
    fun linearFunctionTest7_8() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 2025.0;
            feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 0.0, 2010.0, 20.0, 2020.0, 10.0);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(10.0 .. 10.0, solver.variable("p"))
    }

    @Test
    fun linearFunctionTest7_8_reverse() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature T: ScalarValues::Real;
            feature p: Ranges::RealInRange = linearInterpolation(T, 2000.0, 0.0, 2010.0, 20.0, 2020.0, 10.0) {:>> range = 10.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(2005.0 .. Double.POSITIVE_INFINITY, solver.variable("T"))
    }

    @Test
    fun linearFunctionTest7_9_reverse() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature T: ScalarValues::Real;
            feature p: Ranges::RealInRange = linearInterpolation(T, 2000.0, 0.0, 2010.0, 20.0, 2020.0, 0.0) {:>> range = 10.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(2005.0 .. 2015.0, solver.variable("T"))
    }

    @Test
    fun linearRangeRealTest() = testSession("ISQ") {
        loadKerML("""
            feature DataRate: ISQ::BitRateValue = linearInterpolation(Month("2025-01"), Month("2021-01"), [20.0 .. 80.0] [MB/s], Month("2030-01"), [100.0 .. 800.0] [MB/s]).
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val t = solver.variable("DataRate")
        // Calculation must respect leap years
        assertBounds(55.5582598113782 .. 400.0243383024034, t, unit = "MB/s")
    }

    @Test
    fun linearInterpolation9ParamsTest() = testSession("Ranges", runlevel = Runlevel.VARIANCE_CHECKED) {
        loadKerML("""
            feature T: Ranges::RealInRange {:>> range = 1.5;}
            feature p: ScalarValues::Real = linearInterpolation(T, 0.0, 10.0, 1.0, 20.0, 2.0, 30.0, 3.0, 40.0);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(25.0 .. 25.0, solver.variable("p"))
    }

    /**
     * Regression test for multi-segment linearInterpolation (IndexOutOfBounds bug when indexing segments 0..N instead of 0 until N).
     * 5 points -> 4 segments (0, 1, 2, 3).
     */
    @Test
    fun linearInterpolationMultiSegmentRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature T: Ranges::RealInRange {:>> range = 2.5;}
            feature p: ScalarValues::Real = linearInterpolation(T, 0.0, 0.0, 1.0, 10.0, 2.0, 20.0, 3.0, 30.0, 4.0, 40.0);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(25.0 .. 25.0, solver.variable("p"))
    }

    /**
     * Regression test for y1y2BothBiggerOrSmallerThanY typo in reverse interpolation.
     */
    @Test
    fun linearInterpolationReverseY1Y2TypoRegressionTest() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature T: ScalarValues::Real;
            feature p: Ranges::RealInRange = linearInterpolation(T, 10.0, 100.0, 20.0, 200.0, 30.0, 300.0) {:>> range = 250.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val t = solver.variable("T")
        assertBounds(25.0 .. 25.0, t)
    }


    /** Regression test: decreasing segments in the 7-parameter variant. */
    @Test
    fun linearInterpolationReverseDecreasingRegressionTest() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature T: ScalarValues::Real;
            feature p: Ranges::RealInRange = linearInterpolation(T, 0.0, 10.0, 1.0, 6.0, 2.0, 0.0) {:>> range = 3.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(1.5 .. 1.5, solver.variable("T"))
    }

    /** Regression test: non-monotonic data has two solutions, the hull of both must be kept. */
    @Test
    fun linearInterpolationReverseNonMonotonicRegressionTest() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature T: ScalarValues::Real;
            feature p: Ranges::RealInRange = linearInterpolation(T, 0.0, 0.0, 1.0, 10.0, 2.0, 0.0) {:>> range = 5.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.5 .. 1.5, solver.variable("T"))
    }

    /** Regression test: non-monotonic data with 9 parameters. */
    @Test
    fun linearInterpolationReverseNonMonotonic9RegressionTest() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature T: ScalarValues::Real;
            feature p: Ranges::RealInRange = linearInterpolation(T, 0.0, 0.0, 1.0, 10.0, 2.0, 0.0, 3.0, 10.0) {:>> range = 5.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.5 .. 2.5, solver.variable("T"))
    }

    /** Regression test: a flat segment must not produce NaN (division by zero) in the reverse direction. */
    @Test
    fun linearInterpolationReverseFlatSegmentRegressionTest() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature T: Ranges::RealInRange {:>> range = 0.0 .. 100.0;}
            feature p: ScalarValues::Real = linearInterpolation(T, 10.0, 5.0, 20.0, 5.0);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.0 .. 100.0, solver.variable("T"))
    }
}
