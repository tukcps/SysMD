package constraintnettests.functionstests

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.IDD
import kotlin.test.Test
import kotlin.test.Ignore
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.math.*
import kotlin.test.*

class CeilTests {

        @Test
        fun ceilTest_real() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::RealInRange {:>> range = "3.1 .. 3.1";}
                feature a: ScalarValues::Real = ceil(qa);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(4.0, solver.getVariable("a")!!.min(), 0.00001)
            assertEquals(4.0, solver.getVariable("a")!!.max(), 0.00001)
        }

        @Test
        fun ceilTest_real_range() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::RealInRange {:>> range = "3.1 .. 5.5";}
                feature a: ScalarValues::Real = ceil(qa);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(4.0, solver.getVariable("a")!!.min(), 0.00001)
            assertEquals(6.0, solver.getVariable("a")!!.max(), 0.00001)
        }

        @Test
        fun ceilTest_integer() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = "3 .. 3";}
                feature a: ScalarValues::Integer = ceil(qa);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(3, solver.getVariable("a")!!.idd().getRange().min)
            assertEquals(3, solver.getVariable("a")!!.idd().getRange().max)
        }

        @Test
        fun ceilTest_integer_negative() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = "-5 .. -3";}
                feature a: ScalarValues::Integer = ceil(qa);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(-4L, solver.getVariable("a")!!.min())
            assertEquals(-3L, solver.getVariable("a")!!.max())
        }

        @Test
        fun ceilTest_integer_range() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = "3 .. 5";}
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
                feature qa: Ranges::IntegerInRange {:>> range = " 5 ..  10";}
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
                feature qa: Ranges::RealInRange {:>> range = "-7.3 .. -4.5";}
                feature a: ScalarValues::Real = ceil(qa);
                """)
            solver.propagate()
            assertNoIssues()
            assertEquals(-7.0, solver.getVariable("a")!!.min(), 0.00001)
            assertEquals(-4.0, solver.getVariable("a")!!.max(), 0.00001)
        }

        @Test
        fun ceilTest_mixed_range() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::RealInRange {:>> range = "-1.5 .. 1.5";}
                feature a: ScalarValues::Real = ceil(qa);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(-1.0, solver.getVariable("a")!!.min(), 0.00001)
            assertEquals(2.0, solver.getVariable("a")!!.max(), 0.00001)
        }

        /** Ceil on ISQ LengthValue, check unit preserved */
        @Test
        fun ceil_quantity() = testSession("ISQ") {
            loadKerML("""
                feature a: ISQ::LengthValue = 1.5 [m];
                feature b: ISQ::LengthValue = ceil(a); 
            """)
            solver.propagate()
            assertEquals("m", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertEquals(2.0, solver.getVariable("b")!!.min(), 0.0001)
            assertEquals(2.0, solver.getVariable("b")!!.max(), 0.0001)
            assertNoIssues()
        }

        /** Ceil on Integer range */
        @Test
        fun ceil_quantity_int() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Integer = [1..3]; 
                feature b: ScalarValues::Integer = ceil(a); 
            """)
            solver.propagate()
            assertEquals(2, solver.getVariable("b")!!.vectorQuantity.value.asIdd().min)
            assertEquals(3, solver.getVariable("b")!!.vectorQuantity.value.asIdd().max)
            assertNoIssues()
        }

        @Test
        fun ceilTest_integer_mixed() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = "-3 .. 3";}
                feature a: ScalarValues::Integer = ceil(qa);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(-2L, solver.getVariable("a")!!.min())
            assertEquals(3L, solver.getVariable("a")!!.max())
        }

        @Test
        fun ceilTest_real_zero() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::RealInRange {:>> range = "0.0 .. 0.0";}
                feature a: ScalarValues::Real = ceil(qa);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(0.0, solver.getVariable("a")!!.min(), 0.00001)
            assertEquals(0.0, solver.getVariable("a")!!.max(), 0.00001)
        }
}
