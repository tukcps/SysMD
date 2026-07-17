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

class Power2Tests {

        /** ConstNet shall compute bottom-up with pow2 in real and model.builder.range */
        @Test
        fun evalUpWithPow2_real_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "1.0 .. 5.0";}
                feature b: ScalarValues::Real = power2(a);
            """, Runlevel.ALL)
            val b = solver.getVariable("b")
            assertNotNull(b)
            assertEquals(2.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(32.0, solver.getVariable("b")!!.max(), 0.00001)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with pow2 in real and value */
        @Test
        fun evalUpWithPow2_real_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange = 3.0 {:>> range = "1.0 .. 5.0";}
                feature b: ScalarValues::Real = power2(a); 
            """, Runlevel.ALL)
            assertEquals(8.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(8.0, solver.getVariable("b")!!.max(), 0.00001)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalDownWithPow2_real_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "1.0 .. 5.0";}
                feature b: Ranges::RealInRange = power2(a) {:>> range = "8.0 .. 8.0";} """
            )
            solver.propagate()
            assertEquals(3.0, solver.getVariable("a")!!.min(), 0.00001)
            assertEquals(3.0, solver.getVariable("a")!!.max(), 0.00001)
            assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with pow2 and negative values*/
        @Test
        fun evalUpWithPow2_real_negative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "-2.0 .. -1.0";}
                feature b: ScalarValues::Real = power2(a);
            """, Runlevel.ALL)
            assertNoIssues()
            assertEquals(0.25, solver.getVariable("b")!!.aadd().min, 0.0001)
            assertEquals(0.5, solver.getVariable("b")!!.aadd().max, 0.0001)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        }

        /** ConstNet shall compute bottom-up with pow2 with zero*/
        @Test
        fun evalUpWithPow2_real_zero() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "0.0 .. 0.0";}
                feature b: ScalarValues::Real = power2(a); 
            """, Runlevel.ALL)
            assertNoIssues()
            assertEquals(1.0, solver.getVariable("b")!!.aadd().min, 0.0001)
            assertEquals(1.0, solver.getVariable("b")!!.aadd().max, 0.0001)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        }

        /** ConstNet shall compute bottom-up with pow2 in int and model.builder.range*/
        @Test
        fun evalUpWithPow2_int_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1 .. 5";}
                feature b: ScalarValues::Integer = power2(a); """
            )
            solver.propagate()
            assertEquals(2, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
            assertEquals(32, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        /** ConstNet shall compute bottom-up with pow2 in int and model.builder.range*/
        @Test
        fun evalUpWithPow2_int_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange = 3 {:>> range = "1 .. 5";}
                feature b: ScalarValues::Integer = power2(a); """
            )
            assertNoIssues()
            solver.propagate()
            assertEquals(8, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.min)
            assertEquals(8, (solver.getVariable("b")!!.idd() as IDD.Leaf).value.max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
        }

        @Test
        fun evalUpWithPow2_int_negative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-3 .. -1";}
                feature b: ScalarValues::Integer = power2(a); """
            )
            solver.propagate()
            assertNoIssues()
            assertEquals(floor(0.125).toLong(), solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(ceil(0.5).toLong(), solver.getVariable("b")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithPow2_int_negative2() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-4 .. -2";}
                feature b: ScalarValues::Integer = power2(a); """
            )
            solver.propagate()
            assertNoIssues()
            assertEquals(floor(0.0625).toLong(), solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(ceil(0.25).toLong(), solver.getVariable("b")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithPow2_real_mixed() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "-2.0 .. 2.0";}
                feature b: ScalarValues::Real = power2(a);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(0.25, solver.getVariable("b")!!.min(), 0.0001)
            assertEquals(4.0, solver.getVariable("b")!!.max(), 0.0001)
        }

        @Test
        fun evalUpWithPow2_int_mixed() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-2 .. 2";}
                feature b: ScalarValues::Integer = power2(a);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(0L, solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(4L, solver.getVariable("b")!!.idd().getRange().max)
        }
}
