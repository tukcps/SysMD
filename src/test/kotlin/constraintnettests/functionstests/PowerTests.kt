package constraintnettests.functionstests

import util.variable
import util.assertEmpty
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.math.pow
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class PowerTests {

        @Test
        fun evalUpWithPowB_real_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = 3.0 .. 3.0;} 
                feature b: Ranges::RealInRange {:>> range = 4.0 .. 4.0;} 
                feature c: ScalarValues::Real = power(a, b); 
            """, Runlevel.ALL)
            val c = solver.getVariable("c") !!
            solver.propagate()
            assertNoIssues()
            assertBounds(81.0 .. 81.0, c)
            assertEquals("1", c.vectorQuantity.unit.toString())
        }

        @Test
        fun evalUpWithPowB_real_range() = testSession("Ranges") {
            loadKerML(
                """
                feature a: Ranges::RealInRange {:>> range = 1.5 .. 3.5;}
                feature b: Ranges::RealInRange {:>> range = 2.5 .. 4.5;}
                feature c: ScalarValues::Real = power(a, b); """
            )
            solver.propagate()
            assertBounds(1.5.pow(2.5) .. 3.5.pow(4.5), solver.variable("c"))
            assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
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
            assertBounds(27L .. 27L, solver.variable("c"))
            assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun power_int_value() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = 2..3;}
                feature b: Ranges::IntegerInRange {:>> range = 4..5;}
                feature c: ScalarValues::Integer = power(a, b);"""
            )
            solver.propagate()
            assertBounds(16L .. 243L, solver.variable("c"))
            assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun power_evalDownA() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = 1..2;}
                feature b: Ranges::IntegerInRange {:>> range = 1..3;}
                feature c: Ranges::IntegerInRange = power(a, b) {:>> range = 8;} 
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(2L .. 2L, solver.variable("a"))
            assertEquals("1", solver.variable("a").vectorQuantity.unit.toString())
        }

        @Test
        fun power_evalDownB() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = 1..2;}
                feature b: Ranges::IntegerInRange {:>> range = 1..3;}
                feature c: Ranges::IntegerInRange = power(a, b) {:>> range = 8;} """
            )
            solver.propagate()
            assertBounds(8L .. 8L, solver.variable("c"))
            assertBounds(2L .. 2L, solver.variable("a"))
            assertBounds(3L .. 3L, solver.variable("b"))
            assertNoIssues()
        }

        @Test
        fun power_int_negative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = -3..-2;}
                feature b: Ranges::IntegerInRange {:>> range = 4..5;}
                feature c: Ranges::IntegerInRange = power(a, b);"""
            )
            solver.propagate()
            assertBounds(-243L .. 81L, solver.variable("c"))
            assertEquals("1", solver.variable("c").vectorQuantity.unit.toString())
            assertNoIssues()
        }

        @Test
        fun evalUpWithPowB_int_zero() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Integer = 0;
                feature b: ScalarValues::Integer = 3;
                feature c: ScalarValues::Integer = power(a, b);
            """)
            solver.propagate()
            assertBounds(0L .. 0L, solver.variable("c"))
            assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
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
            solver.propagate()
            assertNoIssues()
            assertBounds(8L .. 81L, c)
            assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
            assertEquals("1", solver.variable("b").vectorQuantity.unit.toString())
        }

        @Test
        fun evalUpPowerNegativeBase() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real = 1.0;
                feature a: ScalarValues::Real = pow(-5.0,1.0);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(-5.0, solver.variable("a"))
        }

        @Test
        fun evalUpPowerNegativeBase2() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real = 1.0;
                feature a: ScalarValues::Real = pow(-1.0,2.0);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(1.0, solver.variable("a"))
        }

        @Test
        fun evalUpPowerSpecialCase() = testSession("Ranges") {
            loadKerML(""" 
                feature a: ScalarValues::Real = oneOf(0.1..2.0); 
                feature b: ScalarValues::Real = pow(a, [1.0..2.0]);
            """)
            solver.propagate()
            assertNoIssues()
            val a = solver.variable("a")
            val b = solver.variable("b")
            assertBounds(0.1 .. 2.0, a)
            assertBounds(0.01 .. 4.0, b)
        }

        /** a^b with real scalar values */
        @Test
        fun testHATbReal() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Real = 5.0;
                feature b: ScalarValues::Real = 3.0;
                feature y: ScalarValues::Real = a^b;
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(125.0, solver.variable("y"))
        }

        /** a^b with integer scalar values */
        @Test
        fun testHATbINT() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Integer = 5;
                feature b: ScalarValues::Integer = 3;
                feature y: ScalarValues::Integer = a^b;
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(125L, solver.variable("y"))
        }

        /** a^b evalDown with real (constraining result propagates back to base) */
        @Test
        fun testHATbEVALDown() = testSession("Ranges") {
            loadKerML("""
                    feature a: ScalarValues::Real;
                    feature b: ScalarValues::Real = 3.0;
                    feature y: Ranges::RealInRange = a ^ b{ :>> range = 125;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(5.0 .. 5.0, solver.variable("a"))
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
            assertBounds(5L .. 5L, solver.variable("a"))
            assertNoIssues()
        }

        @Test
        fun power_negative_exponent() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = 2.0 .. 2.0;}
                feature b: Ranges::RealInRange {:>> range = -2.0 .. -2.0;}
                feature c: ScalarValues::Real = power(a, b);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(0.25 .. 0.25, solver.variable("c"))
        }

        @Test
        fun power_negative_base_fractional_exponent() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = -2.0 .. -2.0;}
                feature b: Ranges::RealInRange {:>> range = 0.5 .. 0.5;}
                feature c: ScalarValues::Real = power(a, b);
            """, Runlevel.ALL)
            assertEquals(1, status.issues.size)
            assertEquals(Issue.Kind.WARN_INCONSISTENCY, status.issues.firstOrNull()?.kind)
        }

        @Test
        fun power_evalDown_even_exponent_symmetric() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = -10.0 .. 10.0;}
                feature b: Ranges::RealInRange = 2.0;
                feature c: Ranges::RealInRange = power(a, b) {:>> range = 4.0 .. 9.0;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val a = solver.variable("a")
            assertBounds(-3.0 .. 3.0, a)
        }

        @Test
        fun power_evalDown_even_exponent_negative_result() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = -10.0 .. 10.0;}
                feature b: Ranges::RealInRange = 2.0;
                feature c: Ranges::RealInRange = power(a, b) {:>> range = -9.0 .. -4.0;}
            """)
            solver.propagate()
            val c = solver.variable("c")
            assertEmpty(c)
            assert(status.issues.any { it.kind == Issue.Kind.WARN_INCONSISTENCY })
        }

        @Test
        fun power_evalDown_int_even_exponent_symmetric() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = -10 .. 10;}
                feature b: Ranges::IntegerInRange = 2;
                feature c: Ranges::IntegerInRange = power(a, b) {:>> range = 4 .. 9;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val a = solver.variable("a")
            assertBounds(-3L .. 3L, a)
        }

        @Test
        fun power_evalDown_int_even_exponent_negative_result() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = -10 .. 10;}
                feature b: Ranges::IntegerInRange = 2;
                feature c: Ranges::IntegerInRange = power(a, b) {:>> range = -9 .. -4;}
            """)
            solver.propagate()
            val a = solver.variable("a")
            assertEmpty(a)
        }

        @Test
        fun power_evalDown_even_exponent_positive_base() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = 0.0 .. 10.0;}
                feature b: Ranges::RealInRange = 2.0;
                feature c: Ranges::RealInRange = power(a, b) {:>> range = 4.0 .. 9.0;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val a = solver.variable("a")
            assertBounds(2.0 .. 3.0, a)
        }

        @Test
        fun power_evalDown_even_exponent_negative_base() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = -10.0 .. 0.0;}
                feature b: Ranges::RealInRange = 2.0;
                feature c: Ranges::RealInRange = power(a, b) {:>> range = 4.0 .. 9.0;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val a = solver.variable("a")
            assertBounds(-3.0 .. -2.0, a)
        }

        @Test
        fun power_evalDown_int_even_exponent_positive_base() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = 0 .. 10;}
                feature b: Ranges::IntegerInRange = 2;
                feature c: Ranges::IntegerInRange = power(a, b) {:>> range = 4 .. 9;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val a = solver.variable("a")
            assertBounds(2L .. 3L, a)
        }

        @Test
        fun power_evalDown_int_even_exponent_negative_base() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = -10 .. 0;}
                feature b: Ranges::IntegerInRange = 2;
                feature c: Ranges::IntegerInRange = power(a, b) {:>> range = 4 .. 9;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val a = solver.variable("a")
            assertBounds(-3L .. -2L, a)
        }

        /**
         * Regression test for exponent evalDown:
         * Exponents are dimensionless numbers, so the backward-propagated exponent must not inherit the unit of the power result.
         */
        @Test
        fun power_evalDown_exponent_dimensionless_regression() = testSession("ISQ") {
            loadKerML("""
                feature baseVal: Quantities::ScalarQuantityValue {:>> range = 2.0 .. 2.0 [1];}
                feature expVal: Quantities::ScalarQuantityValue {:>> range = 1.0 .. 5.0 [1];}
                feature result: Quantities::ScalarQuantityValue = power(baseVal, expVal) {:>> range = 8.0 .. 8.0 [1];}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val exp = solver.variable("expVal")
            assertEquals("1", exp.vectorQuantity.unit.toString())
            assertBounds(3.0 .. 3.0, exp)
        }


        /** Regression test: odd roots of negative numbers are defined, so a^3 in [-8, 27] gives a in [-2, 3]. */
        @Test
        fun power_evalDown_odd_exponent_negative_base() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = -10.0 .. 10.0;}
                feature b: Ranges::RealInRange = 3.0;
                feature c: Ranges::RealInRange = power(a, b) {:>> range = -8.0 .. 27.0;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(-2.0 .. 3.0, solver.variable("a"))
        }

        /** Regression test: x^0 = 1 for every x, so the base must stay unrestricted. */
        @Test
        fun power_evalDown_exponent_zero_keeps_base() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = -5.0 .. 5.0;}
                feature b: Ranges::RealInRange = 0.0;
                feature c: Ranges::RealInRange = power(a, b) {:>> range = 0.0 .. 2.0;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(-5.0 .. 5.0, solver.variable("a"))
        }

        /** Regression test: Integer base with Real exponent is reported as an error instead of a ClassCastException. */
        @Test
        fun power_int_base_real_exponent_reports_issue() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = 2 .. 3;}
                feature b: Ranges::RealInRange = 2.0;
                feature c: ScalarValues::Real = power(a, b);
            """, Runlevel.ALL)
            assert(status.issues.isNotEmpty())
        }

        /** Regression test (AADD): odd real exponent on a negative-only base must enclose the full range. */
        @Test
        fun power_odd_exponent_negative_base_range() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = -3.0 .. -2.0;}
                feature b: Ranges::RealInRange = 3.0;
                feature c: ScalarValues::Real = power(a, b);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(-27.0 .. -8.0, solver.variable("c"))
        }

        @Test
        fun power_evalDown_odd_exponent_negative_result() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = -10.0 .. 10.0;}
                feature b: Ranges::RealInRange = 3.0;
                feature c: Ranges::RealInRange = power(a, b) {:>> range = -27.0 .. -8.0;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(-3.0 .. -2.0, solver.variable("a"))
        }
}
