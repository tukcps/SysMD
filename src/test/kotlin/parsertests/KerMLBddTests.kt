@file:Suppress("unused")

package parsertests

import io.github.tukcps.aadd.BDD
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.letVar
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
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
    @Test fun bddVariableCreatedTest() = testSession("ScalarValues") {
        loadKerML("feature x: ScalarValues::Boolean;")
        assertTrue(global.resolveVar("x")!!.bdd().height() == 1)
        loadKerML("feature a: ScalarValues::Boolean = x;")
        assertTrue((global.resolveVar("a")!!.bdd().height() == 1))
        loadKerML("feature b: ScalarValues::Boolean = not(a).")
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
    @Test fun bddVariableCreatedTestWithSubtype() = testSession("ScalarValues") {
        loadKerML("feature x: ScalarValues::Boolean(true).")
        val test = global.resolveVar("x")!!.bdd().evaluate()
        assertSame(global.resolveVar("x")!!.bdd().evaluate(), builder.True)
        // ToDo: we must check that after considering known value x can be reduced to True.
        // But x should stay as it is as we might change its value interactively.
        // (It is a design decision ... can be adapted if needed to evaluate x as well)
        loadKerML("feature a: ScalarValues::Boolean(false).")
        assertTrue((global.resolveVar("a")!!.bdd().evaluate() === builder.False))
        loadKerML("feature b: ScalarValues::Boolean.")
        assertTrue((global.resolveVar("b")!!.bdd().height() == 1))
        assertTrue(((global.resolveVar("a")!!.bdd() and global.resolveVar("x")!!.bdd()).evaluate() === builder.False))
    }



    @Test @Ignore
    fun setAndEvaluateVariableTestWithComplexBDD() {
        testSession("ScalarValues") {
            loadKerML("feature ca: ScalarValues::Boolean(false);")
            loadKerML("feature b: ScalarValues::Boolean;")
            loadKerML("feature cc: ScalarValues::Boolean(true);")
            loadKerML("feature a: ScalarValues::Boolean;")
            loadKerML("feature c: ScalarValues::Boolean;")
            loadKerML("feature ccomplexBDD: ScalarValues::Boolean(true) = (ca and cc) or (not(b) and not(ca));")
            loadKerML("feature complexBDD: ScalarValues::Boolean(true) = (a and c) or (not(b) and not(a));")

            assertSame(global.resolveVar("ca")!!.bdd().evaluate(), builder.False)
            assertTrue((global.resolveVar("ccomplexbdd")!!.bdd().evaluate()).height() == 1)

            letVar("b", builder.False)
            letVar("a", builder.False)

            assertSame(global.resolveVar("ccomplexbdd")!!.bdd().evaluate(), builder.True)
            assertSame(global.resolveVar("complexbdd")!!.bdd().evaluate(), builder.True)
        }
    }

    @Test
    fun solveAstBDD() = testSession("ScalarValues") {
        loadKerML( """
                feature a: ScalarValues::Boolean; 
                feature b: ScalarValues::Boolean;
                feature c: ScalarValues::Boolean(false);
                feature bdd: ScalarValues::Boolean(true) = a and (b or c);
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

    @Test @Ignore
    fun solveAstBDDFalse() = testSession("ScalarValues") {
        loadKerML(
            """
            feature a: ScalarValues::Boolean;
            feature b: ScalarValues::Boolean;
            feature c: ScalarValues::Boolean(true);
            feature bdd: ScalarValues::Boolean(false) = a and (b or c);"""
        )
        propagate()
        val result = global.resolveVar("bdd")!!.ast!!.solveAst()
        assertTrue(result as BDD === builder.False)
        assertSame(builder.conds.getCondition(1), builder.False)
        assertSame(builder.conds.getCondition(2), builder.Bool)
        assertSame(builder.conds.getCondition(3), builder.True)
    }

    @Test
    fun solveAstBDDMultipleSolutions()  = testSession("ScalarValues") {
        loadKerML(
            """
            feature a: ScalarValues::Boolean;
            feature b: ScalarValues::Boolean(false);
            feature c: ScalarValues::Boolean;
            feature d: ScalarValues::Boolean;
            feature bdd: ScalarValues::Boolean(true) = (a and (b or c)) or d;"""
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
