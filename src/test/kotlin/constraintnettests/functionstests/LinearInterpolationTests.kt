package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinearInterpolationTests {

    val tol = 0.0001

    @Test
    fun linearFunctionTest() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 2005.0;
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 10.0, 2010.0, 20.0);
                """)
        solver.propagate()
        assertNoIssues()
        val p = solver.getVariable("p")
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
        val p = solver.getVariable("p")
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
        val p = solver.getVariable("p")
        assertTrue(10.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
        assertTrue(p.min<Double>() > 9.9)
        assertTrue(p.vectorQuantity.getMaxAsDouble() < 10.1)
    }

    @Test
    fun linearFunctionTest_Reverse() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
                feature T: ScalarValues::Real;
                feature p: Ranges::RealInRange = linearInterpolation(T, 2000.0, 10.0, 2010.0, 20.0) {:>> range = 15.0;}
                """)
        solver.propagate()
        assertNoIssues()
        val t = solver.getVariable("T")
        assertTrue(2005.0 in t?.vectorQuantity?.aadd()?.getRange()!!)
        assertTrue(t.min<Double>() > 2004.9)
        assertTrue(t.vectorQuantity.getMaxAsDouble() < 2005.1)
    }

    @Test
    fun linearFunctionTest7_1() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 2015.0;
            feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 10.0, 2010.0, 20.0, 2020.0, 0.0);
        """, Runlevel.ALL)
        assertNoIssues()
        val p = solver.getVariable("p")
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
        val p = solver.getVariable("p")
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
        val p = solver.getVariable("p")
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
        val p = solver.getVariable("p")
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
        val p = solver.getVariable("p")
        assertEquals(p!!.min(), 20.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 20.0, 0.000001)
    }

    // y = y0 and y1 > y0 and y2 < y0
    @Test
    fun linearFunctionTest7_6() = testSession("ScalarValues") {
        loadKerML("""
                feature T: ScalarValues::Real = 1995.0.
                feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 20.0, 2010.0, 30.0, 2020.0, 10.0).
        """, Runlevel.ALL)
        assertNoIssues()
        val p = solver.getVariable("p")
        assertEquals(p!!.min(), 20.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 20.0, 0.000001)
    }

    // y = y2 and y1 > y2 and y0 > y2
    @Test
    fun linearFunctionTest7_7() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 2020.0.
            feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 30.0, 2010.0, 20.0, 2020.0, 10.0).
        """, Runlevel.ALL)
        assertNoIssues()
        val p = solver.getVariable("p")
        assertEquals(p!!.min(), 10.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 10.0, 0.000001)
    }

    // y = y2 and y1 > y2 and y0 < y2
    @Test
    fun linearFunctionTest7_8() = testSession("ScalarValues") {
        loadKerML("""
            feature T: ScalarValues::Real = 2025.0;
            feature p: ScalarValues::Real = linearInterpolation(T, 2000.0, 0.0, 2010.0, 20.0, 2020.0, 10.0);
        """, Runlevel.ALL)
        assertNoIssues()
        val p = solver.getVariable("p")
        assertEquals(p!!.min(), 10.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 10.0, 0.000001)
    }

    @Test
    fun linearFunctionTest7_8_reverse() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature T: ScalarValues::Real;
            feature p: Ranges::RealInRange = linearInterpolation(T, 2000.0, 0.0, 2010.0, 20.0, 2020.0, 10.0) {:>> range = 10.0;}
        """, Runlevel.ALL)
        assertNoIssues()
        val p = solver.getVariable("T")
        assertEquals(p!!.min(), 2005.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), Double.MAX_VALUE, 0.000001)
    }

    @Test
    fun linearFunctionTest7_9_reverse() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature T: ScalarValues::Real;
            feature p: Ranges::RealInRange = linearInterpolation(T, 2000.0, 0.0, 2010.0, 20.0, 2020.0, 0.0) {:>> range = 10.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val p = solver.getVariable("T")
        assertEquals(p!!.min(), 2005.0, 0.000001)
        assertEquals(p.vectorQuantity.getMaxAsDouble(), 2015.0, 0.000001)
    }

    @Test
    fun linearRangeRealTest() = testSession("ISQ") {
        loadKerML("""
            feature DataRate: ISQ::BitRateValue = linearInterpolation(Month("2025-01"), Month("2021-01"), [20.0 .. 80.0] [MB/s], Month("2030-01"), [100.0 .. 800.0] [MB/s]).
        """, Runlevel.ALL)
        assertNoIssues()
        val t = solver.getVariable("DataRate")
        assertEquals(28.889564952844047, t!!.vectorQuantity.valuesIn("MB/s")[0].asAadd().getRange().min, tol)
        assertEquals(426.69303316094675, t.vectorQuantity.valuesIn("MB/s")[0].asAadd().getRange().max, tol)
    }

    @Test
    fun linearInterpolation9ParamsTest() = testSession("Ranges", runlevel = Runlevel.VARIANCE_CHECKED) {
        loadKerML("""
            feature T: Ranges::RealInRange {:>> range = 1.5;}
            feature p: ScalarValues::Real = linearInterpolation(T, 0.0, 10.0, 1.0, 20.0, 2.0, 30.0, 3.0, 40.0);
        """, Runlevel.ALL)
        assertEquals(25.0, solver.getVariable("p")!!.min(), tol)
        assertEquals(25.0, solver.getVariable("p")!!.max(), tol)
    }
}
