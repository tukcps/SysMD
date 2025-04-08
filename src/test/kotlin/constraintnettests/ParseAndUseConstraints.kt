package constraintnettests

import io.github.tukcps.aadd.AADD
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.getOwned
import com.github.tukcps.sysmd.services.letVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class ParseAndUseConstraintsTests {

    @Test
    fun constraintTestReal() = testSession("ScalarValues") {
        loadKerML("""
                feature a: ScalarValues::Real {:>> range = "1 .. 2";}
                feature b: ScalarValues::Real {:>> range = "1.0 .. 2.0";}
            """)
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, a.min())
        assertEquals(2.0, a.max())
        assertEquals(1.0, b.min())
        assertEquals(2.0, b.max())
    }

    @Test
    fun constraintTestRealStars() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real {:>> range = "1 .. *";}
            feature b: ScalarValues::Real {:>> range = "* .. 2.0";}
        """)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        propagate()
        assertEquals(1.0, a.min())
        assertTrue(settings.maxReal < a.max() as Double)
        assertTrue(settings.minReal > b.min() as Double)
        assertEquals(2.0, b.max())
    }

    @Test
    fun constraintTestInteger() = testSession("Ranges") {
        loadKerML("""
            feature a: ScalarValues::Integer, Ranges::InRange { 
                :>> min = 1; 
                :>> max = 2; 
            } 
        """)
        val a: Feature? = global.resolve("a")
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1L, a?.variable?.min())
        assertEquals(2L, a?.variable?.max())
    }


    @Test
    fun constraintTestIntegerStar() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature a: ScalarValues::Integer;
        """)
        val a = global.resolveVar("a")
        assertNotNull(a)
        propagate()
        assertTrue(settings.minInt >= a.min() as Long)
        assertTrue(settings.maxInt <= a.max() as Long)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }


    @Test
    fun boolSpecTestBoolean() = testSession("ScalarValues") {
        loadKerML("""
            inv a;
            inv b false;
        """)
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        propagate()
        assertEquals(builder.True, a.vectorQuantity.value)
        assertEquals(builder.False, b.vectorQuantity.value)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }


    // Definition of variables initializes its value based on a Boolean expression.
    @Test
    fun boolDefExprCheck() = testSession("ScalarValues") {
        loadKerML("""
                feature a: ScalarValues::Real(1); 
                feature b: ScalarValues::Real(2); 
                feature d: ScalarValues::Boolean = (a > b) & false; 
            """)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        propagate()
        assertEquals(builder.False, global.resolveVar("d")!!.bdd())
    }

    // Definition of variables initializes its value based on an expression.
    @Test
    fun constantsCheck() = testSession("ScalarValues", "Math") {
        loadKerML("feature a: ScalarValues::Real = Math::pi + Math::e;")
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        propagate()
        val a = global.resolve<Feature>("a")!!.variable!!.aadd()
        assertEquals(Math.PI + Math.E, a.getRange().min, 0.00001)
    }

    /**
     * Checks whether min and max are recognized.
     */
    @Test
    fun partsAttributeWithRangeTest() = testSession("Ranges") {
        loadKerML("""
            feature b: ScalarValues::Real, Ranges::InRange {
                :>> min = 2.0;
                :>> max = 3.0;
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val b = global.resolve<Feature>("b")
        assertNotNull(b)
        assertNotNull(b.variable)
        assertTrue(b.getOwned<Feature>("min")!!.variable !in repo.schedule)
        assertTrue(b.getOwned<Feature>("max")!!.variable !in repo.schedule)
        assertEquals(2.0, b.variable?.min())
        assertEquals(3.0, b.variable?.max())
    }

    // A variable can be changed after evaluation.
    // Then the result will change in symbol table as well after later re-calculation
    @Test
    fun changeVarCheck() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real(1);
            feature b: ScalarValues::Real(2);
            feature c: ScalarValues::Real(3);
            feature d: ScalarValues::Real = a+b*c;
        """)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        propagate()
        assertEquals(7.0, global.resolveVar("d")!!.vectorQuantity.getMaxAsDouble(), 0.0000001)
        // now we change 'a' to 10, and set d to any real.
        letVar("a", builder.real(10.0))
        letVar("d", builder.Reals)
        assertEquals(10.0, global.resolveVar("a")!!.min(), 0.0000001)
        // A new evaluation must again change d to now 16.
        propagate()
        val d = global.resolveVar("d")!!.vectorQuantity.value as AADD.Leaf
        assertEquals(16.0, d.central)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    // Tests of 'not' function in parser
    @Test
    fun notFunctionTest() = testSession("ScalarValues") {
        loadKerML("""
                feature a: ScalarValues::Boolean = not(true);
                feature b: ScalarValues::Boolean = not(false);
            """)
        propagate()
        assertEquals(builder.False, global.resolve<Feature>("a")!!.variable!!.bdd())
        assertEquals(builder.True, global.resolve<Feature>("b")!!.variable!!.bdd())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** The function sum_i computes the sum over an expression from an initial value to an end value */
    @Test
    fun sumFunctionTest()  = testSession("ScalarValues") {
        loadKerML("""
                feature i: ScalarValues::Real; 
                feature a: ScalarValues::Real = sum_i(1.0, 9.0, i);
            """)
        propagate()
        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
        assertEquals(45.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00000001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }


    /** The function sum_i computes the sum over an expression from an initial value to an end value */
    @Test
    fun sumFunctionTestIdd() = testSession("ScalarValues") {
        loadKerML("""
                feature i: ScalarValues::Integer;
                feature a: ScalarValues::Integer = sum_i(1, 9, i);
            """)
        propagate()
        assertEquals(45, global.resolveVar("a")!!.idd().getRange().min)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }


    /** The function sum_i computes the sum over an expression from an initial value to an end value */
    @Test
    fun sumFunctionTestRange() = testSession("ScalarValues") {
        loadKerML("""
                    feature i: ScalarValues::Real;
                    feature a: ScalarValues::Real = sum_i(1.0, oneOf(7.0 .. 9.0), i);
            """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(28.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00000001)
        assertEquals(45.0, global.resolveVar("a")!!.aadd().getRange().max, 0.00000001)
    }


    @Test
    fun sumFunctionTestRangeExpr()  = testSession("ScalarValues") {
        loadKerML("""
                feature i: ScalarValues::Real;
                feature s: ScalarValues::Real = 10.0;
                feature MAC_notb: ScalarValues::Real = sum_i( 0.0, 3.0, s*i );
            """)
        propagate()
        assertEquals(60.0, global.resolveVar("MAC_notb")!!.aadd().getRange().min, 0.00001)
        assertEquals(60.0, global.resolveVar("MAC_notb")!!.aadd().getRange().max, 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    // Checks function calls of ITE: ITE(a>b, a+b, ITE(c<5,d,pi)) ====
    @Test
    fun iteFunctionTest() = testSession("ScalarValues") {
        loadKerML("""
                feature y: ScalarValues::Real= ITE(a>c, d, 3.14); 
                feature a: ScalarValues::Real, Ranges::Range { :>> min = 1.0; :>> max = 1.0; }
                feature c: ScalarValues::Real, Ranges::Range { :>> min = 0.0; :>> max = 100.0; }; 
                feature d: ScalarValues::Real(3.0);
                feature unknown: ScalarValues::Boolean; 
                """)
        assertEquals(1, global.resolveVar("y")!!.aadd().height())
        // Simple: knwon Boolean constants
        loadKerML("feature zb: ScalarValues::Boolean = ITE(true, true, false).")
        assertTrue(global.resolveVar("zb")!!.bdd() == builder.True)
        // unknown decision variable with two ScalarValues::Boolean parameters
        loadKerML("feature zbunknown: ScalarValues::Boolean = ITE(unknown, true, false).")
        assertEquals(1, global.resolveVar("zbunknown")!!.bdd().height())
        // unknown decision variable with two real parameters
        loadKerML("feature z: ScalarValues::Real = ITE(unknown, 1.0, 2.0).")
        assertEquals(1, global.resolveVar("z")!!.aadd().height())
    }


    @Test
    // two uncorrelated noise symbols are created by parameter -1.
    fun rangeMinusUncorrelatedTest() = testSession("ScalarValues") {
        loadKerML("""
                feature a: ScalarValues::Real= oneOf(1.0 .. 2.0);
                feature b: ScalarValues::Real= a;
                feature c: ScalarValues::Real= a - b;
            """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0.0, global.resolveVar("c")!!.min(), .00001)
        assertEquals(0.0, global.resolveVar("c")!!.max(), .00001)
    }

    // different names create two uncorrelated noise symbols.
    @Test
    fun rangeMinusUncorrelated2Test()  = testSession("ScalarValues")  {
        loadKerML("""
            feature a: ScalarValues::Real = oneOf(1.0 .. 2.0);
            feature b: ScalarValues::Real = oneOf(1.0 .. 2.0);
            feature c: ScalarValues::Real = a - b; 
        """)
       //  propagate()
        val a = global.resolveVar("a")!!
        assertNotNull(a)
        val b = global.resolveVar("b")!!
        assertNotNull(b)
        val c = global.resolveVar("c")!!
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
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
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(-1.0, global.resolveVar("c")!!.min(), .00001)
        assertEquals(1.0, global.resolveVar("c")!!.max(), .00001)
    }

    @Test
    fun rangeNewSyntaxWithFunctions()  = testSession("ScalarValues") {

        loadKerML("""
            feature a: ScalarValues::Real = sqrt( oneOf(9.0 .. 25.0) );
            feature b: ScalarValues::Real = sqr( oneOf(2.0 .. 3.0) );
            feature c: ScalarValues::Real = a + b; 
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(7.0, global.resolveVar("c")!!.min(), .00001)
        assertEquals(14.0, global.resolveVar("c")!!.max(), .00001)
    }

    /**
     * A cyclic dependency in a single statement is detected and an exception is thrown.
     * Alternatively, an error is reported in status.
     */
    @Test
    fun cyclicDependencyTest() = testSession("ScalarValues") {
        loadKerML("""
                feature b: ScalarValues::Real;
                feature a: ScalarValues::Real = a;
            """)
        assertEquals(1, status.issues.size, status.issues.toString())
    }
}
