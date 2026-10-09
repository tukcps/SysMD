package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.*
import util.mockup.loadKerML
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Ignore
import kotlin.test.Test

class TrigTests {

    @Test
    fun sinTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 1.57079632679;}
            feature b: ScalarValues::Real = sin(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(0.0 .. 1.0, b)
    }

    @Test
    fun cosTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 1.5707963267948966;}
            feature b: ScalarValues::Real = cos(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(0.0 .. 1.0, b)
    }

    @Test
    fun sinEvalDown() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.1 .. 3.0;}
            // sin(0.5) is ~0.4794255386
            feature b: Ranges::RealInRange = sin(a) {:>> range = 0.479425538604203;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        // sin(x) = 0.4794 has two solutions in [0.1, 3.0]: 0.5 and PI - 0.5
        assertBounds(0.5 .. 2.6415926535, a)
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
        assertBounds(0.479425538604203, solver.variable("b").vectorQuantity.value.asAadd())
        assertBounds(0.8775825618903725, solver.variable("c").vectorQuantity.value.asAadd())
    }

    @Test
    fun sinCosTest2() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1.0;}
            feature b: ScalarValues::Real = sin(a);
            feature c: ScalarValues::Real = cos(a);""")
        solver.propagate()
        assertNoIssues()
        assertBounds(0.8414709848078965, solver.variable("b").vectorQuantity.value.asAadd())
        assertBounds(0.5403023058681394, solver.variable("c").vectorQuantity.value.asAadd())
    }

    @Test
    fun sinCosTest3() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = 0.5 .. 1.0;}
            feature b: ScalarValues::Real = sin(a);
            feature c: ScalarValues::Real = cos(a); """)
        solver.propagate()
        assertNoIssues()
        assertBounds(sin(0.5) ..sin(1.0), solver.variable("b"))
        assertBounds(cos(1.0) .. cos(0.5), solver.variable("c"))
    }

    @Test
    fun sinCosTestNegative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -1.0 .. -0.5;}
            feature b: ScalarValues::Real = sin(a);
            feature c: ScalarValues::Real = cos(a); """)
        solver.propagate()
        assertNoIssues()
        assertBounds(cos(-1.0) ..cos(-0.5), solver.variable("c"))
    }

    @Test
    fun sinCosTestMixed() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -1.0 .. 1.0;}
            feature b: ScalarValues::Real = sin(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(sin(-1.0) ..sin(1.0), b)
    }

    @Test
    fun sinTestQuadrantSpanning() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 3.14159265359;}
            feature b: ScalarValues::Real = sin(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(0.0 .. 1.0, b)
    }

    @Test
    fun sinTestFullCircle() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 0.0 .. 6.28318530718;}
            feature b: ScalarValues::Real = sin(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(-1.0 .. 1.0, b)
    }

    /**
     * Regression test for sin evalDown in second quadrant [pi/2, 3pi/2].
     * For a in [1.6 .. 3.0], sin(2.5) ~ 0.598472.
     * With a in [1.6 .. 3.0] strictly in second quadrant, sin(a) in [0.5984721 .. 0.5984722] constrains a to 2.5.
     */
    @Test
    fun sinEvalDownSecondQuadrantRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1.6 .. 3.0;}
            feature b: Ranges::RealInRange = sin(a) {:>> range = 0.5984721441039565;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        assertBounds(2.5, a)
    }

    /**
     * Regression test for cos evalDown: verifies constrained range on input angle.
     * acos(0.5) = pi/3 ~ 1.04719755.
     */
    @Test
    fun cosEvalDownRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature angle: Ranges::RealInRange {:>> range = 0.0 .. 1.57079632679;}
            feature result: Ranges::RealInRange = cos(angle) {:>> range = 0.5 .. 0.5;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val angle = solver.variable("angle")
        assertBounds(1.047197551196598, angle)
    }
}
