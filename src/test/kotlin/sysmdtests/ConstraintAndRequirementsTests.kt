package sysmdtests

import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class ConstraintAndRequirementsTests {
    @Test
    fun requirementTest() = testSession("Parts")  {
        loadSysMD("""
            package Filter {
                part testComponent {
                    attribute value1: ScalarValues::Real(0 .. 10) [m] = 4.0 m; 
                    attribute value2: ScalarValues::Real(0 .. 10) [m] = 2.0 m; 
                    assert current { value1 > value2 }; 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val current = global.resolveVar("Filter::testComponent::current")
        assertEquals(XBool.True, current!!.vectorQuantity.value)
    }

    @Test
    fun requirementTest2() = testSession {
        loadSysMD(
            input = """
                inv testr {(3 >= 3) and (3 <= 3) }; 
                inv testr2 { (3 == 3) }; 
            """.trimIndent())
        val testr = global.resolveVar("testr")
        assertEquals(builder.True, testr!!.vectorQuantity.value)
        val testr2 = global.resolveVar("testr2")
        assertEquals(builder.True, testr2!!.vectorQuantity.value)
    }

    @Test
    fun requirementSysMLTest() = testSession {
        loadSysMD(input="""
            
        """.trimIndent()
        )

    }
}