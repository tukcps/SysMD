package compiler.sysml.examples

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class InterfaceTests {

    /**
     * This test checks the definition of a simple interface in SysML v2.
     * It verifies that interfaces can be defined using the `interface def` keyword and that no exceptions are raised.
     * Refer to Section: 7.14 Interfaces
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testSimpleInterfaceDefinition() = testSession("Interfaces", "Ports", "Connections") {
        loadSysMLv2("""
            interface def C1; 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * This test checks the definition of an interface with ports in SysML v2.
     * It verifies that an interface can define ports and link them through `end` elements.
     * Refer to Section: 7.14 Interfaces
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */

    @Test
    fun testInterfaceDefinition() = testSession("Interfaces", "Ports") {
        loadSysMLv2("""
            port def Port1;
            port def Port2;
            interface def InterfaceDef1 {
                end port1 : Port1;
                end port2 : Port2;
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

}
