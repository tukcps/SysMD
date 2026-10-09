package constraintnettests.functionstests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class VectorFuncTests {

    @Test
    fun sizeTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = (0..6, 6..12, 4..20);}
            feature b: ScalarValues::Integer = size(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(3L .. 3L, b)
    }

    @Test
    fun normTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = (3.0..3.0, 4.0..4.0);}
            feature b: ScalarValues::Real = norm(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(listOf(0.6 .. 0.6, 0.8 .. 0.8), b)
    }

    @Test
    fun quantityOfVectorAtPositionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = (10.0..10.0, 20.0..20.0, 30.0..30.0);}
            feature b: ScalarValues::Real = quantityOfVectorAtPosition(a, 1);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(20.0 .. 20.0, b)
    }
}
