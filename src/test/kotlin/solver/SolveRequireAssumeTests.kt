package solver

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SolveRequireAssumeTests {

    @Test
    fun testRequirement() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            part p {
                attribute a: ScalarValues::Real = 1.0; 
            }
            
            requirement test {
                subject f references p;
                assume constraint ass { f::a == 2.0 }
            }
        """, Runlevel.ALL)
        assertNoIssues()
        val ass = solver.getVariable("test::ass")
        assertEquals(builder.NaB, ass!!.vectorQuantity.value)
    }

    @Test
    fun testRequirement3() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            part p {
                attribute a: ScalarValues::Real = 2.0; 
            }
            
            requirement test {
                subject f references p;
                assert constraint ass { f::a == 2.0 }
            }
        """, Runlevel.ALL)
        assertNoIssues()
        val ass = solver.getVariable("test::ass")
        assertEquals(builder.True, ass!!.vectorQuantity.value)
    }

    @Test
    fun testRequirement4() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            part p {
                attribute a: ScalarValues::Real = 2.0; 
            }
            
            requirement test {
                subject f references p;
                require constraint r { f::a == 2.0} 
            }
        """)
        solver.propagate()
        assertNoIssues()
        val r = solver.getVariable("test::r")
        assertEquals(builder.True, r!!.bool())
        assertEquals(Variable.BaseType.Bool, r.baseType)
    }

    @Test
    fun testRequirement5() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            part p {
                attribute a: ScalarValues::Real = 2.0; 
            }
            
            requirement test {
                subject f references p;
                require constraint r { f::a == 2.0 }
            }
        """, Runlevel.ALL)
        assertNoIssues()
        val r = global.resolve("test::r")?.member<Feature>()
        assertEquals(builder.True, r!!.variable!!.bool())
        assertTrue(r.specializes(global.resolve("ScalarValues::Boolean")?.member()))
        // assertTrue(r.specializes(global.resolve("Constraints::ConstraintUsage")?.member()))
    }
}