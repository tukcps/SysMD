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
        assertEquals(global.resolveVar("r1")!!.ast!!.bdd.height(), 1)

        loadKerML("feature b: ScalarValues::Boolean = true;", Runlevel.VARIABLES)
        loadKerML("feature r2: ScalarValues::Boolean = a and b;", Runlevel.VARIABLES)
        assertEquals(1, global.resolveVar("r2")!!.ast!!.bdd.height())

        letVar("a", builder.True)
        solver.propagate()

        val r2 = global.resolveVar("r2")!!
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
            val r1 = global.resolveVar("r1")!!.vectorQuantity
            assertEquals(r1, global.resolveVar("x")!!.vectorQuantity)

            letVar("a", builder.False)
            solver.propagate()
            val r2 = global.resolveVar("r1")!!.vectorQuantity.value
            val y = global.resolveVar("y")!!.vectorQuantity.value
            assertEquals(r2.asAadd().min, y.asAadd().min, 0.00001)
            assertEquals(r2.asAadd().max, y.asAadd().max, 0.00001)

            letVar("a", builder.boolean("a"))
            solver.propagate()
            val r3 = global.resolveVar("r1")!!.vectorQuantity.value
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
            val a = global.resolveVar("a")
            val y = global.resolveVar("y")
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
        val a = global.resolveVar("a")!!
        assertEquals(XBool.X, a.boolSpecs[0])
        assertEquals(2, global.resolveVar("y")!!.vectorQuantity.value.height())
    }

    @Test
    fun setBoolValue() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean(true);
            feature b: ScalarValues::Boolean(false);
            feature d: ScalarValues::Boolean;
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(null, global.resolveVar("a")!!.ast)
        assertEquals(null, global.resolveVar("b")!!.ast)
        assertEquals(null, global.resolveVar("d")!!.ast)
        assertEquals(XBool.X, global.resolveVar("d")!!.boolSpecs[0])
        assertEquals(XBool.True, global.resolveVar("a")!!.boolSpecs[0])
        assertEquals(XBool.False, global.resolveVar("b")!!.boolSpecs[0])
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
        val y = global.resolveVar("y")!!
        val a = global.resolveVar("a")!!

        val tst = y.vectorQuantity.bdd().intersect(a.vectorQuantity.bdd())
        tst.evaluate()
        assertNoIssues()
    }
}
