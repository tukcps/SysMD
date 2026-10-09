package constraintnettests.bddtests

import util.variable
import util.assertBounds
import com.github.tukcps.sysmd.services.*
import io.github.tukcps.aadd.util.Assertions.assertEquals
import io.github.tukcps.aadd.values.bool.XBool
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("UNUSED_VARIABLE")
internal class OperationTests {

    @Test
    fun setVariableBDD() = testSession("ScalarValues") {
        defScalarVar("a", value = "X", "", "ScalarValues::Boolean")

        loadKerML("feature r1: ScalarValues::Boolean = a;", Runlevel.VARIABLES)
        assertEquals(solver.variable("r1").ast!!.bdd.height(), 1)

        loadKerML("feature b: ScalarValues::Boolean = true;", Runlevel.VARIABLES)
        loadKerML("feature r2: ScalarValues::Boolean = a and b;", Runlevel.VARIABLES)
        assertEquals(1, solver.variable("r2").ast!!.bdd.height())

        letVar("a", builder.Bool.True)
        solver.propagate()

        val r2 = solver.variable("r2")
        assertEquals(builder.Bool.True, r2.vectorQuantity.values[0].asBdd())

        loadKerML("feature c: ScalarValues::Boolean = not(b);")
        loadKerML("feature r3: ScalarValues::Boolean = (not(r1) and r2) or (r1 and not(r2));")

        loadKerML("feature r4: ScalarValues::Boolean = not(r3 and c);")
    }

    @Test
    @Ignore
    fun setVariableAADD() = testSession("ScalarValues") {
            initialize(Runlevel.ALL)
            defScalarVar("a", "X", "", "ScalarValues::Boolean")
            defScalarVar("b", "true", "", "ScalarValues::Boolean")

            defScalarVar("x", "0.0..0.1", type = "ScalarValues::Real")
            defScalarVar("y", "0.7..0.9", type = "ScalarValues::Real")

            loadKerML("feature r1: ScalarValues::Real = ITE(a, x, y);")

            letVar("a", builder.Bool.True)
            val r1 = solver.variable("r1").vectorQuantity
            assertEquals(r1, solver.variable("x").vectorQuantity)

            letVar("a", builder.Bool.False)
            solver.propagate()
            val r2 = solver.variable("r1").vectorQuantity.value
            val y = solver.variable("y").vectorQuantity.value


            assertBounds(0.7..0.9, solver.variable("r1"))
            assertBounds(0.7..0.9, solver.variable("y"))

            letVar("a", builder.boolean("a"))
            solver.propagate()
            val r3 = solver.variable("r1").vectorQuantity.value
            assertBounds(0.0..0.9, r3.asAadd())
    }

    @Test
    fun setVariableWithComplexBDD() = testSession("ScalarValues") {
            loadKerML("feature a: ScalarValues::Boolean;", Runlevel.NONE)
            loadKerML("feature b: ScalarValues::Boolean;", Runlevel.NONE)
            loadKerML("feature c: ScalarValues::Boolean;", Runlevel.NONE)
            loadKerML("feature y: ScalarValues::Boolean = (a and c) or (not(b) and not(a));", Runlevel.VARIABLES)
            assertNoIssues()
            val a = solver.getVariable("a")
            val y = solver.getVariable("y")
            assertEquals(XBool.All, a?.boolSpecs[0])
            assertEquals(2, y?.vectorQuantity?.value?.height())
    }

    @Test
    fun setVariableWithComplexBDDdown() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            feature b: ScalarValues::Boolean;
            feature c: ScalarValues::Boolean;
            feature y: ScalarValues::Boolean = (a and c) or (not(b) and not(a));
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val a = solver.variable("a")
        assertEquals(XBool.All, a.boolSpecs[0])
        assertEquals(2, solver.variable("y").vectorQuantity.value.height())
    }

    @Test
    fun setBoolValue() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean(true);
            feature b: ScalarValues::Boolean(false);
            feature d: ScalarValues::Boolean;
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(null, solver.variable("a").ast)
        assertEquals(null, solver.variable("b").ast)
        assertEquals(null, solver.variable("d").ast)
        assertEquals(XBool.All, solver.variable("d").boolSpecs[0])
        assertEquals(XBool.True, solver.variable("a").boolSpecs[0])
        assertEquals(XBool.False, solver.variable("b").boolSpecs[0])
    }

    @Test
    fun leafIntersection() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Boolean;
            feature b: ScalarValues::Boolean(false);
            feature c: ScalarValues::Boolean(true);
            feature y: ScalarValues::Boolean = (a and c) or (not(b) and not(a));
            feature z: ScalarValues::Boolean(true).""")
        assertNoIssues()
        solver.propagate()
        val y = solver.variable("y")
        val a = solver.variable("a")

        val tst = y.vectorQuantity.bdd().intersect(a.vectorQuantity.bdd())
        tst.evaluate()
        assertNoIssues()
    }
}
