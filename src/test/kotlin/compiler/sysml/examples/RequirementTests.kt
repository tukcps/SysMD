package compiler.sysml.examples

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test

class RequirementTests {

    /**
     * This test checks the definition of a requirement in SysML v2.
     * It verifies that requirements can be defined using the `requirement def` keyword and that no exceptions are raised.
     * Refer to Section: 7.20 Requirements
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testRequirementDefinition() = testSession("Requirements") {
        loadSysMLv2("""
        requirement def RequirementDef1 {
            /* members */
        }
        """)
        assertNoIssues()
    }

    /**
     * This test checks the definition of a requirement in SysML v2.
     * It verifies that requirements can be defined using the `requirement def` keyword and that no exceptions are raised.
     * Refer to Section: 7.20 Requirements
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testRequirementDefinitionWithAssumption() = testSession("Requirements") {
        loadSysMLv2("""
        requirement def RequirementDef1 {
            attribute a : ScalarValues::Real;
            attribute b : ScalarValues::Real;
            assume constraint { a > 0.0 }
            require constraint { a > b }
        }
        """)
        assertNoIssues()
    }

    /**
     * This test checks the definition of a requirement with a subject in SysML v2.
     * It verifies that requirements can define a subject, linking them to specific parts.
     * Refer to Section: 7.20 Requirements
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testRequirementWithSubjectDefinition() = testSession("Requirements", "Parts") {
        loadSysMLv2("""
        part def Subject1;
        requirement def <R1> RequirementDef1 {
            subject s1 : Subject1;
        }
        """)
        assertNoIssues()
    }

    /**
     * This test checks the `satisfy` relationship in SysML v2.
     * It ensures that parts can satisfy specific requirements without errors.
     * Refer to Section: 7.20 Requirements
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testSatisfy() = testSession("Requirements", "Parts") {
        loadSysMLv2("""
        part def Part1;
        requirement def Requirement1;
        requirement requirement1 : Requirement1;
        part part1 : Part1 {
            satisfy requirement1;
        }
        """)
        assertNoIssues()
    }
}
