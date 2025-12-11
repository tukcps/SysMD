package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.resolve.resolveVar
import org.junit.jupiter.api.Test
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.assertEquals

class FloorTests {

    @Test
    fun floor_real_negative() = testSession("Ranges")  {
        loadKerML("""
          feature a: Ranges::RealInRange {:>> range = "-3.5..-2.5";}
          feature b: ScalarValues::Real = floor(a);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(-4.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(-3.0, global.resolveVar("b")!!.max(), 0.00001)
    }

    @Test
    fun floor_zero() = testSession("Ranges") {
        loadKerML("""
          feature a: Ranges::IntegerInRange {:>> range = "0..0";}
          feature b: ScalarValues::Integer = floor(a);
        """)
        solver.propagate()
        assertEquals(0L, global.resolveVar("b")!!.min())
        assertEquals(0L, global.resolveVar("b")!!.max())
        assertNoIssues()
    }

    @Test
    fun floor_evalDown() = testSession("Ranges") {
        loadKerML("""
          feature a: Ranges::IntegerInRange {:>> range = "2..7";}
          feature b: Ranges::IntegerInRange = floor(a) {:>> range = "3..5";}
        """)
        solver.propagate()
        assertEquals(3L, global.resolveVar("a")!!.min())
        assertEquals(6L, global.resolveVar("a")!!.max())
        assertNoIssues()
    }

    @Test
    fun floor_real_evalDown() = testSession("Ranges")  {
        loadKerML("""
          feature a: Ranges::RealInRange {:>> range = "3.5..6.5";}
          feature b: Ranges::RealInRange = floor(a) {:>> range = "3.0..5.0";}
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(3.5, global.resolveVar("a")!!.min(), 0.00001)
        assertEquals(6.0, global.resolveVar("a")!!.max(), 0.00001)
    }
}