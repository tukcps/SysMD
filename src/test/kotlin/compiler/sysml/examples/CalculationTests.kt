package compiler.sysml.examples

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test

class CalculationTests {
    
    /**
     * This test checks the definition of calculations in SysML v2.
     * It verifies that calculations can be defined using the `calc def` keyword and that no exceptions are raised.
     * Refer to Section: 7.18 Calculations
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testCalculationDefinition() = testSession("Calculations") {
        loadSysMLv2("""
            calc def CalcDef1;
        """)
        assertNoIssues()
    }

    /**
     * This test checks the definition of calculations in SysML v2.
     * It verifies that calculations can be defined using the `calc def` keyword and that no exceptions are raised.
     * Refer to Section: 7.19 Calculations
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testCalculationDefinitionWithExpression() = testSession("Calculations") {
        loadSysMLv2("""
            calc def Velocity {
                in v_i : ScalarValues::Real;
                in a : ScalarValues::Real;
                in dt : ScalarValues::Real;
                return v_f : ScalarValues::Real;
            }
        """)
        assertNoIssues()
    }


    /**
     * Incomplete definition; must be completed by either definition in superclass or
     * re-definition later.
     */
    @Test
    fun testCalculationDefinitionWithExpression2() = testSession("Calculations") {
        loadSysMLv2("""
            calc def Velocity {
                in initialState; 
                return nextState; 
            }
        """)
        assertNoIssues()
    }


    /**
     * This test checks the usage of calculations in SysML v2.
     * It ensures that calculations can be instantiated and used after being defined, with no exceptions raised during their usage.
     * Refer to Section: 7.18 Calculations
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testCalculationUsage() = testSession("Calculations") {
        loadSysMLv2("""
            calc def CalcDef1{
                in a: ScalarValues::Real; 
                in b: ScalarValues::Real;
                return : ScalarValues::Real = a+b; // Return value has no name. 
            }
        """)
        assertNoIssues()
    }
}
