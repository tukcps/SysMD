package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.math.sin
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class TrigTests {

    @Test
    fun sinTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 1.57079632679;}
            feature b: ScalarValues::Real = sin(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.getVariable("b")!!
        assertEquals(0.0, b.min(), 0.0001)
        assertEquals(1.0, b.max(), 0.0001)
    }

    @Test
    fun cosTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 1.57079632679;}
            feature b: ScalarValues::Real = cos(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.getVariable("b")!!
        assertEquals(0.0, b.min(), 0.0001)
        assertEquals(1.0, b.max(), 0.0001)
    }

    @Test
    fun sinEvalDown() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.1 .. 3.0;}
            // sin(0.5) is ~0.4794255386
            feature b: Ranges::RealInRange = sin(a) {:>> range = 0.4794255 .. 0.4794256;}
        """, Runlevel.ALL)
        assertNoIssues()
        val a = solver.getVariable("a")!!
        assertEquals(0.5, a.min(), 0.001)
        assertEquals(0.5, a.max(), 0.001)
    }

    @Test
    fun sinCosTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = 0.5 .. 0.5;}
            feature b: ScalarValues::Real = sin(a);
            feature c: ScalarValues::Real = cos(a);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.479425538604203, solver.getVariable("b")!!.vectorQuantity.value.asAadd().min, 0.0001)
        assertEquals(0.8775825618903725, solver.getVariable("c")!!.vectorQuantity.value.asAadd().min, 0.0001)
    }

    @Test
    fun sinCosTest2() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1.0;}
            feature b: ScalarValues::Real = sin(a);
            feature c: ScalarValues::Real = cos(a);""")
        solver.propagate()
        assertNoIssues()
        assertEquals(0.8414709848078965, solver.getVariable("b")!!.vectorQuantity.value.asAadd().min, 0.0001)
        assertEquals(0.5403023058681394, solver.getVariable("c")!!.vectorQuantity.value.asAadd().min, 0.0001)
    }

    @Test
    fun sinCosTest3() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = 0.5 .. 1.0;}
            feature b: ScalarValues::Real = sin(a);
            feature c: ScalarValues::Real = cos(a); """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.479425538604203, solver.getVariable("b")!!.vectorQuantity.value.asAadd().min, 0.0001)
        assertEquals(0.8414709848078965, solver.getVariable("b")!!.vectorQuantity.value.asAadd().max, 0.0001)
        assertEquals(0.5403023058681394, solver.getVariable("c")!!.vectorQuantity.value.asAadd().min, 0.0001)
        assertEquals(0.877582561890373, solver.getVariable("c")!!.vectorQuantity.value.asAadd().max, 0.0001)
    }

    @Test
    fun sinCosTestNegative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -1.0 .. -0.5;}
            feature b: ScalarValues::Real = sin(a);
            feature c: ScalarValues::Real = cos(a); """)
        solver.propagate()
        assertNoIssues()
        assertEquals(kotlin.math.cos(-1.0), solver.getVariable("c")!!.vectorQuantity.value.asAadd().min, 0.0001)
        assertEquals(kotlin.math.cos(-0.5), solver.getVariable("c")!!.vectorQuantity.value.asAadd().max, 0.0001)
    }

    @Test
    fun sinCosTestMixed() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -1.0 .. 1.0;}
            feature b: ScalarValues::Real = sin(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.getVariable("b")!!
        assertEquals(sin(-1.0), b.min(), 0.0001)
        assertEquals(sin(1.0), b.max(), 0.0001)
    }

    @Ignore // Solver limitation: AADD's sin implementation defaults to [-1, 1] on non-monotonic intervals.
    @Test
    fun sinTestQuadrantSpanning() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 3.14159265359;}
            feature b: ScalarValues::Real = sin(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.getVariable("b")!!
        assertEquals(0.0, b.min(), 0.0001)
        assertEquals(1.0, b.max(), 0.0001)
    }

    @Test
    fun sinTestFullCircle() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 6.28318530718;}
            feature b: ScalarValues::Real = sin(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.getVariable("b")!!
        assertEquals(-1.0, b.min(), 0.0001)
        assertEquals(1.0, b.max(), 0.0001)
    }
}
