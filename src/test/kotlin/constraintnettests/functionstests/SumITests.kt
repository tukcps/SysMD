package constraintnettests.functionstests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test

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
        assertBounds(3.0 .. 15.0, solver.variable("sum"))
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
        assertNoIssues()
        assertBounds(6.0 .. 9.0, solver.variable("sum"))
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    @Test
    fun sumEvalDown() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: Ranges::RealInRange(1.0 .. 3.0);
            feature b: Ranges::RealInRange(1.0 .. 5.0);
            feature sum: Ranges::RealInRange = sum_i( a, b, i ) { :>> range = 3.0..10.0;}
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(3.0 .. 10.0, solver.variable("sum"))
        assertBounds(1.0 .. 3.0, solver.variable("a"))
        assertBounds(1.5 .. 4.5, solver.variable("b"))
        assertNoIssues()
    }

    @Test
    fun sumEvalDown2() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: Ranges::RealInRange {:>> range = 0..5;} 
            feature b: Ranges::RealInRange {:>> range = 3..5;} 
            feature sum: Ranges::RealInRange = sum_i( a, b, i ) {:>> range = 3.0..14.0;}
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(3.0 .. 14.0, solver.variable("sum"))
        assertBounds(3.0 .. 5.0, solver.variable("b"))
        assertBounds(0.0 .. 5.0, solver.variable("a"))
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
            feature sum: Ranges::RealInRange = sum_i( a, b, s*i ) {:>> range = 30.0..140.0;}
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(30.0 .. 140.0, solver.variable("sum"))
        assertBounds(3.0 .. 5.0, solver.variable("b"))
        assertBounds(1.5 .. 3.5, solver.variable("a"))
        assertNoIssues()
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegative() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: Ranges::RealInRange {:>> range = 1.0..1.0;}
            feature b: Ranges::RealInRange {:>> range = 5.0..5.0;}
            feature sum: ScalarValues::Real = sum_i( a, b, pow(-1.0,i)*i );
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(-3.0 .. -3.0, solver.variable("sum"))
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegativeTest2() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: Ranges::RealInRange {:>> range = -3.0..0.0;}
            feature b: Ranges::RealInRange {:>> range = 0.0..3.0;}
            feature sum: ScalarValues::Real = sum_i( a, b, pow(-1.0,i)*(1.0-sqr(i)) );
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(-5.0 .. 11.0, solver.variable("sum"))
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegativeTest3() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: Ranges::RealInRange {:>> range = -3.0..0.0;}
            feature b: Ranges::RealInRange {:>> range = 0.0..3.0;}
            feature sum: ScalarValues::Real = sum_i( a, b, -pow(-1.0,i)*(1.0-sqr(i)) );
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(-11.0 .. 5.0, solver.variable("sum"))
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    //Calculations with evalDown do not work
    @Test
    fun sumWithNegativeTest4() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature a: Ranges::RealInRange {:>> range = -3.0..0.0;}
            feature b: Ranges::RealInRange {:>> range = 0.0..3.0;}
            feature sum: ScalarValues::Real = sum_i( a, b, -pow(-1.0,i));""")
        solver.propagate()
        assertNoIssues()
        assertBounds(-1.0 .. 1.0, solver.variable("sum"))
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    @Test
    fun sumEvalDownInt() = testSession("Ranges") {
        loadKerML("""
             feature i: ScalarValues::Integer;
             feature a: ScalarValues::Integer = oneOf(1..3);
             feature b: ScalarValues::Integer = oneOf(3..4);
             feature sum: Ranges::IntegerInRange = sum_i( a, b, i ) { :>> range = 3..10;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(3L .. 10L, solver.variable("sum"))
        assertBounds(1L .. 3L, solver.variable("a"))
        assertBounds(3L .. 4L, solver.variable("b"))
    }

    @Ignore //Does not work for ScalarValues::Integer
    @Test
    fun sumEvalDownInt2() = testSession("Ranges") {
        loadKerML("""
            feature i: ScalarValues::Integer;
            feature a: ScalarValues::Integer;
            feature b: ScalarValues::Integer = oneOf(3..5);
            feature sum: Ranges::IntegerInRange = sum_i( a, b, i ) {:>> range = 3..14";}""")
        solver.propagate()
        assertNoIssues()
        assertBounds(9L .. 10L, solver.variable("sum"))
        assertBounds(2L .. 3L, solver.variable("a"))
        assertBounds(3L .. 5L, solver.variable("b"))
        assertNoIssues()
    }
}
