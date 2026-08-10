package compiler.sysml

import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class RequirementTests {

    @Test
    fun testRequirement1() = testSession("Parts", "Requirements") {
        loadSysMLv2("requirement r; ")
        assertNoIssues()
        val r = global.resolve("r")
        assertNotNull(r)
    }

    @Test
    fun testRequirement2() = testSession("Parts", "Requirements") {
        loadSysMLv2("""
            part p {
                attribute a: ScalarValues::Real; 
            }
            
            requirement r {
                subject f references p;
            }
        """, Runlevel.MODEL)
        assertNoIssues()
        val r = global.resolve("r")
        assertNotNull(r)
        val rf = global.resolve("r::f")?.member<PartUsage>()
        assertNotNull(rf)
    }

    @Test
    fun testRequirement3() = testSession("Parts", "Requirements") {
        loadSysMLv2("""
            part p {
                attribute a: ScalarValues::Real = 2.0; 
            }
            
            requirement test {
                subject f references p;
                assert constraint ass { f::a == 2.0 }
            }
        """)
        assertNoIssues()
        val ass = global.resolve("test::ass")?.member<Invariant>()
        assertNotNull(ass)
    }

    @Test
    fun testRequirement4Bool() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            part p {
                attribute a: ScalarValues::Boolean = false; 
            }
            
            requirement test {
                subject f references p;
                assume constraint r { f::a == false }
            }
        """, Runlevel.MODEL)
        assertNoIssues()
        val test = global.resolve("test")?.member<Feature>()
        assertNotNull(test)
        val f = global.resolve("test::f::a")
        assertNotNull(f)

        val testR = global.resolve("test::r")?.member<Feature>()
        assertNotNull(testR)
    }

    @Test
    fun testRequirementDefinition() = testSession("Parts", "Requirements") {
        loadSysMLv2("""
            requirement def rDef {
                attribute a: ScalarValues::Real; 
            }
        """)
        assertNoIssues()
    }
}