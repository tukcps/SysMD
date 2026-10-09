package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.letVar
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.util.Assertions.assertEquals
import io.github.tukcps.aadd.util.Assertions.assertSafeInclusion
import io.github.tukcps.aadd.values.bounds.LongBound
import util.*
import util.mockup.loadKerML
import kotlin.test.*
import kotlin.test.assertEquals

class ParseAndUseConstraintsTests {

    @Test
    fun constraintTestReal() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 1 .. 2;}
            feature b: Ranges::RealInRange {:>> range = 1.0 .. 2.0;}
        """, Runlevel.VARIABLES)
        val a = solver.variable("a")
        val b = solver.variable("b")
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0 .. 2.0, a)
        assertBounds(1.0 .. 2.0, b)
    }

    @Test
    fun constraintTestRealStars() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = (1 .. *);}
            feature b: Ranges::RealInRange { :>> range = (* .. 2.0);}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        val b = solver.variable("b")

        assertBounds(1.0 .. Double.POSITIVE_INFINITY, a)
        assertBounds(Double.NEGATIVE_INFINITY .. 2.0, b)
    }

    @Test
    fun constraintTestInteger() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange { :>> range = 1 .. 2; } 
        """)
        solver.propagate()
        val a = solver.getVariable("a")
        assertNoIssues()
        assertBounds(1L .. 2L, a!!)
    }


    @Test
    fun constraintTestIntegerStar() = testSession( "Ranges") {
        loadKerML("""
            feature a: ScalarValues::Integer;
        """, Runlevel.ALL)
        assertNoIssues()
        val a = solver.variable("a")
        assertBounds(Double.NEGATIVE_INFINITY..Double.POSITIVE_INFINITY, a)
    }


    @Test
    fun boolSpecTestBoolean() = testSession("ScalarValues") {
        loadKerML("""
            inv a;
            inv false b;
        """, Runlevel.ALL)
        val a = solver.variable("a")
        val b = solver.variable("b")
        assertEquals(builder.Bool.True, a.vectorQuantity.value)
        assertEquals(builder.Bool.False, b.vectorQuantity.value)
        assertNoIssues()
    }

    // Definition of variables initializes its value based on a Boolean expression.
    @Test
    fun boolDefExprCheck() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = 1; } 
            feature b: Ranges::RealInRange { :>> range = 2; } 
            feature d: ScalarValues::Boolean = (a > b) & false; 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(builder.Bool.False, solver.variable("d").bdd())
    }

    // Definition of variables initializes its value based on an expression.
    @Test
    fun constantsCheck() = testSession("Math") {
        loadKerML("feature a: ScalarValues::Real = Math::pi + Math::e;", Runlevel.ALL)
        assertNoIssues()
        val a = solver.getVariable("a")
        assertSafeInclusion(Math.PI + Math.E .. Math.PI + Math.E, a!!.aadd(), 0.00001)
    }

    /**
     * Checks whether min and max are recognized.
     */
    @Test
    fun partsAttributeWithRangeTest() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange { :>> range = 2.0 .. 3.0; }
        """, Runlevel.VARIABLES)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(2.0 .. 3.0, b)
    }

    // A variable can be changed after evaluation.
    // Then the result will change in the symbol table as well after later re-calculation
    @Test
    fun changeVarCheck() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = 1; }
            feature b: Ranges::RealInRange { :>> range = 2; }
            feature c: Ranges::RealInRange { :>> range = 3; }
            feature d: ScalarValues::Real = a+b*c;
        """, Runlevel.ALL)
        assertNoIssues()
        solver.propagate()
        assertBounds(7.0, solver.variable("d"))
        // now we change 'a' to 10, and set d to any real.
        letVar("a", builder.real(10.0))
        letVar("d", builder.Reals.All)
        assertBounds(10.0, solver.variable("a"))
        // A new evaluation must again change d to now 16.
        settings.runlevel = Runlevel.VARIANCE_CHECKED
        solver.propagate()
        solver.propagate()
        assertBounds(16.0, solver.variable("d"))
        assertNoIssues()
    }

    // Tests of 'not' function in parser
    @Test
    fun notFunctionTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean = not(true);
            feature b: ScalarValues::Boolean = not(false);
        """)
        assertNoIssues()
        solver.propagate()
        assertEquals(builder.Bool.False, solver.variable("a").bdd())
        assertEquals(builder.Bool.True, solver.variable("b").bdd())
        assertNoIssues()
    }

    /** The function sum_i computes the sum over an expression from an initial value to an end value */
    @Test
    fun sumFunctionTest()  = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Real; 
            feature a: ScalarValues::Real = sum_i(1.0, 9.0, i);
        """, Runlevel.ALL)
        assertNoIssues()
        assertSafeInclusion(45.0..45.0, solver.variable("a").aadd(), 0.00000001)
    }


    /** The function sum_i computes the sum over an expression from an initial value to an end value */
    @Test
    fun sumFunctionTestIdd() = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Integer;
            feature a: ScalarValues::Integer = sum_i(1, 9, i);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(45, solver.variable("a").idd())
    }


    /** The function sum_i computes the sum over an expression from an initial value to an end value */
    @Test
    fun sumFunctionTestRange() = testSession("ScalarValues") {
        loadKerML("""
                feature i: ScalarValues::Real;
                feature a: ScalarValues::Real = sum_i(1.0, oneOf(7.0 .. 9.0), i);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(28.0 .. 45.0, solver.variable("a"))
    }


    @Test
    fun sumFunctionTestRangeExpr()  = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature s: ScalarValues::Real = 10.0;
            feature MAC_notb: ScalarValues::Real = sum_i( 0.0, 3.0, s*i );
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(60.0 .. 60.0, solver.variable("MAC_notb"))
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    // Checks function calls of ITE: ITE(a>b, a+b, ITE(c<5,d,pi)) ====
    @Test
    fun iteFunctionTest() = testSession("ScalarValues", "Ranges", runlevel = Runlevel.VARIABLES) {
        loadKerML("""
            feature y: ScalarValues::Real= ITE(a>c, d, 3.14); 
            feature a: Ranges::RealInRange { :>> range = 1.0..1.0; }
            feature c: Ranges::RealInRange { :>> range = 0.0..100.0; }; 
            feature d: Ranges::RealInRange {:>> range = 3.0;}
            feature unknown: ScalarValues::Boolean; 
        """, Runlevel.VARIABLES)
        assertEquals(1, solver.variable("y").aadd().height())
        // Simple: knwon Boolean constants
        loadKerML("feature zb: ScalarValues::Boolean = ITE(true, true, false).")
        assertEquals(solver.variable("zb").bdd(), builder.Bool.True)
        // unknown decision variable with two ScalarValues::Boolean parameters
        loadKerML("feature zbunknown: ScalarValues::Boolean = ITE(unknown, true, false).")
        assertEquals(1, solver.variable("zbunknown").bdd().height())
        // unknown decision variable with two real parameters
        loadKerML("feature z: ScalarValues::Real = ITE(unknown, 1.0, 2.0).")
        assertEquals(1, solver.variable("z").aadd().height())
    }


    @Test
    // two uncorrelated noise symbols are created by parameter -1.
    fun rangeMinusUncorrelatedTest() = testSession("ScalarValues") {
        loadKerML("""
                feature a: ScalarValues::Real= oneOf(1.0 .. 2.0);
                feature b: ScalarValues::Real= a;
                feature c: ScalarValues::Real= a - b;
            """)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.0 .. 0.0, solver.variable("c"))
    }

    // different names create two uncorrelated noise symbols.
    @Test
    fun rangeMinusUncorrelated2Test()  = testSession("ScalarValues")  {
        loadKerML("""
            feature a: ScalarValues::Real = oneOf(1.0 .. 2.0);
            feature b: ScalarValues::Real = oneOf(1.0 .. 2.0);
            feature c: ScalarValues::Real = a - b; 
        """, Runlevel.ALL)
        val a = solver.variable("a")
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        val c = solver.variable("c")
        assertBounds(-1.0 .. 1.0, c)
    }


    @Test
    fun rangeNewSyntaxSimpleInteger() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = oneOf(1..2);
            feature b: ScalarValues::Integer = oneOf(1..2);
            feature c: ScalarValues::Integer = a - b;
        """, Runlevel.ALL)
        assertNoIssues()
        assertBounds(-1L .. 1L, solver.variable("c"))
    }

    @Test
    fun rangeNewSyntaxWithFunctions()  = testSession("ScalarValues") {

        loadKerML("""
            feature a: ScalarValues::Real = sqrt( oneOf(9.0 .. 25.0) );
            feature b: ScalarValues::Real = sqr( oneOf(2.0 .. 3.0) );
            feature c: ScalarValues::Real = a + b; 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(7.0 .. 14.0, solver.variable("c"))
    }

    /**
     * A cyclic dependency in a single statement is detected and an exception is thrown.
     * Alternatively, an error is reported in status.
     */
    @Test
    fun cyclicDependencyTest() = testSession("ScalarValues") {
        loadKerML("""
            feature b: ScalarValues::Real;
            feature a: ScalarValues::Real = a; // Meaningless binding. 
        """, Runlevel.ALL)
        assertIssue("Cyclic")
    }

    @Test
    fun constraintTestRealNegative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = -2.0 .. -1.0;}
        """, Runlevel.ALL)
        val a = solver.variable("a")
        solver.propagate()
        assertNoIssues()
        assertBounds(-2.0 .. -1.0, a)
    }

    @Test
    fun constraintTestIntegerNegative() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange { :>> range = -2 .. -1; } 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = solver.variable("a")
        assertBounds(-2L .. -1L, a)
    }
}
