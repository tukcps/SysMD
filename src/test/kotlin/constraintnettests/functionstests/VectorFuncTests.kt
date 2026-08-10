package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
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
        assertNoIssues()
        val b = solver.getVariable("b")!!
        assertEquals(3L, b.min())
        assertEquals(3L, b.max())
    }

    @Test
    fun normTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = (3.0..3.0, 4.0..4.0);}
            feature b: ScalarValues::Real = norm(a);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.getVariable("b")!!
        assertEquals(0.6, b.min(), 0.0001)
        assertEquals(0.6, b.max(), 0.0001)
        assertEquals(0.8, b.min(1), 0.0001)
        assertEquals(0.8, b.max(1), 0.0001)
    }

    @Test
    fun quantityOfVectorAtPositionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = (10.0..10.0, 20.0..20.0, 30.0..30.0);}
            feature b: ScalarValues::Real = quantityOfVectorAtPosition(a, 1);
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.getVariable("b")!!
        assertEquals(20.0, b.min(), 0.0001)
        assertEquals(20.0, b.max(), 0.0001)
    }
}
