package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StepInterpolationTests {

        @Test
        fun assertTestStepInterpolationEvalDOwn() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
            loadSysMLv2("""
                attribute Avail : Quantities::ScalarQuantityValue { :>> range = 0.0..100.0 [%];} 
                attribute level: Ranges::IntegerInRange = stepInterpolation(Avail, 0.0, 2, 0.9, 3, 0.95, 4, 1.0, 5) { :>> range = 4..4; }
            """, Runlevel.ALL)
            assertNoIssues()
            val test2 = solver.getVariable("Avail")
            assertEquals(0.95, test2!!.vectorQuantity.value.asAadd().min,0.0001)
            assertEquals(1.0, test2.vectorQuantity.value.asAadd().max,0.0001)
        }

        @Test
        fun assertTestStepInterpolationEvalDOwn2() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
            loadSysMLv2("""
                attribute reliability: Quantities::ScalarQuantityValue { :>> range = 0.0..100.0 [%];}
                attribute ASIlFromReliability: Ranges::IntegerInRange = stepInterpolation(reliability, 0.0, 1, 0.99, 2, 0.995, 3, 0.999, 4) {:>> range = 3..4;} 
            """, Runlevel.ALL)
            assertNoIssues()
            val test2 = solver.getVariable("reliability")
            assertEquals(0.995, test2!!.vectorQuantity.value.asAadd().min,0.000001)
            assertEquals(1.0, test2.vectorQuantity.value.asAadd().max,0.000001)
            // val testr = solver.getVariable("Controller1::ASIlFromReliability")
            // assertEquals(1, testr!!.vectorQuantity.value.asIdd().min)
        }

        @Test
        fun assertTestStepInterpolationEvalDOwnInteger() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
            loadSysMLv2("""
                attribute Avail : Ranges::IntegerInRange {:>> range = 0..100;} 
                attribute level: Ranges::IntegerInRange = stepInterpolation(Avail, 0, 2, 90, 3, 95, 4, 100, 5) { :>> range = 5; }
            """, Runlevel.ALL)
            assertNoIssues()
            val test2 = solver.getVariable("Avail")
            assertNotNull(test2)
            assertEquals(100L, test2.min() )
            assertEquals(100L, test2.max() )
        }


        @Test
        fun stepFunctionTest() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Real = 2005.0;
                feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.getVariable("p")
            assertTrue(10.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
            assertTrue(p.min<Double>() > 9.9)
            assertTrue(p.vectorQuantity.getMaxAsDouble() < 10.1)
        }

        @Test
        fun stepFunctionTest2() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Real = 1990.0;
                feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.getVariable("p")
            assertTrue(10.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
            assertTrue(p.min<Double>() > 9.9)
            assertTrue(p.vectorQuantity.getMaxAsDouble() < 10.1)
        }

        @Test
        fun stepFunctionTest3() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Real = 2011.0;
                feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.getVariable("p")
            assertTrue(20.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
            assertTrue(p.min<Double>() > 19.9)
            assertTrue(p.vectorQuantity.getMaxAsDouble() < 20.1)
        }

        @Test
        fun stepFunctionTest5() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Real = 2011.0;
                feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0, 2020.0, 0.0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.getVariable("p")
            assertTrue(20.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
            assertTrue(p.min<Double>() > 19.9)
            assertTrue(p.vectorQuantity.getMaxAsDouble() < 20.1)
        }

        @Test
        fun stepFunctionTest6() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Real = 2020.0;
                feature p: ScalarValues::Real = stepInterpolation(T, 2000.0, 10.0, 2010.0, 20.0, 2020.0, 0.0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.getVariable("p")
            assertTrue(0.0 in p?.vectorQuantity?.aadd()?.getRange()!!)
            assertTrue(p.min<Double>() > -0.1)
            assertTrue(p.vectorQuantity.getMaxAsDouble() < 0.1)
        }

        @Test
        fun stepFunctionTestInt() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Integer = 2020;
                feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.getVariable("p")
            assertEquals(0L, p?.min())
            assertEquals(0L, p?.max())
        }

        @Test
        fun stepFunctionTestInt2() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Integer = 1990;
                feature p: ScalarValues::Integer = stepInterpolation(T, 1980, 10, 2010, 20, 2020, 0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.getVariable("p")
            assertEquals(10L, p?.min())
            assertEquals(10L, p?.max())
        }

        @Test
        fun stepFunctionTestInt3() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Integer = 2000;
                feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.getVariable("p")
            assertEquals(10L, p?.min())
            assertEquals(10L, p?.max())
        }

        @Test
        fun stepFunctionTestInt4() = testSession("ScalarValues") {
            loadKerML("""
                    feature T: ScalarValues::Integer = 2005;
                    feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.getVariable("p")
            assertEquals(10L, p?.min())
            assertEquals(10L, p?.max())
        }

        @Test
        fun stepFunctionTestInt5() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Integer = 2015;
                feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.getVariable("p")
            assertEquals(20L, p?.min())
            assertEquals(20L, p?.max())
        }

        @Test
        fun stepFunctionTestInt6() = testSession("ScalarValues") {
            loadKerML("""
                feature T: ScalarValues::Integer = 2015;
                feature p: ScalarValues::Integer = stepInterpolation(T, 2000, 10, 2010, 20, 2020, 0);
            """)
            solver.propagate()
            assertNoIssues()
            val p = solver.getVariable("p")
            assertEquals(20L, p?.min())
            assertEquals(20L, p?.max())
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
}
