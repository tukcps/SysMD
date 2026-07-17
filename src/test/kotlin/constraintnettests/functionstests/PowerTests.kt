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

class PowerTests {

        @Test
        fun evalUpWithPowB_real_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "3.0 .. 3.0";} 
                feature b: Ranges::RealInRange {:>> range = "4.0 .. 4.0";} 
                feature c: ScalarValues::Real = power(a, b); 
            """, Runlevel.ALL)
            val c = solver.getVariable("c") !!
            assertEquals(81.0, c.min(), 0.000001)
            assertEquals(81.0, c.max(), 0.000001)
            assertEquals("1", c.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithPowB_real_range() = testSession("Ranges") {
            loadKerML(
                """
                feature a: Ranges::RealInRange {:>> range = "1.5 .. 3.5";}
                feature b: Ranges::RealInRange {:>> range = "2.5 .. 4.5";}
                feature c: ScalarValues::Real = power(a, b); """
            )
            solver.propagate()
            assertEquals(1.5.pow(2.5), solver.getVariable("c")!!.min(), 0.000001)
            assertEquals(3.5.pow(4.5), solver.getVariable("c")!!.max(), 0.000001)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithPowB_int_value() = testSession("Ranges") {
            loadKerML("""
                feature a: ScalarValues::Integer = 3;
                feature b: ScalarValues::Integer = 3;
                feature c: ScalarValues::Integer = power(a, b);"""
            )
            solver.propagate()
            assertEquals(27, solver.getVariable("c")!!.idd().getRange().min)
            assertEquals(27, solver.getVariable("c")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun power_int_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "2..3";}
                feature b: Ranges::IntegerInRange {:>> range = "4..5";}
                feature c: ScalarValues::Integer = power(a, b);"""
            )
            solver.propagate()
            assertEquals(16, solver.getVariable("c")!!.idd().getRange().min)
            assertEquals(243, solver.getVariable("c")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun power_evalDownA() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..2";}
                feature b: Ranges::IntegerInRange {:>> range = "1..3";}
                feature c: Ranges::IntegerInRange = power(a, b) {:>> range = "8..8";} """
            )
            solver.propagate()
            assertEquals(2L, solver.getVariable("a")!!.min())
            assertEquals(2L, solver.getVariable("a")!!.max())
            assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test @Ignore
        fun power_evalDownB() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "1..2";}
                feature b: Ranges::IntegerInRange {:>> range = "1..3";}
                feature c: Ranges::IntegerInRange = power(a, b) {:>> range = "8..8";} """
            )
            solver.propagate()
            assertEquals(8, solver.getVariable("c")!!.idd().getRange().min)
            assertEquals(8, solver.getVariable("c")!!.idd().getRange().max)
            assertEquals(2, solver.getVariable("a")!!.idd().getRange().min)
            assertEquals(2, solver.getVariable("a")!!.idd().getRange().max)
            assertEquals(3, solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(3, solver.getVariable("b")!!.idd().getRange().max)
            assertNoIssues()
        }

        @Test @Ignore
        fun power_int_negative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-3..-2";}
                feature b: Ranges::IntegerInRange {:>> range = "4..5";}
                feature c: Ranges::IntegerInRange = power(a, b);"""
            )
            solver.propagate()
            assertEquals(-32, solver.getVariable("c")!!.idd().getRange().min)
            assertEquals(81, solver.getVariable("c")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("c")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test @Ignore
        fun evalUpWithPowB_int_zero() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Integer = 0;
                feature b: ScalarValues::Integer = 3;
                feature c: ScalarValues::Integer = power(a, b);"""
            )
            solver.propagate()
            assertEquals(0, solver.getVariable("c")!!.idd().getRange().min)
            assertEquals(0, solver.getVariable("c")!!.idd().getRange().max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithPow_int_range() = testSession("Ranges") {
            loadKerML("""
                feature a: ScalarValues::Integer = oneOf(2 .. 3);
                feature b: ScalarValues::Integer = oneOf(3 .. 4);
                feature c: ScalarValues::Integer = power(a, b); 
            """, Runlevel.ALL)
            val c = solver.getVariable("c") !!
            assertEquals(8, c.idd().getRange().min)
            assertEquals(81, c.idd().getRange().max)
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertEquals("1", solver.getVariable("b")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpPowerNegativeBase() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real = 1.0;
                feature a: ScalarValues::Real = pow(-5.0,1.0);
            """, Runlevel.ALL)
            assertEquals(-5.0, solver.getVariable("a")!!.min(), 0.00001)
            assertNoIssues()
        }

        @Test
        fun evalUpPowerNegativeBase2() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real = 1.0;
                feature a: ScalarValues::Real = pow(-1.0,2.0);
            """, Runlevel.ALL)
            assertEquals(1.0, solver.getVariable("a")!!.min(), 0.00001)
            assertNoIssues()
        }

        @Test
        fun evalUpPowerSpecialCase() = testSession("Ranges") {
            loadKerML(""" 
                feature a: ScalarValues::Real = oneOf(0.1..2.0); 
                feature b: ScalarValues::Real = pow(a, [1.0..2.0]);
            """)
            solver.propagate()
            assertNoIssues()
            val a = solver.getVariable("a")!!
            val b = solver.getVariable("b")!!
            assertEquals(0.1, a.min(), 0.0001)
            assertEquals(2.0, a.max(), 0.0001)
            assertEquals(0.01, b.min(), 0.0001)
            assertEquals(4.0, b.max(), 0.0001)
        }

        /** a^b with real scalar values */
        @Test
        fun testHATbReal() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Real = 5.0;
                feature b: ScalarValues::Real = 3.0;
                feature y: ScalarValues::Real = a^b;
            """, com.github.tukcps.sysmd.services.Runlevel.ALL)
            assertEquals(125.0, solver.getVariable("y")!!.min(), 0.0001)
            assertNoIssues()
        }

        /** a^b with integer scalar values */
        @Test
        fun testHATbINT() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Integer = 5;
                feature b: ScalarValues::Integer = 3;
                feature y: ScalarValues::Integer = a^b;
            """, com.github.tukcps.sysmd.services.Runlevel.ALL)
            assertEquals(125L, solver.getVariable("y")!!.min())
            assertNoIssues()
        }

        /** a^b evalDown with real (constraining result propagates back to base) */
        @Test
        fun testHATbEVALDown() = testSession("Ranges") {
            loadKerML("""
                    feature a: ScalarValues::Real;
                    feature b: ScalarValues::Real = 3.0;
                    feature y: Ranges::RealInRange = a ^ b{ :>> range = "125..125";}
            """, com.github.tukcps.sysmd.services.Runlevel.ALL)
            assertEquals(5.0, solver.getVariable("a")!!.min(), 0.0001)
            assertEquals(5.0, solver.getVariable("a")!!.max(), 0.0001)
            assertNoIssues()
        }

        /** a^b evalDown with Integer (not yet implemented) */
        @Test @Ignore
        fun testHATbEVALINTDown() = testSession("Ranges") {
            loadKerML("""
                    feature a: ScalarValues::Integer;
                    feature b: ScalarValues::Integer = 3;
                    feature y: Ranges::IntegerInRange = a ^ b{ :>> range = "125..125";}
                """)
            solver.propagate()
            assertEquals(5, solver.getVariable("a")!!.min())
            assertEquals(5, solver.getVariable("a")!!.max())
            assertNoIssues()
        }

        @Test
        fun power_negative_exponent() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "2.0 .. 2.0";}
                feature b: Ranges::RealInRange {:>> range = "-2.0 .. -2.0";}
                feature c: ScalarValues::Real = power(a, b);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(0.25, solver.getVariable("c")!!.min(), 0.000001)
            assertEquals(0.25, solver.getVariable("c")!!.max(), 0.000001)
        }

        @Test
        fun power_negative_base_fractional_exponent() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "-2.0 .. -2.0";}
                feature b: Ranges::RealInRange {:>> range = "0.5 .. 0.5";}
                feature c: ScalarValues::Real = power(a, b);
            """)
            solver.propagate()
            assertEquals(1, status.issues.size)
            assertEquals(Issue.Kind.WARN_INCONSISTENCY, status.issues.firstOrNull()?.kind)
        }
}
