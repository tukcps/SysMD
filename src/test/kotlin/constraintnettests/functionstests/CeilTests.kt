package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class CeilTests {
    @Test
    fun ceilTest_real() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::RealInRange {:>> range = 3.1 .. 3.1;}
            feature a: ScalarValues::Real = ceil(qa);
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(4.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(4.0, solver.getVariable("a")!!.max(), 0.00001)
    }
    @Test
    fun ceilTest_real_range() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::RealInRange {:>> range = 3.1 .. 5.5;}
            feature a: ScalarValues::Real = ceil(qa);
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(4.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(6.0, solver.getVariable("a")!!.max(), 0.00001)
    }

    @Test
    fun ceilTest_integer() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = 3 .. 3;}
            feature a: ScalarValues::Integer = ceil(qa);
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(3, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(3, solver.getVariable("a")!!.idd().getRange().max)
    }

    @Test
    fun ceilTest_integer_negative() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = -5 .. -3;}
            feature a: ScalarValues::Integer = ceil(qa);
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(-4L, solver.getVariable("a")!!.min())
        assertEquals(-3L, solver.getVariable("a")!!.max())
    }

    @Test
    fun ceilTest_integer_range() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = 3 .. 5;}
            feature a: ScalarValues::Integer = ceil(qa);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(4, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(5, solver.getVariable("a")!!.idd().getRange().max)
    }

    @Test
    fun ceilTestEvalDown() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = 5 ..  10;}
            feature a:  Ranges::IntegerInRange = ceil(qa) 
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(6, solver.getVariable("a")!!.idd().getRange().min)
        assertEquals(10, solver.getVariable("a")!!.idd().getRange().max)
    }

    @Test
    fun ceilTest_real_negative() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::RealInRange {:>> range = -7.3 .. -4.5;}
            feature a: ScalarValues::Real = ceil(qa);
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(-7.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(-4.0, solver.getVariable("a")!!.max(), 0.00001)
    }
}
