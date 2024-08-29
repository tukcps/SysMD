package sysmlv2tests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RequirementTests {

    @Test
    fun testRequirement() = testSession("Parts", "Requirements", "Constraints") {
        loadSysMD("""
            part p {
                attribute a: ScalarValues::Real = 1.0; 
            }
            
            requirement test {
                subject f references p;
                assume ass { f::a == 2.0 }
            }
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val ass = global.resolveVar("test::ass")
        assertEquals(builder.NaB, ass!!.vectorQuantity.value)
    }

    @Test
    fun testRequirement2() = testSession("Parts", "Requirements") {
        loadSysMD("""
            part p {
                attribute a: ScalarValues::Real = 2.0; 
            }
            
            requirement test {
                subject f references p;
                assert ass { f::a == 2.0 }
            }
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val ass = global.resolveVar("test::ass")
        assertEquals(builder.True, ass!!.vectorQuantity.value)
    }

    @Test
    fun testRequirement3() = testSession("Parts", "Requirements", "Constraints") {
        loadSysMD("""
            part p {
                attribute a: ScalarValues::Real = 2.0; 
            }
            
            requirement test {
                subject f references p;
                require r f::a == 2.0;  
            }
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val r = global.resolve<Feature>("test::r")
        assertEquals(builder.True, r!!.variable!!.vectorQuantity.value)
        val const = global.resolve<Type>("Requirements::RequirementUsage")
        assertTrue( r.specializes(const) )
        assertTrue( r.specializes(global.resolve<Type>("ScalarValues::Boolean")) )
    }

    @Test
    fun testRequirement4() = testSession("Parts", "Requirements") {
        loadSysMD(
            """
            part p {
                attribute a: ScalarValues::Real = 2.0; 
            }
            
            requirement test {
                subject f references p;
                require r { f::a == 2.0 }
            }
        """.trimIndent()
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val r = global.resolve<Feature>("test::r")
        assertEquals(builder.True, r!!.variable!!.vectorQuantity.value)
        assertTrue(r.specializes(global.resolve<Type>("ScalarValues::Boolean")) )
        assertTrue(r.specializes(global.resolve<Type>("Requirements::RequirementUsage")) )
    }

    @Test
    fun testRequirement4Bool() = testSession("Parts", "Requirements", "Constraints") {
        loadSysMD(
            """
            part p {
                attribute a: ScalarValues::Boolean = false; 
            }
            
            requirement test {
                subject f references p;
                require r { f::a == false }
            }
        """.trimIndent()
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val ass = global.resolveVar("test::r")
        assertEquals(builder.True, ass!!.vectorQuantity.value)
    }

    @Test
    fun testRequirementDefinition() = testSession("Parts", "Requirements") {
        loadSysMD("""
            requirement def rdef {
                attribute a: ScalarValues::Real; 
            }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

    }
}