package constraintnettests.functionstests

import util.variable
import util.assertBounds
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for unit conversions and string representation of quantities.
 * These tests verify that the solver correctly handles value/unit conversion
 * when quantities are assigned across different unit specifications.
 */
class UnitConversionTests {

    @Test
    fun conversionTest1() = testSession("ISQ") {
        loadKerML("""
            feature t1: Quantities::ScalarQuantityValue = 1.0 [h^2] {:>> range = (*..*) [h^2];}
            feature t2: Quantities::ScalarQuantityValue = t1 {:>> range = (*..*) [min^2];}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(3600.0, solver.variable("t2"), unit = "min^2")
    }

    @Test
    fun conversionTest2() = testSession("ISQ") {
        loadKerML("""
            feature t1: ISQ::AccelerationValue = 1.0 [km/min^2];
            feature t2: ISQ::AccelerationValue  = t1;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.2777777777777778, solver.variable("t2"))
    }

    @Test
    fun conversionTest3() = testSession("ISQ") {
        loadKerML("""
            feature t1: Quantities::ScalarQuantityValue  = 1.0 [N/m^2] { :>> range = (*..*) [N/m^2];}
            feature t2: Quantities::ScalarQuantityValue = t1 { :>> range = (* ..*) [mN/dm^2];}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(10.0, solver.variable("t2"), unit = "mN/dm^2")
    }

    @Test
    fun stringToStringTest() = testSession("ScalarValues") {
        loadKerML("""
            feature name: ScalarValues::String = "Hallo";
        """, Runlevel.ALL)
        assertEquals("Hallo", solver.variable("name").vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun unitConversationTest() = testSession("ISQ", "Ranges") {
        loadKerML("""
            feature t: ISQ::AccelerationValue{ :>> range = -9.81 [m/s^2];}
            feature s: ISQ::AccelerationValue = t;
        """, Runlevel.ALL)
        assertEquals("-9.81 m/s^2", solver.variable("s").vectorQuantity.toString())
        assertNoIssues()
    }
}
