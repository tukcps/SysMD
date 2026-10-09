package constraintnettests.functionstests


import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ToIntegerTests {
    @Test
    fun toInteger_evalUp() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange  = 3.0 {:>> range = 1.0 .. 5.0;}
            feature b: Ranges::IntegerInRange = ToInteger(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(3L .. 3L, solver.variable("b"))
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }

    @Test
    fun toInteger_evalUp2() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange  {:>> range = 1.5 .. 5.5;}
            feature b: Ranges::IntegerInRange = ToInteger(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(1L .. 6L, solver.variable("b"))
        assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
    }

    @Test
    fun toInteger_evalDown() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1.5 .. 5.5;}
            feature b: Ranges::IntegerInRange = ToInteger(a) {:>> range = 1 .. 4;} """
        )
        solver.propagate()
        assertNoIssues()
        assertBounds(1.5 .. 4.0, solver.variable("a"))
        assertEquals("1", solver.variable("a").vectorQuantity.unit.toString())
    }


        @Test
        fun integerFunctionTest() = testSession("ScalarValues") {
            loadKerML("""
                private import ScalarValues::*; 
                feature i: Real = [2.0 .. 3.0]; 
                feature r: Integer = ToInteger(i); 
                """)
            solver.propagate()
            val i = solver.variable("i")
            val r = solver.variable("r")
            assertNoIssues()
            assertBounds(2L .. 3L, r)
        }

    @Test
    fun toInteger_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -2.5 .. -1.5;}
            feature b: Ranges::IntegerInRange = ToInteger(a);
        """)
        solver.propagate()
        assertNoIssues()
        // floor(-2.5) is -3, ceil(-1.5) is -1
        assertBounds(-3L .. -1L, solver.variable("b"))
    }
}
