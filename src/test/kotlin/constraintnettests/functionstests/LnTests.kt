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

class LnTests {

        /** ConstNet shall compute bottom-up with ln in real and model.builder.range */
        @Test
        fun evalUpWithLog_real_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "1.0 .. 5.0";}
                feature b: ScalarValues::Real = ln(a);"""
            )
            solver.propagate()
            assertEquals(ln(1.0), solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(ln(5.0), solver.getVariable("b")!!.max(), 0.00001)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with ln in real and value */
        @Test
        fun evalUpWithLog_real_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange = 3.0 {:>> range = "1.0 .. 5.0";}
                feature b: ScalarValues::Real = ln(a);
            """)
            solver.propagate()
            assertEquals(ln(3.0), solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(ln(3.0), solver.getVariable("b")!!.max(), 0.00001)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with ln in real and negative numbers => not possible */
        @Test
        fun evalUpWithLog_real_negative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "-10.0 .. -5.0";}
                feature b: ScalarValues::Real = ln(a);
            """, Runlevel.ALL)
            val b = solver.getVariable("b")
            assertTrue(b!!.vectorQuantity.value.asAadd().isEmpty())
            assertEquals(1, status.issues.size)
        }

        /** ConstNet shall compute bottom-up with ln in int and model.builder.range */
        @Test
        fun evalUpWithLog_int_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1 .. 5";}
                feature b: ScalarValues::Integer = ln(a);""")
            solver.propagate()
            assertEquals(floor(ln(1.0)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(ceil(ln(5.0)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalDownWithLog_int() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1 .. 8";}
                feature b: Ranges::IntegerInRange = ln(a) {:>> range = "1 .. 2";}""")
            solver.propagate()
            assertEquals(floor(Math.E.pow(1)).toLong(), solver.getVariable("a")!!.idd().getRange().min)
            assertEquals(ceil(Math.E.pow(2)).toLong(), solver.getVariable("a")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithLog_int() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1 .. 1";}
                feature b: ScalarValues::Integer = ln(a);""")
            solver.propagate()
            assertEquals(floor(ln(1.0)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(ceil(ln(1.0)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with ln in int and value */
        @Test
        fun evalUpWithLog_int_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange = 3 {:>> range = "1 .. 5";}
                feature b: ScalarValues::Integer = ln(a);"""
            )
            solver.propagate()
            assertEquals(floor(ln(3.0)).toLong(), solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(ceil(ln(3.0)).toLong(), solver.getVariable("b")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithLog_real_zero_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "0.0 .. 5.0";}
                feature b: ScalarValues::Real = ln(a);
            """)
            solver.propagate()
            // ln(0.0) is Double.NEGATIVE_INFINITY
            assertEquals(Double.NEGATIVE_INFINITY, solver.getVariable("b")!!.min())
            assertEquals(ln(5.0), solver.getVariable("b")!!.max(), 0.00001)
        }

        @Test
        fun evalUpWithLog_real_mixed() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "-2.0 .. 5.0";}
                feature b: ScalarValues::Real = ln(a);
            """)
            solver.propagate()
            assertEquals(Double.NEGATIVE_INFINITY, solver.getVariable("b")!!.min())
            assertEquals(ln(5.0), solver.getVariable("b")!!.max(), 0.00001)
        }

        @Test
        fun evalUpWithLog_int_mixed() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-2 .. 5";}
                feature b: ScalarValues::Integer = ln(a);
            """)
            solver.propagate()
            assertEquals(0L, solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(2L, solver.getVariable("b")!!.idd().getRange().max)
        }
}
