package sysmlv2specificationtests

import util.mockup.loadSysMLv2
import com.github.tukcps.sysmd.model.sysml.PortDefinition
import com.github.tukcps.sysmd.model.sysml.PortUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import util.assertNoIssues
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

        val portDef1 = global.resolve<PortDefinition>("PortDef1")
        assertNotNull(portDef1)

        val portDef2 = global.resolve<PortDefinition>("PortDef2")
        assertNotNull(portDef2)
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

        val port1 = global.resolve<PortUsage>("port1")
        assertNotNull(port1)

        val port2 = global.resolve<PortUsage>("port2")
        assertNotNull(port2)

        val portDef1 = global.resolve<PortDefinition>("PortDef1")
        assertNotNull(portDef1)
    }
}
