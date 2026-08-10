package solver

import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.XBool
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class VariableTests {

    @Test
    fun invariantCreatesVariableTest() = testSession("ScalarValues") {
        loadKerML("""
            inv i { true }
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val i = solver.getVariable("i")
        assertEquals("i", i?.path)
        assertEquals(XBool.True, i?.boolSpecs?.first())
        // assertEquals("true", i?.expression?.trim())
    }

    @Test
    fun invariantCreatesVariableTestNegated() = testSession("ScalarValues") {
        loadKerML("""
            inv false i { true }
        """, Runlevel.MODEL)
        assertNoIssues()
        solver.initVariables()
        val i = solver.getVariable("i")
        assertEquals("i", i?.path)
        assertEquals(XBool.False, i?.boolSpecs?.first())
        // assertEquals("true", i?.expression?.trim())
    }

    @Test
    fun vectorRangeTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = (1..2, 3..4, 5..6); }
        """)
        assertNoIssues()
        solver.initVariables()
    }

    @Test
    fun variableTests2() = testSession("ScalarValues") {
        loadKerML("""
            namespace n1 {
                type t1 :> Base::Anything {
                    feature f1: ScalarValues::Real; 
                    feature f2: ScalarValues::Integer;
                }
                feature f3 : t1; 
            }                
        """, Runlevel.VARIABLES)
        assertNoIssues()
        assertEquals(5, solver.getVariables().size)
        assertEquals("n1::f3::f1", solver.resolveVar(null, "n1::f3::f1")?.path)
    }

    @Test
    fun ifElseExpressionTest1() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Boolean;
            feature y: ScalarValues::Boolean = if x ? true else false;
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val y = solver.getVariable("y")!!
        assertEquals(XBool.X, y.bool())
    }

    @Test
    fun ifElseExpressionTest2() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Boolean = false;
            feature y: ScalarValues::Boolean = if x ? true else false;
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val y = solver.getVariable("y")!!
        assertEquals(XBool.False, y.bool())
    }

    @Test
    fun ifElseExpressionTest3() = testSession("ScalarValues") {
        loadKerML("""
               feature x: ScalarValues::Boolean;
               feature y: ScalarValues::Real = if x ? 1.0 else 2.0;
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val y = solver.getVariable("y")!!
        assertEquals(1.0, y.min(), 0.00001)
        assertEquals(2.0, y.max(), 0.00001)
    }

    @Test
    fun ifElseExpressionTest4() = testSession("ScalarValues") {
        loadKerML("""
           feature x: ScalarValues::Boolean;
           feature y: ScalarValues::Integer = if x ? 1 else 2;
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val y = solver.getVariable("y")!!
        assertEquals(1.0, y.min(), 0.00001)
        assertEquals(2.0, y.max(), 0.00001)
    }

    @Test
    fun MultiplicityVariableTestKerML() = testSession("ScalarValues") {
        loadKerML("""
            feature f: ScalarValues::Real [1..30];
            inv { f::cardinality < 10 }
        """, Runlevel.ALL)
        assertNoIssues()
        val f = solver.getVariable("f")!!
        val inv = global.getOwnedElementOfType<Invariant>()
        val multiplicity = solver.getVariable("f::multiplicity")!!
        assertEquals(9L, multiplicity.max())
    }

    @Test
    fun MultiplicityVariableTestSysML() = testSession("ScalarValues") {
        loadSysMLv2("""
            attribute f: ScalarValues::Real [1..30];
            assert { f::cardinality < 10 }
        """, Runlevel.ALL)
        assertNoIssues()
        val f = solver.getVariable("f")!!
        val inv = global.getOwnedElementOfType<Invariant>()
        val multiplicity = solver.getVariable("f::multiplicity")!!
        assertEquals(9L, multiplicity.max())
    }
}