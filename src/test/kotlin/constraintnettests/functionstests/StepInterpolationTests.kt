package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import kotlin.test.assertEquals
import util.*
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StepInterpolationTests {

        @Test
        fun assertTestStepInterpolationEvalDOwn() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
            loadSysMLv2("""
                attribute Avail : Quantities::ScalarQuantityValue { :>> range = 0.0..100.0 [%];} 
                attribute level: Ranges::IntegerInRange = stepInterpolation(Avail, 0.0, 2, 0.9, 3, 0.95, 4, 1.0, 5) { :>> range = 4..4; }
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val test2 = solver.variable("Avail")
            assertBounds(0.95 .. 1.0, test2)
        }

        @Test
        fun assertTestStepInterpolationEvalDOwn2() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
            loadSysMLv2("""
                attribute reliability: Quantities::ScalarQuantityValue { :>> range = 0.0..100.0 [%];}
                attribute ASIlFromReliability: Ranges::IntegerInRange = stepInterpolation(reliability, 0.0, 1, 0.99, 2, 0.995, 3, 0.999, 4) {:>> range = 3..4;} 
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val test2 = solver.variable("reliability")
            assertBounds(0.995 .. 1.0, test2)
            // val testr = solver.getVariable("Controller1::ASIlFromReliability")
            // assertEquals(1, testr!!.vectorQuantity.value.asIdd().min)
        }

        @Test
        fun assertTestStepInterpolationEvalDOwnInteger() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
            loadSysMLv2("""
                attribute Avail : Ranges::IntegerInRange {:>> range = 0..100;} 
                attribute level: Ranges::IntegerInRange = stepInterpolation(Avail, 0, 2, 90, 3, 95, 4, 100, 5) { :>> range = 5; }
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val test2 = solver.variable("Avail")
            assertBounds(100L .. 100L, test2)
        }


        @Test
        fun stepFunctionTest() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Real = 2005.0;
                feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.variable("p")
            assertBounds(10.0, p)
        }

        @Test
        fun stepFunctionTest2() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Real = 1990.0;
                feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.variable("p")
            assertBounds(10.0, p)
        }

        @Test
        fun stepFunctionTest3() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Real = 2011.0;
                feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.variable("p")
            assertBounds(20.0, p)
        }

        @Test
        fun stepFunctionTest5() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Real = 2011.0;
                feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0, 2020.0, 0.0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.variable("p")
            assertBounds(20.0, p)
        }

        @Test
        fun stepFunctionTest6() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Real = 2020.0;
                feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0, 2020.0, 0.0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.variable("p")
            assertBounds(0.0, p)
        }

        @Test
        fun stepFunctionTestInt() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Integer = 2020;
                feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.variable("p")
            assertBounds(0L .. 0L, p)
        }

        @Test
        fun stepFunctionTestInt2() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Integer = 1990;
                feature p: ScalarValues::Integer = stepInterpolation(T, 1980, 10, 2010, 20, 2020, 0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.variable("p")
            assertBounds(10L .. 10L, p)
        }

        @Test
        fun stepFunctionTestInt3() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Integer = 2000;
                feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.variable("p")
            assertBounds(10L .. 10L, p)
        }

        @Test
        fun stepFunctionTestInt4() = testSession("ScalarValues") {
            loadKerML("""
                    feature T: ScalarValues::Integer = 2005;
                    feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val p = solver.variable("p")
            assertBounds(10L .. 10L, p)
        }

        @Test
        fun stepFunctionTestInt5() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Integer = 2015;
                feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val p = solver.variable("p")
            assertBounds(20L .. 20L, p)
        }

        @Test
        fun stepFunctionTestInt6() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Integer = 2015;
                feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.variable("p")
            assertBounds(20L .. 20L, p)
        }

        @Test
        fun stepFunctionTestNonIncreasing() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Real = 2005.0;
                feature p: ScalarValues::Real = stepInterpolation(T, 2010.0, 10.0, 2000.0, 20.0);
            """)
            solver.propagate()
            assertTrue(status.issues.isNotEmpty(), "Expected non-increasing x-values to report semantic error")
        }

        /**
         * Regression test for parameter count validation (odd number >= 3).
         * Even number of parameters or < 3 should report an issue.
         */
        @Test
        fun stepFunctionTooFewOrEvenParametersTest() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Real = 2005.0;
                feature p: ScalarValues::Real = stepInterpolation(T, 2000.0);
            """)
            solver.propagate()
            assertTrue(status.issues.isNotEmpty(), "Expected error for invalid parameter count (<3 or even)")
        }

        /**
         * Regression test: x below the first point also yields the first y-value, so the first region is open
         * to the left instead of starting at x0.
         */
        @Test
        fun stepEvalDownRegionBelowFirstPointRegressionTest() = testSession("Ranges") {
            loadKerML("""
                feature T: Ranges::RealInRange {:>> range = 0.0 .. 100.0;}
                feature p: Ranges::IntegerInRange = stepInterpolation(T, 10.0, 1, 20.0, 2) {:>> range = 1 .. 1;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(0.0 .. 20.0, solver.variable("T"))
        }

        /** Regression test: the last region is open to the right. */
        @Test
        fun stepEvalDownRegionAboveLastPointRegressionTest() = testSession("Ranges") {
            loadKerML("""
                feature T: Ranges::RealInRange {:>> range = 0.0 .. 100.0;}
                feature p: Ranges::IntegerInRange = stepInterpolation(T, 10.0, 1, 20.0, 2) {:>> range = 2 .. 2;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(20.0 .. 100.0, solver.variable("T"))
        }

        /** Regression test: a step value that only overlaps (but is not contained in) the result must not be dropped. */
        @Test
        fun stepEvalDownOverlapRegressionTest() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = 1.0 .. 3.0;}
                feature T: Ranges::RealInRange {:>> range = 0.0 .. 100.0;}
                feature p: Ranges::RealInRange = stepInterpolation(T, 0.0, a, 10.0, 20.0) {:>> range = 2.0 .. 2.0;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(0.0 .. 10.0, solver.variable("T"))
        }
}
