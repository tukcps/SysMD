package constraintnettests.functionstests


import com.github.tukcps.sysmd.services.resolve.resolveVar
import kotlin.test.assertEquals
import kotlin.test.Test
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class ToIntegerTests {
    @Test
    fun toInteger_evalUp() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange  = 3.0 {:>> range = "1.0 .. 5.0";}
            feature b: Ranges::IntegerInRange = ToInteger(a);"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(3L, global.resolveVar("b")!!.min())
        assertEquals(3L, global.resolveVar("b")!!.max())
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    @Test
    fun toInteger_evalUp2() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange  {:>> range = "1.5 .. 5.5";}
            feature b: Ranges::IntegerInRange = ToInteger(a);"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1L, global.resolveVar("b")!!.min())
        assertEquals(6L, global.resolveVar("b")!!.max())
        assertEquals("1", global.resolveVar("b")!!.vectorQuantity.unit.toString())
    }

    @Test
    fun toInteger_evalDown() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "1.5 .. 5.5";}
            feature b: Ranges::IntegerInRange = ToInteger(a) {:>> range = "1 .. 4";} """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.5, global.resolveVar("a")!!.min(), 0.00001)
        assertEquals(4.0, global.resolveVar("a")!!.max(), 0.00001)
        assertEquals("1", global.resolveVar("a")!!.vectorQuantity.unit.toString())
    }


        @Test
        fun integerFunctionTest() = testSession("ScalarValues") {
            loadKerML("""
                private import ScalarValues::*; 
                feature i: Real = [2.0 .. 3.0]; 
                feature r: Integer = ToInteger(i); 
                """)
            solver.propagate()
            val i = solver.getVariable("i")
            assertNotNull(i)
            val r = solver.getVariable("r")!!
            assertNoIssues()
            assertEquals(2L, r.vectorQuantity.value.asIdd().min)
            assertEquals(3L, r.vectorQuantity.value.asIdd().max)
        }

    @Test
    fun toInteger_negative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "-2.5 .. -1.5";}
            feature b: Ranges::IntegerInRange = ToInteger(a);
        """)
        solver.propagate()
        assertNoIssues()
        // floor(-2.5) is -3, ceil(-1.5) is -1
        assertEquals(-3L, global.resolveVar("b")!!.min())
        assertEquals(-1L, global.resolveVar("b")!!.max())
    }
}
