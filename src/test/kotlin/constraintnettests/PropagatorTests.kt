package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.functions.numInternalNodes
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class PropagatorTests {

    //Test to find remaining Null pointers... make sure no exception is thrown.
    @Test
    fun propagationByPropagatorsTest() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Boolean;
            feature b: ScalarValues::Boolean;
            feature c: ScalarValues::Boolean;
            feature y: ScalarValues::Boolean = (a and c) or (not(b) and not(a));
            feature z: ScalarValues::Boolean(true) ;
        """, Runlevel.ALL)
        assertNoIssues()
    }

    //Test if all don't cares are found
    @Test
    fun propagationByPropagatorsDontCareTest()  = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            inv false b;
            inv c; 
            inv y = (a and c) or (not(b) and not(a)); 
            inv z; 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(1, solver.getVariable("a")!!.vectorQuantity.value.numInternalNodes())
    }

    @Test
    fun propagationByPropagatorsUnitClausesTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            feature b: ScalarValues::Boolean;
            feature c: ScalarValues::Boolean;
            feature d: ScalarValues::Boolean;
            feature f: ScalarValues::Boolean;
            feature g: ScalarValues::Boolean(true) = (a or b or c or d) and f; 
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(true, solver.getVariable("f")!!.vectorQuantity.value is BDD.Leaf)
    }
}