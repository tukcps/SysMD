package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class FloorTests {

    val tol = 0.0001

    @Test
    fun floor_real_negative() = testSession("Ranges")  {
        loadKerML("""
          feature a: Ranges::RealInRange {:>> range = -3.5..-2.5;}
          feature b: ScalarValues::Real = floor(a);
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(-4.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(-3.0, solver.getVariable("b")!!.max(), 0.00001)
    }

    @Test
    fun floor_zero() = testSession("Ranges") {
        loadKerML("""
          feature a: Ranges::IntegerInRange {:>> range = 0;}
          feature b: ScalarValues::Integer = floor(a);
        """, Runlevel.ALL)
        assertEquals(0L, solver.getVariable("b")!!.min())
        assertEquals(0L, solver.getVariable("b")!!.max())
        assertNoIssues()
    }

    @Test
    fun floor_evalDown() = testSession("Ranges") {
        loadKerML("""
          feature a: Ranges::IntegerInRange {:>> range = 2..7;}
          feature b: Ranges::IntegerInRange = floor(a) {:>> range = 3..5;}
        """, Runlevel.ALL)
        assertEquals(3L, solver.getVariable("a")!!.min())
        assertEquals(6L, solver.getVariable("a")!!.max())
        assertNoIssues()
    }

    @Test
    fun floor_real_evalDown() = testSession("Ranges")  {
        loadKerML("""
          feature a: Ranges::RealInRange {:>> range = 3.5..6.5;}
          feature b: Ranges::RealInRange = floor(a) {:>> range = 3.0..5.0;}
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(3.5, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(6.0, solver.getVariable("a")!!.max(), 0.00001)
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
            assertEquals(2.0, solver.getVariable("a_pb")!!.min(), tol)
            assertEquals(2.0, solver.getVariable("a_pb")!!.max(), tol)
        }
    }

    /**
     * Test of Floor.
     */
    @Test
    fun testFloorFxnB() = testSession("ScalarValues", "Ranges", runlevel = Runlevel.VARIANCE_CHECKED) {
        loadKerML("feature C_wb_s_floor_arg: Ranges::RealInRange {:>> range = 2.75;}")
        loadKerML("feature a_pb: ScalarValues::Real = floor(C_wb_s_floor_arg);")
        assertEquals(2.0, solver.getVariable("a_pb")!!.min(), tol)
        assertEquals(2.0, solver.getVariable("a_pb")!!.max(), tol)
    }

    /**
     * Test of Floor.
     */
    @Test
    fun testFloorFxnC() = testSession("ScalarValues", "Ranges", runlevel = Runlevel.VARIANCE_CHECKED) {
        loadKerML("feature C_wb_s_floor_arg: Ranges::RealInRange {:>> range = 2.75;} feature a_pb: ScalarValues::Real = floor(C_wb_s_floor_arg) - 1.0.")
        assertEquals(1.0, solver.getVariable("a_pb")!!.min(), 0.001)
        assertEquals(1.0, solver.getVariable("a_pb")!!.max(), 0.001)
    }


    /**
     * Test of Floor.
     */
    @Test
    fun testFloorFxnD() {
        testSession("ScalarValues", "Ranges", runlevel = Runlevel.VARIANCE_CHECKED) {
            loadKerML("feature C_wb_s_floor_arg: Ranges::RealInRange {:>> range = 2.75;}")
            loadKerML("feature a_pb: ScalarValues::Real = 3.0 - floor(C_wb_s_floor_arg);")
            assertEquals(1.0, solver.getVariable("a_pb")!!.min(), tol)
            assertEquals(1.0, solver.getVariable("a_pb")!!.max(), tol)
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
            assertEquals(-2.0, solver.getVariable("a")!!.min(), 0.00001)
            assertEquals(1.0, solver.getVariable("a")!!.max(), 0.00001)
        }

        /** Floor on ISQ LengthValue, check unit preserved */
        @Test
        fun floor_quantity() = testSession("ISQ") {
            loadKerML("""
                feature a: ISQ::LengthValue(1.5..4.5 [m]);  
                feature b: ISQ::LengthValue = floor(a);
            """, Runlevel.ALL)
            assertEquals("m", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertEquals(1.0, solver.getVariable("b")!!.min(), 0.0001)
            assertEquals(4.0, solver.getVariable("b")!!.max(), 0.0001)
            assertNoIssues()
        }

        /** Floor on Integer with oneOf */
        @Test
        fun floor_quantity_int() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Integer = oneOf(1..3);
                feature b: ScalarValues::Integer = floor(a); 
            """, Runlevel.ALL)
            assertEquals(1, solver.getVariable("b")!!.vectorQuantity.value.asIdd().min)
            assertEquals(2, solver.getVariable("b")!!.vectorQuantity.value.asIdd().max)
            assertNoIssues()
        }

        /** Floor on negative integer range */
        @Test
        fun floor_quantity_int1() = testSession("Ranges") {
            loadKerML("""
              feature a: Ranges::IntegerInRange {:>> range = -3..0;}
              feature b: ScalarValues::Integer = floor(a);
            """)
            solver.propagate()
            assertEquals(-3, solver.getVariable("b")!!.vectorQuantity.value.asIdd().min)
            assertEquals(-1, solver.getVariable("b")!!.vectorQuantity.value.asIdd().max)
            assertNoIssues()
        }

        @Test
        fun floorTest_integer_mixed() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = -3 .. 3;}
                feature a: ScalarValues::Integer = floor(qa);
            """, Runlevel.ALL)
            assertNoIssues()
            assertEquals(-3L, solver.getVariable("a")!!.min())
            assertEquals(2L, solver.getVariable("a")!!.max())
        }
}
