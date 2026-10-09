package constraintnettests.functionstests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
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
        solver.propagate()
        assertNoIssues()
        assertBounds(4.0 .. 4.0, solver.variable("a"))
    }
    @Test
    fun ceilTest_real_range() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::RealInRange {:>> range = 3.1 .. 5.5;}
            feature a: ScalarValues::Real = ceil(qa);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(4.0 .. 6.0, solver.variable("a"))
    }

    @Test
    fun ceilTest_integer() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = 3 .. 3;}
            feature a: ScalarValues::Integer = ceil(qa);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(3L .. 3L, solver.variable("a"))
    }

    @Test
    fun ceilTest_integer_negative() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = -5 .. -3;}
            feature a: ScalarValues::Integer = ceil(qa);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(-5L .. -3L, solver.variable("a"))
    }

    @Test
    fun ceilTest_integer_range() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = 3 .. 5;}
            feature a: ScalarValues::Integer = ceil(qa);
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(3L .. 5L, solver.variable("a"))
    }

    @Test
    fun ceilTestEvalDown() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::IntegerInRange {:>> range = 5 ..  10;}
            feature a:  Ranges::IntegerInRange = ceil(qa) 
            """)
        solver.propagate()
        assertNoIssues()
        assertBounds(5L .. 10L, solver.variable("a"))
    }

    @Test
    fun ceilTest_real_negative() = testSession("Ranges") {
        loadKerML("""
            feature qa: Ranges::RealInRange {:>> range = -7.3 .. -4.5;}
            feature a: ScalarValues::Real = ceil(qa);
            """)
        solver.propagate()
        assertNoIssues()
        assertBounds(-7.0 .. -4.0, solver.variable("a"))
    }

    /** ceil rounds in the displayed unit, also for the propagation down to the parameter */
    @Test
    fun ceilTest_displayedUnit() = testSession("ISQ") {
        loadKerML("""
            feature a: ISQ::LengthValue(100..200 [cm]);
            feature b: ISQ::LengthValue(150..150 [cm]) = ceil(a);
            feature c: ISQ::LengthValue(150.5..150.5 [cm]);
            feature d: ISQ::LengthValue(* [cm]) = ceil(c);
            feature t: ISQ::ThermodynamicTemperatureValue(0..100 [°C]);
            feature u: ISQ::ThermodynamicTemperatureValue(20..21 [°C]) = ceil(t);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(149.0 .. 150.0, solver.variable("a"), unit = "cm")
        assertBounds(150.0 .. 150.0, solver.variable("b"), unit = "cm")
        assertBounds(151.0 .. 151.0, solver.variable("d"), unit = "cm")
        assertBounds(19.0 .. 21.0, solver.variable("t"), unit = "°C")
        assertBounds(20.0 .. 21.0, solver.variable("u"), unit = "°C")
    }
}
