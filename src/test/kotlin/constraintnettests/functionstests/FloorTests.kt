package constraintnettests.functionstests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class FloorTests {


    @Test
    fun floor_real_negative() = testSession("Ranges")  {
        loadKerML("""
          feature a: Ranges::RealInRange {:>> range = -3.5..-2.5;}
          feature b: ScalarValues::Real = floor(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(-4.0 .. -3.0, solver.variable("b"))
    }

    @Test
    fun floor_zero() = testSession("Ranges") {
        loadKerML("""
          feature a: Ranges::IntegerInRange {:>> range = 0;}
          feature b: ScalarValues::Integer = floor(a);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0L .. 0L, solver.variable("b"))
    }

    @Test
    fun floor_evalDown() = testSession("Ranges") {
        loadKerML("""
          feature a: Ranges::IntegerInRange {:>> range = 3..7;}
          feature b: Ranges::IntegerInRange = floor(a) {:>> range = 3..3;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(3L .. 3L, solver.variable("a"))
    }

    @Test
    fun floor_real_evalDown() = testSession("Ranges")  {
        loadKerML("""
          feature a: Ranges::RealInRange {:>> range = 3.5..6.5;}
          feature b: Ranges::RealInRange = floor(a) {:>> range = 3.0..5.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(3.5 .. 6.0, solver.variable("a"))
    }

    /**
     * Test of Floor.
     */
    @Test
    fun testFloorFxnA() {
        testSession("ScalarValues", "Ranges", runlevel = Runlevel.VARIANCE_CHECKED) {
            loadKerML("feature p: Ranges::RealInRange {:>> range = 1.0 .. 1.0;}") // padding, 0 = NOT enabled, 1 = enabled
            loadKerML("feature C_wb_s_floor_arg: Ranges::RealInRange {:>> range = 2.75 .. 2.75;}")
            loadKerML("feature a_pb: ScalarValues::Real = p * floor(C_wb_s_floor_arg);")
            assertBounds(2.0 .. 2.0, solver.variable("a_pb"))
        }
    }

    /**
     * Test of Floor.
     */
    @Test
    fun testFloorFxnB() = testSession("ScalarValues", "Ranges", runlevel = Runlevel.VARIANCE_CHECKED) {
        loadKerML("feature C_wb_s_floor_arg: Ranges::RealInRange {:>> range = 2.75;}")
        loadKerML("feature a_pb: ScalarValues::Real = floor(C_wb_s_floor_arg);")
        assertBounds(2.0 .. 2.0, solver.variable("a_pb"))
    }

    /**
     * Test of Floor.
     */
    @Test
    fun testFloorFxnC() = testSession("ScalarValues", "Ranges", runlevel = Runlevel.VARIANCE_CHECKED) {
        loadKerML("feature C_wb_s_floor_arg: Ranges::RealInRange {:>> range = 2.75;} feature a_pb: ScalarValues::Real = floor(C_wb_s_floor_arg) - 1.0.")
        assertBounds(1.0 .. 1.0, solver.variable("a_pb"))
    }


    /**
     * Test of Floor.
     */
    @Test
    fun testFloorFxnD() {
        testSession("ScalarValues", "Ranges", runlevel = Runlevel.VARIANCE_CHECKED) {
            loadKerML("feature C_wb_s_floor_arg: Ranges::RealInRange {:>> range = 2.75;}")
            loadKerML("feature a_pb: ScalarValues::Real = 3.0 - floor(C_wb_s_floor_arg);")
            assertBounds(1.0 .. 1.0, solver.variable("a_pb"))
        }
    }
    @Test
        fun floorTest_mixed_range() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::RealInRange {:>> range = -1.5 .. 1.5;}
                feature a: ScalarValues::Real = floor(qa);
            """)
            solver.propagate()
            assertNoIssues()
            assertBounds(-2.0 .. 1.0, solver.variable("a"))
        }

        /** Floor on ISQ LengthValue, check unit preserved */
        @Test
        fun floor_quantity() = testSession("ISQ") {
            loadKerML("""
                feature a: ISQ::LengthValue(1.5..4.5 [m]);  
                feature b: ISQ::LengthValue = floor(a);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertEquals("m", solver.variable("b").vectorQuantity.unit.toString())
            assertBounds(1.0 .. 4.0, solver.variable("b"))
        }

        /** Floor on Integer with oneOf */
        @Test
        fun floor_quantity_int() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Integer = oneOf(1..3);
                feature b: ScalarValues::Integer = floor(a); 
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(1L .. 3L, solver.variable("b"))
        }

        /** Floor on negative integer range */
        @Test
        fun floor_quantity_int1() = testSession("Ranges") {
            loadKerML("""
              feature a: Ranges::IntegerInRange {:>> range = -3..0;}
              feature b: ScalarValues::Integer = floor(a);
            """)
            solver.propagate()
            assertBounds(-3L .. 0L, solver.variable("b"))
            assertNoIssues()
        }

        @Test
        fun floorTest_integer_mixed() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = -3 .. 3;}
                feature a: ScalarValues::Integer = floor(qa);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(-3L .. 3L, solver.variable("a"))
        }

        /** floor rounds in the displayed unit, also for the propagation down to the parameter */
        @Test
        fun floor_displayedUnit() = testSession("ISQ") {
            loadKerML("""
                feature a: ISQ::LengthValue(100..200 [cm]);
                feature b: ISQ::LengthValue(150..150 [cm]) = floor(a);
                feature c: ISQ::LengthValue(150.5..150.5 [cm]);
                feature d: ISQ::LengthValue(* [cm]) = floor(c);
                feature t: ISQ::ThermodynamicTemperatureValue(0..100 [°C]);
                feature u: ISQ::ThermodynamicTemperatureValue(20..21 [°C]) = floor(t);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(150.0 .. 151.0, solver.variable("a"), unit = "cm")
            assertBounds(150.0 .. 150.0, solver.variable("b"), unit = "cm")
            assertBounds(150.0 .. 150.0, solver.variable("d"), unit = "cm")
            assertBounds(20.0 .. 22.0, solver.variable("t"), unit = "°C")
            assertBounds(20.0 .. 21.0, solver.variable("u"), unit = "°C")
        }
}
