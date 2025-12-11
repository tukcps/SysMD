package constraintnettests

import com.github.tukcps.sysmd.cspsolver.Solver
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class VariableTests {

    @Test
    fun variableTests() = testSession{
        val solver = Solver(this)
        // val variable = solver.createVariable("test", "test")
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
        """)
        assertNoIssues()
        assertEquals(5, solver.getVariables().size)
        assertEquals("n1::f3::f1", solver.resolveVar(null, "n1::f3::f1")?.name)
    }
}