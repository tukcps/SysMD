package compiler.sysml.examples

import com.github.tukcps.sysmd.model.sysml.PortDefinition
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PortTests {

    /**
     * This test checks the definition of ports in SysML v2.
     * It verifies that ports can be defined using the `port def` keyword and that no exceptions are raised.
     * Refer to Section: 7.12 Ports
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testPortsDefinition() = testSession("Ports") {
        loadSysMLv2("""
            port def PortDef1;
            port def PortDef2 {
            /* members */
            }
        """)
        assertNoIssues()

        val portDef1 = global.resolve("PortDef1")
        assertTrue(portDef1?.memberElement is PortDefinition)

        val portDef2 = global.resolve("PortDef2")
        assertTrue(portDef2?.memberElement is PortDefinition)
    }

    /**
     * This test checks the usage of ports in SysML v2.
     * It ensures that ports can be instantiated and used with no exceptions raised.
     * Refer to Section: 7.12 Ports
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testPortsUsage() = testSession("Ports") {
        loadSysMLv2("""
            port def PortDef1;
            port port1 : PortDef1;
            port port2 : PortDef1 {
                /* members */
            }
        """)
        assertNoIssues()

        val port1 = global.resolve("port1")?.memberElement
        assertNotNull(port1)

        val port2 = global.resolve("port2")
        assertNotNull(port2)

        val portDef1 = global.resolve("PortDef1")
        assertNotNull(portDef1)
    }
}
