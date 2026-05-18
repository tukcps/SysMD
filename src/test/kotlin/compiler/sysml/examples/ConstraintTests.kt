package compiler.sysml.examples

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test

class ConstraintTests {

    /**
     * This test checks the definition of constraints in SysML v2.
     * It verifies that constraints can be defined using the `constraint def` keyword and that no exceptions are raised.
     * Refer to Section: 7.19 Constraints
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testConstraintDefinition() = testSession("ScalarValues", "Constraints") {
        loadSysMLv2("""
            constraint def Constraint1 {
                in a : ScalarValues::Real;
                in b : ScalarValues::Real;
                a == b
            }
        """)
        assertNoIssues()
    }

    /**
     * This test checks the usage of constraints in SysML v2.
     * It ensures that constraints can be instantiated and used after being defined, with no exceptions raised during their usage.
     * Refer to Section: 7.19 Constraints
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testConstraintUsage() = testSession("ScalarValues", "Constraints") {
        loadSysMLv2("""
            constraint def Constraint1 {
                in a : ScalarValues::Real;
                in b : ScalarValues::Real;
                a == b
            }
            
            constraint constraint1 : Constraint1 {
                // a = b; ---> BUG: is a redefinition 
                ::> a = b;
            }
        """)
        assertNoIssues()
    }
}
