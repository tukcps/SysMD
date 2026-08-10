package constraintnettests.bddtests

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.defScalarVar
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.letVar
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.XBool
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
        assertEquals(solver.getVariable("r1")!!.ast!!.bdd.height(), 1)

        loadKerML("feature b: ScalarValues::Boolean = true;", Runlevel.VARIABLES)
        loadKerML("feature r2: ScalarValues::Boolean = a and b;", Runlevel.VARIABLES)
        assertEquals(1, solver.getVariable("r2")!!.ast!!.bdd.height())

        letVar("a", builder.True)
        solver.propagate()

        val r2 = solver.getVariable("r2")!!
        assertEquals(builder.True, r2.vectorQuantity.values[0].asBdd())

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

            letVar("a", builder.True)
            val r1 = solver.getVariable("r1")!!.vectorQuantity
            assertEquals(r1, solver.getVariable("x")!!.vectorQuantity)

            letVar("a", builder.False)
            solver.propagate()
            val r2 = solver.getVariable("r1")!!.vectorQuantity.value
            val y = solver.getVariable("y")!!.vectorQuantity.value
            assertEquals(r2.asAadd().min, y.asAadd().min, 0.00001)
            assertEquals(r2.asAadd().max, y.asAadd().max, 0.00001)

            letVar("a", builder.boolean("a"))
            solver.propagate()
            val r3 = solver.getVariable("r1")!!.vectorQuantity.value
            assertEquals(0.0, r3.asAadd().min,  0.00001)
            assertEquals(0.9, r3.asAadd().max, 0.00001)
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
            assertEquals(XBool.X, a?.boolSpecs[0])
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
        val a = solver.getVariable("a")!!
        assertEquals(XBool.X, a.boolSpecs[0])
        assertEquals(2, solver.getVariable("y")!!.vectorQuantity.value.height())
    }

    @Test
    fun setBoolValue() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean(true);
            feature b: ScalarValues::Boolean(false);
            feature d: ScalarValues::Boolean;
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(null, solver.getVariable("a")!!.ast)
        assertEquals(null, solver.getVariable("b")!!.ast)
        assertEquals(null, solver.getVariable("d")!!.ast)
        assertEquals(XBool.X, solver.getVariable("d")!!.boolSpecs[0])
        assertEquals(XBool.True, solver.getVariable("a")!!.boolSpecs[0])
        assertEquals(XBool.False, solver.getVariable("b")!!.boolSpecs[0])
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
        val y = solver.getVariable("y")!!
        val a = solver.getVariable("a")!!

        val tst = y.vectorQuantity.bdd().intersect(a.vectorQuantity.bdd())
        tst.evaluate()
        assertNoIssues()
    }
}
