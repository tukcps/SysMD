package constraintnettests

import util.variable
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.util.Assertions.assertEquals
import io.github.tukcps.aadd.values.bounds.DoubleBound
import util.assertIssue
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class AllOnePropagationTests {

    @Test 
    fun allOnePropagationTestReal() = testSession("Ranges") {
        loadKerML("""
            // a is a Real from 1..2, and is assigned a value from 1.2 to 2.5
            feature all a: Ranges::RealInRange = oneOf(1.5 .. 2.5) {:>> range = 1 .. 2;}
            feature b: Ranges::RealInRange = oneOf(1.5 .. 2.5) {:>> range = 1 .. 2;}
        """)
        solver.propagate()
        val a = solver.variable("a")
        val b = solver.variable("b")
        assertBounds(1.0 .. 2.0, a)
        assertBounds(1.5 .. 2.0, b)
        assertIssue("cannot be satisfied for all")
    }

    @Test 
    fun allOnePropagationTestInt() = testSession("Ranges") {
        loadKerML("""
            // Contradiction ...         
            feature all a: Ranges::IntegerInRange = oneOf(5 .. 15) {:>> range = 1 .. 10;}
            feature b: Ranges::IntegerInRange = oneOf(5 .. 15) {:>> range = 1 .. 10;}
        """, Runlevel.ALL)
        val a = solver.variable("a")
        val b = solver.variable("b")
        assertBounds(1L .. 10L, a)
        assertBounds(5L .. 10L, b)
        assertIssue("be satisfied")
    }

    @Test 
    fun allOnePropagationTestRealNew() = testSession("Ranges") {
        loadKerML("""
                feature all a: Ranges::RealInRange { :>> range = 1.0 .. 10.0; }
        """)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        assertBounds(1.0 .. 10.0, a)
    }

    @Test 
    fun allOnePropagationTestIntNew() = testSession("Ranges") {
        loadKerML("""
            feature all a: Ranges::IntegerInRange = oneOf(5 .. 15) { :>> range = 1 .. 10;  }
            feature b: Ranges::IntegerInRange = oneOf(5 .. 15) {:>> range = 1 .. 10; }
        """)
        solver.propagate()
        assertEquals(Issue.Kind.WARN_INCONSISTENCY, status.issues.firstOrNull()?.kind,
            "Insatisfiability for all shall be reported")
        val a = solver.variable("a")
        val b = solver.variable("b")
        assertBounds(1L .. 10L, a)
        assertBounds(5L .. 10L, b)
        assertEquals(1, status.issues.size, status.issues.toString())
    }

    @Ignore // Compiler limitation: Unary minus on range bounds (e.g. oneOf(-2.5 .. -1.5)) is parsed as an expression and not constant-folded at compile-time.
    @Test 
    fun allOnePropagationTestRealNegative() = testSession("Ranges") {
        loadKerML("""
            // a is a Real from -2..-1, and is assigned a value from -2.5 to -1.5
            feature all a: Ranges::RealInRange = oneOf((-2.5) .. (-1.5)) {:>> range = "-2 .. -1";}
            feature b: Ranges::RealInRange = oneOf((-2.5) .. (-1.5)) {:>> range = "-2 .. -1";}
        """)
        solver.propagate()
        val a = solver.variable("a")
        val b = solver.variable("b")
        println("allOneRealNegative: a min=${a.min<DoubleBound>()}, a max=${a.max<DoubleBound>()}, b min=${b.min<DoubleBound>()}, max=${b.max<DoubleBound>()}")
        assertBounds(-2.0 .. -1.0, a)
        assertBounds(-2.0 .. -1.5, b)
        assertIssue("cannot be satisfied for all")
    }
}
