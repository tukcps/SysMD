package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.letVar
import io.github.tukcps.aadd.AADD
import util.assertIssue
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class ParseAndUseConstraintsTests {

    @Test
    fun constraintTestReal() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "1 .. 2";}
            feature b: Ranges::RealInRange {:>> range = "1.0 .. 2.0";}
        """, Runlevel.VARIABLES)
        val a = solver.getVariable("a")!!
        val b = solver.getVariable("b")!!
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, a.min())
        assertEquals(2.0, a.max())
        assertEquals(1.0, b.min())
        assertEquals(2.0, b.max())
    }

    @Test
    fun constraintTestRealStars() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "1 .. *";}
            feature b: Ranges::RealInRange {:>> range = "* .. 2.0";}
        """, Runlevel.ALL)
        assertNoIssues()
        val a = solver.getVariable("a")!!
        val b = solver.getVariable("b")!!
        assertEquals(1.0, a.min())
        assertTrue(settings.maxReal < a.max() as Double)
        assertTrue(settings.minReal > b.min() as Double)
        assertEquals(2.0, b.max())
    }

    @Test
    fun constraintTestInteger() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange { :>> range = "1 .. 2"; } 
        """)
        solver.propagate()
        val a = solver.getVariable("a")
        assertNoIssues()
        assertEquals(1L, a?.min())
        assertEquals(2L, a?.max())
    }


    @Test
    fun constraintTestIntegerStar() = testSession( "Ranges") {
        loadKerML("""
            feature a: ScalarValues::Integer;
        """)
        assertNoIssues()
        solver.propagate()
        val a = solver.getVariable("a")
        assertNotNull(a)
        assertTrue(settings.minInt >= a.min<Long>())
        assertTrue(settings.maxInt <= a.max<Long>())
        assertNoIssues()
    }


    @Test
    fun boolSpecTestBoolean() = testSession("ScalarValues") {
        loadKerML("""
            inv a;
            inv b false;
        """, Runlevel.ALL)
        val a = solver.getVariable("a")!!
        val b = solver.getVariable("b")!!
        assertEquals(builder.True, a.vectorQuantity.value)
        assertEquals(builder.False, b.vectorQuantity.value)
        assertNoIssues()
    }


    // Definition of variables initializes its value based on a Boolean expression.
    @Test
    fun boolDefExprCheck() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = "1"; } 
            feature b: Ranges::RealInRange { :>> range = "2"; } 
            feature d: ScalarValues::Boolean = (a > b) & false; 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(builder.False, solver.getVariable("d")!!.bdd())
    }

    // Definition of variables initializes its value based on an expression.
    @Test
    fun constantsCheck() = testSession("Math") {
        loadKerML("feature a: ScalarValues::Real = Math::pi + Math::e;", Runlevel.ALL)
        assertNoIssues()
        val a = solver.getVariable("a")
        assertEquals(Math.PI + Math.E, a!!.min(), 0.00001)
    }

    /**
     * Checks whether min and max are recognized.
     */
    @Test
    fun partsAttributeWithRangeTest() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange { :>> range = "2.0 .. 3.0"; }
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val b = solver.getVariable("b")
        assertNotNull(b)
        assertEquals(2.0, b.min())
        assertEquals(3.0, b.max())
    }

    // A variable can be changed after evaluation.
    // Then the result will change in the symbol table as well after later re-calculation
    @Test
    fun changeVarCheck() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = "1"; }
            feature b: Ranges::RealInRange { :>> range = "2"; }
            feature c: Ranges::RealInRange { :>> range = "3"; }
            feature d: ScalarValues::Real = a+b*c;
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(7.0, solver.getVariable("d")!!.vectorQuantity.getMaxAsDouble(), 0.0000001)
        // now we change 'a' to 10, and set d to any real.
        letVar("a", builder.real(10.0))
        letVar("d", builder.Reals)
        assertEquals(10.0, solver.getVariable("a")!!.min(), 0.0000001)
        // A new evaluation must again change d to now 16.
        runlevel = Runlevel.VARIANCE_CHECKED
        solver.propagate()
        val d = solver.getVariable("d")!!.vectorQuantity.value as AADD.Leaf
        assertEquals(16.0, d.central)
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
        assertEquals(builder.False, solver.getVariable("a")!!.bdd())
        assertEquals(builder.True, solver.getVariable("b")!!.bdd())
        assertNoIssues()
    }

    /** The function sum_i computes the sum over an expression from an initial value to an end value */
    @Test
    fun sumFunctionTest()  = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Real; 
            feature a: ScalarValues::Real = sum_i(1.0, 9.0, i);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(45.0, solver.getVariable("a")!!.min(), 0.00000001)
        assertNoIssues()
    }


    /** The function sum_i computes the sum over an expression from an initial value to an end value */
    @Test
    fun sumFunctionTestIdd() = testSession("ScalarValues") {
        loadKerML("""
                feature i: ScalarValues::Integer;
                feature a: ScalarValues::Integer = sum_i(1, 9, i);
            """)
        solver.propagate()
        assertEquals(45, solver.getVariable("a")!!.idd().getRange().min)
        assertNoIssues()
    }


    /** The function sum_i computes the sum over an expression from an initial value to an end value */
    @Test
    fun sumFunctionTestRange() = testSession("ScalarValues") {
        loadKerML("""
                feature i: ScalarValues::Real;
                feature a: ScalarValues::Real = sum_i(1.0, oneOf(7.0 .. 9.0), i);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(28.0, solver.getVariable("a")!!.min(), 0.00000001)
        assertEquals(45.0, solver.getVariable("a")!!.max(), 0.00000001)
    }


    @Test
    fun sumFunctionTestRangeExpr()  = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature s: ScalarValues::Real = 10.0;
            feature MAC_notb: ScalarValues::Real = sum_i( 0.0, 3.0, s*i );
        """)
        solver.propagate()
        assertEquals(60.0, solver.getVariable("MAC_notb")!!.min(), 0.00001)
        assertEquals(60.0, solver.getVariable("MAC_notb")!!.max(), 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    // Checks function calls of ITE: ITE(a>b, a+b, ITE(c<5,d,pi)) ====
    @Test
    fun iteFunctionTest() = testSession("ScalarValues", "Ranges", runlevel = Runlevel.VARIABLES) {
        loadKerML("""
            feature y: ScalarValues::Real= ITE(a>c, d, 3.14); 
            feature a: Ranges::RealInRange { :>> range = "1.0..1.0"; }
            feature c: Ranges::RealInRange { :>> range = "0.0..100.0"; }; 
            feature d: Ranges::RealInRange {:>> range = "3.0";}
            feature unknown: ScalarValues::Boolean; 
        """, Runlevel.VARIABLES)
        assertEquals(1, solver.getVariable("y")!!.aadd().height())
        // Simple: knwon Boolean constants
        loadKerML("feature zb: ScalarValues::Boolean = ITE(true, true, false).")
        assertEquals(solver.getVariable("zb")!!.bdd(), builder.True)
        // unknown decision variable with two ScalarValues::Boolean parameters
        loadKerML("feature zbunknown: ScalarValues::Boolean = ITE(unknown, true, false).")
        assertEquals(1, solver.getVariable("zbunknown")!!.bdd().height())
        // unknown decision variable with two real parameters
        loadKerML("feature z: ScalarValues::Real = ITE(unknown, 1.0, 2.0).")
        assertEquals(1, solver.getVariable("z")!!.aadd().height())
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
        assertEquals(0.0, solver.getVariable("c")!!.min(), .00001)
        assertEquals(0.0, solver.getVariable("c")!!.max(), .00001)
    }

    // different names create two uncorrelated noise symbols.
    @Test
    fun rangeMinusUncorrelated2Test()  = testSession("ScalarValues")  {
        loadKerML("""
            feature a: ScalarValues::Real = oneOf(1.0 .. 2.0);
            feature b: ScalarValues::Real = oneOf(1.0 .. 2.0);
            feature c: ScalarValues::Real = a - b; 
        """, Runlevel.ALL)
        val a = solver.getVariable("a")!!
        assertNotNull(a)
        val b = solver.getVariable("b")!!
        assertNotNull(b)
        val c = solver.getVariable("c")!!
        assertNoIssues()
        assertEquals(-1.0, c.min(), .00001)
        assertEquals(1.0, c.max(), .00001)
    }


    @Test
    fun rangeNewSyntaxSimpleInteger() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = oneOf(1..2);
            feature b: ScalarValues::Integer = oneOf(1..2);
            feature c: ScalarValues::Integer = a - b;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(-1.0, solver.getVariable("c")!!.min(), .00001)
        assertEquals(1.0, solver.getVariable("c")!!.max(), .00001)
    }

    @Test
    fun rangeNewSyntaxWithFunctions()  = testSession("ScalarValues") {

        loadKerML("""
            feature a: ScalarValues::Real = sqrt( oneOf(9.0 .. 25.0) );
            feature b: ScalarValues::Real = sqr( oneOf(2.0 .. 3.0) );
            feature c: ScalarValues::Real = a + b; 
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(7.0, solver.getVariable("c")!!.min(), .00001)
        assertEquals(14.0, solver.getVariable("c")!!.max(), .00001)
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
        """)
        solver.propagate()
        assertIssue("Cyclic")
    }
}
