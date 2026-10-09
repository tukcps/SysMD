package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.*
import util.mockup.loadKerML
import kotlin.test.*

class ToRealTests {

        /**
         * Test of the function to Real(x) : Boolean -> ScalarValues::Real.
         */
        @Test
        fun toRealTest() = testSession("ScalarValues") {
            loadKerML("""
                    feature a: ScalarValues::Boolean = true; 
                     feature b: ScalarValues::Real = toReal(a); 
                    feature c: ScalarValues::Boolean = false; 
                    feature d: ScalarValues::Real = toReal(c);  
                    feature e: ScalarValues::Boolean;
                    feature f: ScalarValues::Real = toReal(e);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val b = solver.variable("b")
            val d = solver.variable("d")
            val f = solver.variable("f")
            assertBounds(1.0 .. 1.0, b)
            assertBounds(0.0 .. 0.0, d)
            assertEquals(0, d.vectorQuantity.value.height())
            assertBounds(0.0 .. 1.0, f)
            assertEquals(1, f.vectorQuantity.value.height())
        }

        @Test @Ignore
        fun toRealEvalDownTest() = testSession("Ranges") {
            loadKerML("""
                    feature a: ScalarValues::Boolean; 
                    feature b: Ranges::RealInRange  = toReal(a) {:>> range = "1.0 .. 1.0";}  
            """)
            assertNoIssues()
            val a = solver.getVariable("a")
            assertTrue((a!!.vectorQuantity.value === builder.Bool.True))

        }


        @Test
        fun realFunctionTest() = testSession("ScalarValues") {
            loadKerML("""
                feature i: ScalarValues::Integer = [2 .. 3].
                feature r: ScalarValues::Real = ToReal(i).
                """)
            solver.propagate()
            val r = solver.variable("r")
            assertNoIssues()
            assertBounds(2.0 .. 3.0, r)
        }

        /**
         * Regression test for AstReal evalDown:
         * ToReal(i) constrained to [2.1 .. 4.9] means integer i must be in [3 .. 4] (ceil(2.1)..floor(4.9)).
         */
        @Test
        fun realFunctionEvalDownRegressionTest() = testSession("Ranges") {
            loadKerML("""
                feature i: Ranges::IntegerInRange {:>> range = 0 .. 10;}
                feature r: Ranges::RealInRange = ToReal(i) {:>> range = 2.1 .. 4.9;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val i = solver.variable("i")
            assertBounds(3L .. 4L, i)
        }

        /**
         * Regression test for AstReal evalDown when no integers exist in the real range:
         * ToReal(i) in [2.1 .. 2.8] has ceil(2.1)=3 > floor(2.8)=2 -> Empty integer set.
         */
        @Test
        fun realFunctionEvalDownEmptyIntervalRegressionTest() = testSession("Ranges") {
            loadKerML("""
                feature i: Ranges::IntegerInRange {:>> range = 0 .. 10;}
                feature r: Ranges::RealInRange = ToReal(i) {:>> range = 2.1 .. 2.8;}
            """, Runlevel.ALL)
            val i = solver.variable("i")
            assertEmpty(i, "Expected empty integer set when range contains no integer")
        }
}
