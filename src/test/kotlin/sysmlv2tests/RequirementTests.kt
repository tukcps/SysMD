package sysmlv2tests

import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RequirementTests {

    @Test
    fun testRequirement() = testSession("Parts", "Requirements") {
        loadSysMLv2("""
            part p {
                attribute a: ScalarValues::Real = 1.0; 
            }
            
            requirement test {
                subject f references p;
                assume constraint ass { f::a == 2.0 }
            }
        """)
        solver.propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val ass = global.resolveVar("test::ass")
        assertEquals(builder.NaB, ass!!.vectorQuantity.value)
    }

    @Test
    fun testRequirement2() = testSession("Parts", "Requirements") {
        loadSysMLv2("""
            part p {
                attribute a: ScalarValues::Real = 2.0; 
            }
            
            requirement test {
                subject f references p;
                assert constraint ass { f::a == 2.0 }
            }
        """)
        solver.propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val ass = global.resolveVar("test::ass")
        assertEquals(builder.True, ass!!.vectorQuantity.value)
    }

    @Test
    fun testRequirement3() = testSession("Parts", "Requirements") {
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val r = global.resolveVar("test::r")
        assertEquals(builder.True, r!!.bool())
        assertEquals( BaseType.Bool,r.baseType )
    }

    @Test
    fun testRequirement4() = testSession("Parts", "Requirements") {
        loadSysMLv2("""
            part p {
                attribute a: ScalarValues::Real = 2.0; 
            }
            
            requirement test {
                subject f references p;
                require constraint r { f::a == 2.0 }
            }
        """)
        solver.propagate()
        assertNoIssues()
        val r = global.resolve("test::r")?.member<Feature>()
        assertEquals(builder.True, r!!.variable!!.bool())
        assertTrue(r.specializes(global.resolve("ScalarValues::Boolean")?.member()) )
        assertTrue(r.specializes(global.resolve("Constraints::ConstraintUsage")?.member()) )
    }

    @Test
    fun testRequirement4Bool() = testSession("Parts", "Requirements") {
        loadSysMLv2("""
            part p {
                attribute a: ScalarValues::Boolean = false; 
            }
            
            requirement test {
                subject f references p;
                assume constraint r { f::a == false }
            }
        """)
        solver.propagate()
        assertNoIssues()
        val test = global.resolve("test")?.member<Feature>()
        assertNotNull(test)
        val testR = global.resolve("test::r")?.member<Feature>()
        assertNotNull(testR)
        val testRVar = global.resolveVar("test::r")
        assertEquals(builder.True, testRVar!!.bool())
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