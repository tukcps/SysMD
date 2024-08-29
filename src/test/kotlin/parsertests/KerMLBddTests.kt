package parsertests

import com.github.tukcps.aadd.BDD
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.letVar
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue


@Suppress("UNUSED_VARIABLE", "DEPRECATION")
class KerMLBddTests {

    /**
     * BDD are entered into the variable list.
     * Feature can create a variable x: Boolean, it then holds
     * a BDD with just height 1, and the index of the root is the index of the variable.
     * Leaves are true and false.
     */
    @Test fun bddVariableCreatedTest() = testSession {
        loadSysMD("attribute x: ScalarValues::Boolean;")
        assertTrue(global.resolveVar("x")!!.bdd().height() == 1)
        loadSysMD("attribute a: ScalarValues::Boolean = x;")
        assertTrue((global.resolveVar("a")!!.bdd().height() == 1))
        loadSysMD("attribute b: ScalarValues::Boolean = not(a).")
        assertTrue(global.resolveVar("x")!!.bdd().height() == 1)
        assertTrue((global.resolveVar("a")!!.bdd().height() == 1))
        initialize()
        val aAndB = global.resolveVar("a")!!.bdd() and global.resolveVar("b")!!.bdd()
        initialize()
        assertEquals(builder.False,
            global.resolveVar("a")!!.bdd() and global.resolveVar("b")!!.bdd()  )
        // println(symbolTableInfo()) ; println(conds)
    }

    /**
     * Domain constraint is considered properly.
     */
    @Test fun bddVariableCreatedTestWithSubtype() = testSession {
        loadSysMD("attribute x: ScalarValues::Boolean(true).")
        val test = global.resolveVar("x")!!.bdd().evaluate()
        assertSame(global.resolveVar("x")!!.bdd().evaluate(), builder.True)
        // ToDo: we must check that after considering known value x can be reduced to True.
        // But x should stay as it is as we might change its value interactively.
        // (It is a design decision ... can be adapted if needed to evaluate x as well)
        loadSysMD("attribute a: ScalarValues::Boolean(false).")
        assertTrue((global.resolveVar("a")!!.bdd().evaluate() === builder.False))
        loadSysMD("attribute b: ScalarValues::Boolean.")
        assertTrue((global.resolveVar("b")!!.bdd().height() == 1))
        assertTrue(((global.resolveVar("a")!!.bdd() and global.resolveVar("x")!!.bdd()).evaluate() === builder.False))
    }



    @Test @Disabled
    fun setAndEvaluateVariableTestWithComplexBDD() {
        testSession {
            + "attribute ca: ScalarValues::Boolean(false)."
            + "attribute b: ScalarValues::Boolean."
            + "attribute cc: ScalarValues::Boolean(true)."
            + "attribute a: ScalarValues::Boolean."
            + "attribute c: ScalarValues::Boolean."
            + "attribute ccomplexBDD: ScalarValues::Boolean(true) = (ca and cc) or (not(b) and not(ca))"
            + "attribute complexBDD: ScalarValues::Boolean(true) = (a and c) or (not(b) and not(a))"

            assertSame(global.resolveVar("ca")!!.bdd().evaluate(), builder.False)
            assertTrue((global.resolveVar("ccomplexbdd")!!.bdd().evaluate()).height() == 1)

            letVar("b", builder.False)
            letVar("a", builder.False)

            assertSame(global.resolveVar("ccomplexbdd")!!.bdd().evaluate(), builder.True)
            assertSame(global.resolveVar("complexbdd")!!.bdd().evaluate(), builder.True)
        }
    }

    @Test
    fun solveAstBDD() = testSession {
        loadSysMD( """
                attribute a: ScalarValues::Boolean; 
                attribute b: ScalarValues::Boolean;
                attribute c: ScalarValues::Boolean(false);
                attribute bdd: ScalarValues::Boolean(true) = a and (b or c);
        """.trimIndent())
        assertEquals(0, status.exceptions.size, "Messages: ${status.exceptions}")
        propagate()
        val result = global.resolveVar("bdd")!!.ast!!.solveAst()
        assertTrue(result as BDD === builder.True)
        assertEquals(builder.True, global.resolveVar("a")?.vectorQuantity?.value)
        assertEquals(builder.True, global.resolveVar("b")?.vectorQuantity?.value)
        // assertTrue(builder.conds.getCondition(1) === builder.True)
        // assertTrue(builder.conds.getCondition(2) === builder.True)
        // assertTrue(builder.conds.getCondition(3) === builder.False)
    }

    @Test @Disabled
    fun solveAstBDDFalse() = testSession {
        loadSysMD(
            """
            attribute a: ScalarValues::Boolean;
            attribute b: ScalarValues::Boolean;
            attribute c: ScalarValues::Boolean(true);
            attribute bdd: ScalarValues::Boolean(false) = a and (b or c);"""
        )
        propagate()
        val result = global.resolveVar("bdd")!!.ast!!.solveAst()
        assertTrue(result as BDD === builder.False)
        assertSame(builder.conds.getCondition(1), builder.False)
        assertSame(builder.conds.getCondition(2), builder.Bool)
        assertSame(builder.conds.getCondition(3), builder.True)
    }

    @Test
    fun solveAstBDDMultipleSolutions()  = testSession {
        loadSysMD(
            """
            attribute a: ScalarValues::Boolean;
            attribute b: ScalarValues::Boolean(false);
            attribute c: ScalarValues::Boolean;
            attribute d: ScalarValues::Boolean;
            attribute bdd: ScalarValues::Boolean(true) = (a and (b or c)) or d;"""
        )
        propagate()
        val hrm = global.resolveVar("bdd")!!.bdd().evaluate()
        val test = global.resolveVar("bdd")!!.ast!!.findAllPaths(
            global.resolveVar("bdd")!!.bdd().evaluate(),
            global.resolveVar("bdd")!!.boolSpecs[0])
        val result = global.resolveVar("bdd")!!.ast!!.solveAstWithAlternatives()
        val breakpoint = 1
    }
}
