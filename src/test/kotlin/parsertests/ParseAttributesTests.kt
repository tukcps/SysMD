package parsertests

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.BDD
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.defScalarVar
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.letVar
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParseAttributesTests {

    /**
     * We can use "string"s that are recognized as a literal.
     * Strings are saved in the AST as leaf with idType=STRING.
     * The string literal is in the field id.
     */
    @Test
    fun stringLiteralsTest() = testSession {
        settings.catchExceptions = false
        +"feature x: ScalarValues::String = \"test2\" ;"
        assertEquals(0, status.exceptions.size, "error messages: ${status.exceptions}")
        val feature = global.resolve<Feature>("x")
        assertEquals("x", feature!!.declaredName)
        assertEquals("\"test2\"", feature.variable!!.dependency.trim())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    /**
     * model.defVar directly enters variable into symbol table.
     * model.findProperty directly gets variable from symbol table.
     */
    @Test
    fun defVarCheck() = testSession {
        defScalarVar("a", "1.0", type = "ScalarValues::Real")
        defScalarVar("b", "2.0", type = "ScalarValues::Real")
        defScalarVar("c", "X", type = "ScalarValues::Boolean")
        initialize()
        assertEquals(1.0, global.resolveVar("a")!!.min())
        assertEquals(2.0, global.resolveVar("b")!!.min())
        val found = global.resolveVar("c")
        assertEquals(builder.variable("x"), found!!.vectorQuantity.value as BDD)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    // Definition of variables initializes its value based on an expression.
    @Test
    fun constantExprCheck() = testSession(catchExceptions = false) {
        loadSysMD("attribute a: ScalarValues::Real = -1.0+2.0*- 3.0/(3.0-2.0).")
        propagate()
        val a = global.resolveVar("a")!!.aadd()
        // println(a.toIteString())
        // println(builder.conds)
        // println(builder.noiseVars)
        // builder.toStringVerbose
        // println(a.getRange())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(-7.0, a.getRange().min, 0.000001)
    }

    @Test
    fun constraintTestReal() = testSession {
        loadSysMD("attribute a: ScalarValues::Real(1 .. 2); attribute b: ScalarValues::Real(1.0 .. 2.0).")
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(1.0, a.min())
        assertEquals(2.0, a.max())
        assertEquals(1.0, b.min())
        assertEquals(2.0, b.max())
    }

    @Test
    fun constraintTestRealStars() = testSession(catchExceptions = false) {
        loadSysMD("attribute a: ScalarValues::Real(1 .. *); attribute b: ScalarValues::Real(* .. 2.0).")
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        propagate()
        assertEquals(1.0, a.min())
        assertTrue(settings.maxReal < a.max())
        assertTrue(settings.minReal > b.min())
        assertEquals(2.0, b.max())
    }

    @Test
    fun constraintTestInteger() = testSession {
        loadSysMD("attribute a: ScalarValues::Integer(1 .. 2).")
        val a = global.resolveVar("a")!!
        propagate()
        assertEquals(1.0, a.min())
        assertEquals(2.0, a.max())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    @Test
    fun constraintTestIntegerStar() = testSession {
        loadSysMD("attribute a: ScalarValues::Integer(-* .. *).")
        val a = global.resolveVar("a")!!
        propagate()
        assertTrue(settings.minInt.toDouble() >= a.min())
        assertTrue(settings.maxInt.toDouble() <= a.max())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    @Test
    fun boolSpecTestBoolean() = testSession {
        loadSysMD("attribute a: ScalarValues::Boolean(true); attribute b: ScalarValues::Boolean(false).")
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        propagate()
        assertEquals(builder.True, a.vectorQuantity.value)
        assertEquals(builder.False, b.vectorQuantity.value)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    // Definition of variables initializes its value based on a Boolean expression.
    @Test
    fun boolDefExprCheck() = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Real(1); 
            attribute b: ScalarValues::Real(2); 
            attribute d: ScalarValues::Boolean = (a > b) & false; 
        """)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        propagate()
        assertEquals(builder.False, global.resolveVar("d")!!.bdd())
    }

    // Definition of variables initializes its value based on an expression.
    @Test
    fun constantsCheck() = testSession("Math") {
        loadSysMD("feature a: ScalarValues::Real = Math::pi + Math::e;")
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        propagate()
        val a = global.resolve<Feature>("a")!!.variable!!.aadd()
        assertEquals(Math.PI + Math.E, a.getRange().min, 0.00001)
    }


    // A variable can be changed after evaluation.
    // Then the result will change in symbol table as well after subsequent re-calculation
    @Test
    fun changeVarCheck() = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Real(1);
            attribute b: ScalarValues::Real(2);
            attribute c: ScalarValues::Real(3);
            attribute d: ScalarValues::Real = a+b*c;""")
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        propagate()
        assertEquals(7.0, global.resolveVar("d")!!.vectorQuantity.getMaxAsDouble(), 0.0000001)
        // now we change 'a' to 10, and set d to any real.
        letVar("a", builder.scalar(10.0))
        letVar("d", builder.Reals)
        assertEquals(10.0, global.resolveVar("a")!!.min(), 0.0000001)
        // A new evaluation must again change d to now 16.
        propagate()
        val d = global.resolveVar("d")!!.vectorQuantity.value as AADD.Leaf
        assertEquals(16.0, d.central)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    // Tests of 'not' function in parser
    @Test
    fun notFunctionTest() = testSession {
        loadSysMD("""
            feature a: ScalarValues::Boolean = not(true);
            feature b: ScalarValues::Boolean = not(false);""")
        propagate()
        assertEquals(builder.False, global.resolve<Feature>("a")!!.variable!!.bdd())
        assertEquals(builder.True, global.resolve<Feature>("b")!!.variable!!.bdd())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** The function sum_i computes the sum over an expression from an initial value to an end value */
    @Test
    fun sumFunctionTest()  = testSession {
        loadSysMD("""
            attribute i: ScalarValues::Real; 
            attribute a: ScalarValues::Real = sum_i(1.0, 9.0, i).
            """.trimMargin())
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
        assertEquals(45.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00000001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    /** The function sum_i computes the sum over an expression from an initial value to an end value */
    @Test
    fun sumFunctionTestIdd() = testSession {
        loadSysMD(
            """
            attribute i: ScalarValues::Integer.
            attribute a: ScalarValues::Integer = sum_i(1, 9, i)."""
        )
        propagate()
        assertEquals(45, global.resolveVar("a")!!.idd().getRange().min)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    /** The function sum_i computes the sum over an expression from an initial value to an end value */
    @Test
    fun sumFunctionTestRange() = testSession {
        loadSysMD("""
                attribute i: ScalarValues::Real;
                attribute a: ScalarValues::Real = sum_i(1.0, [7.0 .. 9.0], i);
                """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(28.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00000001)
        assertEquals(45.0, global.resolveVar("a")!!.aadd().getRange().max, 0.00000001)
    }


    @Test
    fun sumFunctionTestRangeExpr()  = testSession {
        loadSysMD("""
            attribute i: ScalarValues::Real;
            attribute s: ScalarValues::Real = 10.0;
            attribute MAC_notb: ScalarValues::Real = sum_i( 0.0, 3.0, s*i );""")
        // println("MAC_notb=" + resolveName<Expression>("MAC_notb").aadd().getRange().min)
        initialize()
        propagate()
        assertEquals(60.0, global.resolveVar("MAC_notb")!!.aadd().getRange().min, 0.00001)
        assertEquals(60.0, global.resolveVar("MAC_notb")!!.aadd().getRange().max, 0.00001)
        //assertEquals(0, status.errors.size, "Error messages: ${status.errors}")
    }

    // Checks function calls of ITE: ITE(a>b, a+b, ITE(c<5,d,pi)) ====
    @Test
    fun iteFunctionTest() = testSession("ScalarValues", catchExceptions = false) {
        loadSysMD("""
            attribute y: ScalarValues::Real= ITE(a>c, d, 3.14); 
            attribute a: ScalarValues::Real(1.0);
            attribute c: ScalarValues::Real(0.0 .. 100.0); 
            attribute d: ScalarValues::Real(3.0);
            attribute unknown: ScalarValues::Boolean; 
            """)
        // println(resolveName<Expression>(global, "y"))
        assertEquals(1, global.resolveVar("y")!!.aadd().height())
        // Simple: knwon Boolen constants
        loadSysMD("attribute zb: ScalarValues::Boolean = ITE(true, true, false).")
        assertTrue(global.resolveVar("zb")!!.bdd() == builder.True)
        // unknown decision variable with two ScalarValues::Boolean parameters
        loadSysMD("attribute zbunknown: ScalarValues::Boolean = ITE(unknown, true, false).")
        assertEquals(1, global.resolveVar("zbunknown")!!.bdd().height())
        // unknown decision variable with two real parameters
        loadSysMD("attribute z: ScalarValues::Real = ITE(unknown, 1.0, 2.0).")
        assertEquals(1, global.resolveVar("z")!!.aadd().height())
    }


    @Test
    // two uncorrelated noise symbols are created by parameter -1.
    fun rangeMinusUncorrelatedTest() = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Real= [1.0 .. 2.0].
            attribute b: ScalarValues::Real= a.
            attribute c: ScalarValues::Real= a - b.""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0.0, global.resolveVar("c")!!.min(), .00001)
        assertEquals(0.0, global.resolveVar("c")!!.max(), .00001)
    }

    // two uncorrelated noise symbols are created by different names.
    @Test
    fun rangeMinusUncorrelated2Test()  = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Real = [1.0 .. 2.0].
            attribute b: ScalarValues::Real = [1.0 .. 2.0].
            attribute c: ScalarValues::Real = a - b.""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(-1.0, global.resolveVar("c")!!.min(), .00001)
        assertEquals(1.0, global.resolveVar("c")!!.max(), .00001)
    }


    @Test
    fun rangeNewSyntaxSimpleInteger() = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Integer = [1..2].
            attribute b: ScalarValues::Integer = [1..2].
            attribute c: ScalarValues::Integer = a - b.""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(-1.0, global.resolveVar("c")!!.min(), .00001)
        assertEquals(1.0, global.resolveVar("c")!!.max(), .00001)
    }

    @Test
    fun rangeNewSyntaxWithFunctions()  = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Real = sqrt([9.0..25.0]).
            attribute b: ScalarValues::Real = sqr([2.0..3.0]).
            attribute c: ScalarValues::Real = a + b.""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(7.0, global.resolveVar("c")!!.min(), .00001)
        assertEquals(14.0, global.resolveVar("c")!!.max(), .00001)
    }

    /**
     * Checks whether the quantors all and one are recognized and property kind is set correctly.
     */
    @Test
    fun allParseTest() = testSession {
        loadSysMD(input = """
            attribute a: one ScalarValues::Real(1.0 .. 2.0).
            attribute b: all ScalarValues::Real(2.0 .. 3.0).
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * A cyclic dependency in a single statement is detected and an exception is thrown.
     * Alternatively, an error is reported in status.
     */
    @Test
    fun cyclicDependencyTest() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValues { datatype ScalarValue; datatype Real :> ScalarValue; datatype Integer :> ScalarValue; }
            attribute b: ScalarValues::Real;
            attribute a: ScalarValues::Real = a;
            """)
        assertEquals(1, status.exceptions.size, status.exceptions.toString())
    }
}
