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

class SumITests {

        @Test
        fun sumEvalUp1() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real; 
                feature a: ScalarValues::Real = oneOf(1.0..3.0);
                feature b: ScalarValues::Real = oneOf(3.0..5.0);
                feature sum: ScalarValues::Real = sum_i( a, b, i );
            """)
            solver.propagate()
            assertEquals(15.0, solver.getVariable("sum")!!.max(), 0.00001)
            assertEquals(3.0, solver.getVariable("sum")!!.min(), 0.00001)
            assertNoIssues()
        }

        @Test
        fun sumEvalUp2() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real;
                feature a: ScalarValues::Real = oneOf(0.0..0.0);
                feature b: ScalarValues::Real = oneOf(1.0..2.0);
                feature s: ScalarValues::Real = oneOf(4.0..5.0);
                feature t: ScalarValues::Real = oneOf(2.0..2.0);
                feature sum: ScalarValues::Real = sum_i( a, b, s-t*i );
            """)
            solver.propagate()
            assertEquals(9.0, solver.getVariable("sum")!!.max(), 0.00001)
            assertEquals(6.0, solver.getVariable("sum")!!.min(), 0.00001)
            //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
        }

        @Test
        fun sumEvalDown() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real;
                feature a: Ranges::RealInRange(1.0 .. 3.0);
                feature b: Ranges::RealInRange(1.0 .. 5.0);
                feature sum: Ranges::RealInRange = sum_i( a, b, i ) { :>> range = "3.0..10.0";}
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(3.0, solver.getVariable("sum")!!.min(), 0.00001)
            assertEquals(10.0, solver.getVariable("sum")!!.max(), 0.00001)
            assertEquals(1.0, solver.getVariable("a")!!.min(), 0.00001)
            assertEquals(3.0, solver.getVariable("a")!!.max(), 0.00001)
            assertEquals(1.5, solver.getVariable("b")!!.min(), 0.00001)
            // with int the borders would be 3 and 4, but rounding makes the intervals bigger
            assertEquals(4.5, solver.getVariable("b")!!.max(), 0.00001)
            assertNoIssues()
        }

        @Test
        fun sumEvalDown2() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real;
                feature a: Ranges::RealInRange {:>> range = "0..5";} 
                feature b: Ranges::RealInRange {:>> range = "3..5";} 
                feature sum: Ranges::RealInRange = sum_i( a, b, i ) {:>> range = "3.0..14.0";}
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(3.0, solver.getVariable("sum")!!.min(), 0.00001)
            assertEquals(14.0, solver.getVariable("sum")!!.max(), 0.00001)
            assertEquals(3.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(5.0, solver.getVariable("b")!!.max(), 0.00001)
            assertEquals(0.0, solver.getVariable("a")!!.min(), 0.00001)
            assertEquals(5.0, solver.getVariable("a")!!.max(), 0.00001)
            assertNoIssues()
        }

        //Calculations with evalDown do not work
        @Ignore

        @Test
        fun sumEvalDownWithMultiplication() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real;
                feature a: ScalarValues::Real;
                feature s: ScalarValues::Real = 10.0;
                feature b: ScalarValues::Real = oneOf(3.0 .. 5.0);
                feature sum: Ranges::RealInRange = sum_i( a, b, s*i ) {:>> range = "30.0..140.0";}
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(30.0, solver.getVariable("sum")!!.min(), 0.00001)
            assertEquals(140.0, solver.getVariable("sum")!!.max(), 0.00001)
            assertEquals(3.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(5.0, solver.getVariable("b")!!.max(), 0.00001)
            assertEquals(1.5, solver.getVariable("a")!!.min(), 0.00001)
            assertEquals(3.5, solver.getVariable("a")!!.max(), 0.00001)
            assertNoIssues()
        }

        //Calculations with evalDown do not work
        @Test
        fun sumWithNegative() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real;
                feature a: Ranges::RealInRange {:>> range = "1.0..1.0";}
                feature b: Ranges::RealInRange {:>> range = "5.0..5.0";}
                feature sum: ScalarValues::Real = sum_i( a, b, pow(-1.0,i)*i );
            """)
            solver.propagate()
            assertEquals(-3.0, solver.getVariable("sum")!!.min(), 0.00001)
            assertEquals(-3.0, solver.getVariable("sum")!!.max(), 0.00001)
            //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
        }

        //Calculations with evalDown do not work
        @Test
        fun sumWithNegativeTest2() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real;
                feature a: Ranges::RealInRange {:>> range = "-3.0..0.0";}
                feature b: Ranges::RealInRange {:>> range = "0.0..3.0";}
                feature sum: ScalarValues::Real = sum_i( a, b, pow(-1.0,i)*(1.0-sqr(i)) );
            """)
            solver.propagate()
            assertEquals(-5.0, solver.getVariable("sum")!!.min(), 0.00001)
            assertEquals(11.0, solver.getVariable("sum")!!.max(), 0.00001)
            //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
        }

        //Calculations with evalDown do not work
        @Test
        fun sumWithNegativeTest3() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real;
                feature a: Ranges::RealInRange {:>> range = "-3.0..0.0";}
                feature b: Ranges::RealInRange {:>> range = "0.0..3.0";}
                feature sum: ScalarValues::Real = sum_i( a, b, -pow(-1.0,i)*(1.0-sqr(i)) );
            """)
            solver.propagate()
            assertEquals(-11.0, solver.getVariable("sum")!!.min(), 0.00001)
            assertEquals(5.0, solver.getVariable("sum")!!.max(), 0.00001)
            //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
        }

        //Calculations with evalDown do not work
        @Test
        fun sumWithNegativeTest4() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Real;
                feature a: Ranges::RealInRange {:>> range = "-3.0..0.0";}
                feature b: Ranges::RealInRange {:>> range = "0.0..3.0";}
                feature sum: ScalarValues::Real = sum_i( a, b, -pow(-1.0,i));""")
            solver.propagate()
            assertEquals(-1.0, solver.getVariable("sum")!!.min(), 0.00001)
            assertEquals(1.0, solver.getVariable("sum")!!.max(), 0.00001)
            //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
        }

        @Test
        fun sumEvalDownInt() = testSession("Ranges") {
            loadKerML("""
                 feature i: ScalarValues::Integer;
                 feature a: ScalarValues::Integer = oneOf(1..3);
                 feature b: ScalarValues::Integer = oneOf(3..4);
                 feature sum: Ranges::IntegerInRange = sum_i( a, b, i ) {:>> range = "3..10";}""")
            solver.propagate()
            assertNoIssues()
            assertEquals(3, solver.getVariable("sum")!!.idd().getRange().min)
            assertEquals(10, solver.getVariable("sum")!!.idd().getRange().max)
            assertEquals(1, solver.getVariable("a")!!.idd().getRange().min)
            assertEquals(3, solver.getVariable("a")!!.idd().getRange().max)
            assertEquals(3, solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(4, solver.getVariable("b")!!.idd().getRange().max)
        }

        @Ignore //Does not work for ScalarValues::Integer

        @Test
        fun sumEvalDownInt2() = testSession("Ranges") {
            loadKerML("""
                feature i: ScalarValues::Integer;
                feature a: ScalarValues::Integer;
                feature b: ScalarValues::Integer = oneOf(3..5);
                feature sum: Ranges::IntegerInRange = sum_i( a, b, i ) {:>> range = "3..14";}""")
            solver.propagate()
            assertNoIssues()
            assertEquals(9, solver.getVariable("sum")!!.idd().getRange().min)
            assertEquals(10, solver.getVariable("sum")!!.idd().getRange().max)
            assertEquals(2, solver.getVariable("a")!!.idd().getRange().min)
            assertEquals(3, solver.getVariable("a")!!.idd().getRange().max)
            assertEquals(3, solver.getVariable("b")!!.idd().getRange().min)
            assertEquals(5, solver.getVariable("b")!!.idd().getRange().max)
            assertNoIssues()
        }

    /**
     * Tests sum_i function with a range expression as body.
     * Throws error "lateinit property downQuantity has not been initialized" if the 3rd parameter
     * of sum_i is evaluated before 'i' is defined.
     * sum_i(0.0, 3.0, s*i) with s=10.0 should yield 60.0 (sum of 0*10, 1*10, 2*10, 3*10 = 60).
     */
    @Test fun sumFunctionTestRangeExpr() = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature s: ScalarValues::Real = 10.0;
            feature MAC_notb: ScalarValues::Real = sum_i( 0.0, 3.0, s*i );
        """, Runlevel.ALL)
        assertEquals(60.0, solver.getVariable("MAC_notb")!!.aadd().getRange().min, 0.00001)
        assertEquals(60.0, solver.getVariable("MAC_notb")!!.aadd().getRange().max, 0.00001)
        assertNoIssues()
    }
}
