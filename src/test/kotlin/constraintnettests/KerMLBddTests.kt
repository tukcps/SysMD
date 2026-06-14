package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.letVar
import io.github.tukcps.aadd.BDD
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

@Suppress("UNUSED_VARIABLE", "DEPRECATION")
class KerMLBddTests {

    /**
     * BDD are entered into the variable list.
     * Feature can create a variable x: Boolean, it then holds
     * a BDD with just height 1, and the index of the root is the index of the variable.
     * Leaves are true and false.
     */
    @Test
    fun bddVariableCreatedTest() = testSession("ScalarValues") {
        loadKerML("feature x: ScalarValues::Boolean;", Runlevel.VARIABLES)
        assertEquals(1, solver.getVariable("x")!!.bdd().height())
        loadKerML("feature a: ScalarValues::Boolean = x;", Runlevel.VARIABLES)
        assertTrue((solver.getVariable("a")!!.bdd().height() == 1))
        loadKerML("feature b: ScalarValues::Boolean = not(a);", Runlevel.VARIABLES)
        assertEquals(1, solver.getVariable("x")!!.bdd().height())
        assertTrue((solver.getVariable("a")!!.bdd().height() == 1))
        val aAndB = solver.getVariable("a")!!.bdd() and solver.getVariable("b")!!.bdd()
        runlevel = Runlevel.VARIANCE_CHECKED
        assertEquals(
            builder.False,
            solver.getVariable("a")!!.bdd() and solver.getVariable("b")!!.bdd()
        )
        // println(symbolTableInfo()) ; println(conds)
    }

    /**
     * Domain constraint is considered properly.
     */
    @Test
    fun bddVariableCreatedTestWithSubtype() = testSession("ScalarValues") {
        loadKerML("feature x: ScalarValues::Boolean(true);", Runlevel.ALL)
        val x = solver.getVariable("x")!!.bdd().evaluate()
        assertSame(builder.True, x)
        // ToDo: we must check that after considering known value x can be reduced to True.
        // But x should stay as it is as we might change its value interactively.
        // (It is a design decision ... can be adapted if needed to evaluate x as well)
        loadKerML("feature a: ScalarValues::Boolean(false);")
        assertTrue((solver.getVariable("a")!!.bdd().evaluate() === builder.False))
        loadKerML("feature b: ScalarValues::Boolean;")
        assertTrue((solver.getVariable("b")!!.bdd().height() == 1))
        assertTrue(((solver.getVariable("a")!!.bdd() and solver.getVariable("x")!!.bdd()).evaluate() === builder.False))
    }



    @Test
    @Ignore
    fun setAndEvaluateVariableTestWithComplexBDD() {
        testSession("ScalarValues") {
            loadKerML("feature ca: ScalarValues::Boolean(false);")
            loadKerML("feature b: ScalarValues::Boolean;")
            loadKerML("feature cc: ScalarValues::Boolean(true);")
            loadKerML("feature a: ScalarValues::Boolean;")
            loadKerML("feature c: ScalarValues::Boolean;")
            loadKerML("feature ccomplexBDD: ScalarValues::Boolean(true) = (ca and cc) or (not(b) and not(ca));")
            loadKerML("feature complexBDD: ScalarValues::Boolean(true) = (a and c) or (not(b) and not(a));")

            assertSame(solver.getVariable("ca")!!.bdd().evaluate(), builder.False)
            assertEquals(1, (solver.getVariable("ccomplexbdd")!!.bdd().evaluate()).height())

            letVar("b", builder.False)
            letVar("a", builder.False)

            assertSame(solver.getVariable("ccomplexbdd")!!.bdd().evaluate(), builder.True)
            assertSame(solver.getVariable("complexbdd")!!.bdd().evaluate(), builder.True)
        }
    }

    @Test
    fun solveAstBDD() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean; 
            feature b: ScalarValues::Boolean;
            feature c: ScalarValues::Boolean(false);
            feature bdd: ScalarValues::Boolean(true) = a and (b or c);
        """)
        assertNoIssues()
        solver.propagate()
        val result = solver.getVariable("bdd")!!.ast!!.solveAst()
        assertSame(builder.True, result as BDD)
        assertEquals(builder.True, solver.getVariable("a")?.vectorQuantity?.value)
        assertEquals(builder.True, solver.getVariable("b")?.vectorQuantity?.value)
        // assertTrue(builder.conds.getCondition(1) === builder.True)
        // assertTrue(builder.conds.getCondition(2) === builder.True)
        // assertTrue(builder.conds.getCondition(3) === builder.False)
    }

    @Test
    @Ignore
    fun solveAstBDDFalse() = testSession("ScalarValues") {
        loadKerML(
            """
            feature a: ScalarValues::Boolean;
            feature b: ScalarValues::Boolean;
            feature c: ScalarValues::Boolean(true);
            feature bdd: ScalarValues::Boolean(false) = a and (b or c);"""
        )
        solver.propagate()
        val result = solver.getVariable("bdd")!!.ast!!.solveAst()
        assertSame(result as BDD, builder.False)
        assertSame(builder.conds.getCondition(1), builder.False)
        assertSame(builder.conds.getCondition(2), builder.Bool)
        assertSame(builder.conds.getCondition(3), builder.True)
    }
}