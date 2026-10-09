package constraintnettests

import util.variable
import util.assertBounds
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.letVar
import io.github.tukcps.aadd.DDBuilder.BoolMath.and
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
        assertEquals(1, solver.variable("x").bdd().height())
        loadKerML("feature a: ScalarValues::Boolean = x;", Runlevel.VARIABLES)
        assertTrue((solver.variable("a").bdd().height() == 1))
        loadKerML("feature b: ScalarValues::Boolean = not(a);", Runlevel.VARIABLES)
        assertEquals(1, solver.variable("x").bdd().height())
        assertTrue((solver.variable("a").bdd().height() == 1))
        settings.runlevel = Runlevel.VARIANCE_CHECKED
        assertEquals(
            builder.Bool.False,
            solver.variable("a").bdd() and solver.variable("b").bdd()
        )
        // println(symbolTableInfo()) ; println(conds)
    }

    /**
     * Domain constraint is considered properly.
     */
    @Test
    fun bddVariableCreatedTestWithSubtype() = testSession("ScalarValues") {
        loadKerML("feature x: ScalarValues::Boolean(true);", Runlevel.ALL)
        val x = solver.variable("x").bdd().evaluate()
        assertSame(builder.Bool.True, x)
        // ToDo: we must check that after considering known value x can be reduced to True.
        // But x should stay as it is as we might change its value interactively.
        // (It is a design decision ... can be adapted if needed to evaluate x as well)
        loadKerML("feature a: ScalarValues::Boolean(false);")
        assertSame(builder.Bool.False, solver.variable("a").bdd().evaluate())

        loadKerML("feature b: ScalarValues::Boolean;")
        assertEquals(1, solver.variable("b").bdd().height())
        assertSame(builder.Bool.False, (solver.variable("a").bdd() and solver.variable("x").bdd()).evaluate())
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

            assertSame(builder.Bool.False, solver.variable("ca").bdd().evaluate())
            assertEquals(1, (solver.variable("ccomplexbdd").bdd().evaluate()).height())

            letVar("b", builder.Bool.False)
            letVar("a", builder.Bool.False)

            assertSame(builder.Bool.True, solver.variable("ccomplexbdd").bdd().evaluate())
            assertSame(builder.Bool.True, solver.variable("complexbdd").bdd().evaluate())
        }
    }

    @Test
    fun solveAstBDD() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean; 
            feature b: ScalarValues::Boolean;
            inv false c;
            inv bdd = a and (b or c);
        """)
        assertNoIssues()
        solver.propagate()
        val result = solver.variable("bdd").ast!!.solveAst()
        assertSame(builder.Bool.True, result)
        assertEquals(builder.Bool.True, solver.getVariable("a")?.vectorQuantity?.value)
        assertEquals(builder.Bool.True, solver.getVariable("b")?.vectorQuantity?.value)
        // assertTrue(builder.conds.getCondition(1) === builder.Bool.True)
        // assertTrue(builder.conds.getCondition(2) === builder.Bool.True)
        // assertTrue(builder.conds.getCondition(3) === builder.Bool.False)
    }

    @Test
    @Ignore
    fun solveAstBDDFalse() = testSession("ScalarValues") {
        loadKerML(
            """
            feature a: ScalarValues::Boolean;
            feature b: ScalarValues::Boolean;
            inv c;
            inv false bdd = a and (b or c);
        """, Runlevel.ALL)
        assertNoIssues()
        val result = solver.variable("bdd").ast!!.solveAst()
        assertSame(builder.Bool.False, result)
        assertSame(builder.Bool.False, builder.conds.getCondition(1))
        assertSame(builder.Bool.All, builder.conds.getCondition(2))
        assertSame(builder.Bool.True, builder.conds.getCondition(3))
    }

    /**
     * Comparison of two AADDs that cannot be equal must return FALSE.
     * Tests that (p > p2) evaluates to False when p is in 2..4 and p2 = p + 1.
     * Problem was: correlation terms were lost, maybe by intersect operation.
     * Fixed: always new created in each iteration should be done once with fixed noise variable.
     * Remaining: Infeasible paths are not reduced; converted to True/False in toString.
     */
    @Test
    fun fail4() = testSession("Ranges") {
        loadKerML("""
            feature p:  Ranges::RealInRange {:>> range = 2 .. 4;}
            feature p2: ScalarValues::Real = p + 1.0;
            feature p3: ScalarValues::Boolean = ( p > p2 ).
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()

        val p = solver.variable("p").vectorQuantity.aadd()
        val p2 = solver.variable("p2").vectorQuantity.aadd()
        val p3 = solver.variable("p3").vectorQuantity.bdd()

        assertBounds(2.0..4.0, p)
        assertBounds(3.0..5.0, p2)
        assertEquals("False", p3.toString())
        assertNoIssues()
    }
}