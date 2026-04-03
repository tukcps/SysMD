//package bddOperationsTests

package constraintnettests.bddtests

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
internal class OperationTests /*: DDBuilderIF by Global.context */{

    @Test
    // After setting a decision variable of a BDD to true, the results of all BDD shall consider this.
    fun setVariableTestBDD() = testSession("ScalarValues") {
        // create some BDD that depend on variables "a", "b" , "c"
        // set "a" to True or False
        // check result that only the "right" path is considered.

        defScalarVar("a", value = "X", "", "ScalarValues::Boolean")
        // Do never use builder.BOOL to get a Boolean variable. It is just used as placeholder
        // for a boolean unknown as a RESULT or INPUT!!!

        loadKerML("feature r1: ScalarValues::Boolean = a;")
        initialize()
        // println(resolveName<ValueFeature>("a"))        // it is "if a true, else false"
        assertEquals(global.resolveVar("r1")!!.ast!!.bdd.height(), 1)

        loadKerML("feature b: ScalarValues::Boolean = true;")
        loadKerML("feature r2: ScalarValues::Boolean = a and b;") //
        assertEquals(1, global.resolveVar("r2")!!.ast!!.bdd.height())

        val a = letVar("a", builder.True)
        initialize()
        solver.propagate()

        val r2 = global.resolveVar("r2")!!
        // println("r2 = $r2")
        assertEquals(builder.True, r2.vectorQuantity.values[0].asBdd())

        loadKerML("feature c: ScalarValues::Boolean = not(b);")
        loadKerML("feature r3: ScalarValues::Boolean = (not(r1) and r2) or (r1 and not(r2));")
        // println("r3 = " + resolveName<ValueFeature>("r3"))

        loadKerML("feature r4: ScalarValues::Boolean = not(r3 and c);")
        // println("r4 = " + resolveName<ValueFeature>("r4"))
    }

    /**
     * We can set single variables to True, False, Unknown, and then
     * run 'initialize' and 'propagate' to get results.
     * No need to re-build the complete AST.
     */
    @Test @Ignore
    fun setVariableTestAADD() = testSession("ScalarValues") {
            // Create some BDD that depend on variables "a", "b" , "c"
            // set "a" to True or False
            // Do some operation with AADD x, y
            // check result
            // AADD only follow the correct path, not both paths.
            // Check that operations yield correct results.
            initialize()
            defScalarVar("a", "X", "", "ScalarValues::Boolean")
            defScalarVar("b", "true", "", "ScalarValues::Boolean")
            // derVar("c", NaB) //Not sure if intended to use. Will use later on if correct

            defScalarVar("x", "0.0..0.1", type = "ScalarValues::Real")
            defScalarVar("y", "0.7..0.9", type = "ScalarValues::Real")

            loadKerML("feature r1: ScalarValues::Real = ITE(a, x, y);")

            letVar("a", builder.True)
            // global.resolveName<Expression>("r1")!!.evalUp()
            val r1 = global.resolveVar("r1")!!.vectorQuantity
            assertEquals(r1, global.resolveVar("x")!!.vectorQuantity)

            letVar("a", builder.False)
            initialize()
            solver.propagate()
            val r2 = global.resolveVar("r1")!!.vectorQuantity.value
            val y = global.resolveVar("y")!!.vectorQuantity.value
            assertEquals(r2.asAadd().min, y.asAadd().min, 0.00001)
            assertEquals(r2.asAadd().max, y.asAadd().max, 0.00001)

            letVar("a", builder.boolean("a"))
            initialize()
            solver.propagate()
            val r3 = global.resolveVar("r1")!!.vectorQuantity.value
            assertEquals(0.0, r3.asAadd().min,  0.00001)
            assertEquals(0.9, r3.asAadd().max, 0.00001)
    }

    @Test
    fun setVariableTestWithComplexBDD() = testSession("ScalarValues") {
            // The following is equivalent:
            loadKerML("feature a: ScalarValues::Boolean;")
            loadKerML("feature b: ScalarValues::Boolean;")
            loadKerML("feature c: ScalarValues::Boolean;")
            loadKerML("feature y: ScalarValues::Boolean = (a and c) or (not(b) and not(a));")
            assertNoIssues()
            assertEquals(XBool.X, global.resolveVar("a")!!.boolSpecs[0])
            assertEquals(2, global.resolveVar("y")!!.vectorQuantity.value.height())
            // 1) a auf True setzen
            // 2) impact auf y checken
            // 3) b auf False setzen (oder auch True)
            // 4) impact auf y checken
    }


    @Test
    fun setVariableTestWithComplexBDDdown() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            feature b: ScalarValues::Boolean;
            feature c: ScalarValues::Boolean;
            feature y: ScalarValues::Boolean = (a and c) or (not(b) and not(a));""")
        assertNoIssues()
        val a = global.resolveVar("a")!!
        val b = global.resolveVar("b")!!
        val c = global.resolveVar("c")!!
        val y = global.resolveVar("y")!!
        assertEquals(XBool.X, a.boolSpecs[0])
        // println(builder.conds.indexes)
        assertEquals(2, global.resolveVar("y")!!.vectorQuantity.value.height())
        // println(resolveName<ValueFeature>("y")!!.quantity.value)
        // 1) y auf True setzen
        // 2) impact auf a, b, c checken
        // 3) y auf False setzen
        // 4) impact auf a, b, c checken
        // (was passiert, wenn es keine Variablenbelegung gibt, die passt?)
    }

    /**
     * Declared Boolean properties with no expression just have a constant value defined by
     * the BoolSpec field.
     * Nevertheless, the variable's values might be changed, so we give them an index.
     */
    @Test
    fun setBoolValueTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean(true);
            feature b: ScalarValues::Boolean(false);
            feature d: ScalarValues::Boolean;
        """)
        assertNoIssues()
        assertEquals(null, global.resolveVar("a")!!.ast)
        assertEquals(null, global.resolveVar( "b")!!.ast)
        assertEquals(null, global.resolveVar( "d")!!.ast)
        assertEquals(XBool.X, global.resolveVar( "d")!!.boolSpecs[0])
        assertEquals(XBool.True, global.resolveVar( "a")!!.boolSpecs[0])
        assertEquals(XBool.False, global.resolveVar( "b")!!.boolSpecs[0])
        /* assertEquals(1, builder.conds.indexes[resolveName<ValueFeature>("a")!!.id])
        assertEquals(2, builder.conds.indexes[resolveName<ValueFeature>("b")!!.id])
        assertEquals(3, builder.conds.indexes[resolveName<ValueFeature>("d")!!.id]) */
    }

    @Test
    fun leafIntersection1() = testSession("ScalarValues") {
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
        val b = global.resolveVar("b")!!
        val c = global.resolveVar("c")!!

        val brk = 1
        val tst = y.vectorQuantity.bdd().intersect(a.vectorQuantity.bdd())
        val tstEval = tst.evaluate()
        val brk2 = 2
        assertNoIssues()
        //println(this.getProperties().forEach { println(it.toString()) })
    }
}
