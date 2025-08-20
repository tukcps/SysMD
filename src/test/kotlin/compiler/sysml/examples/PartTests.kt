package sysmlv2specificationtests

import util.mockup.loadSysMLv2
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.sysml.PartDefinition
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.sysml.PortDefinition
import com.github.tukcps.sysmd.model.sysml.PortUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PartTests {

    /**
     * This test checks the definition of parts in SysML v2.
     * It verifies that parts can be defined using the `part def` keyword and that no exceptions are raised.
     * Refer to Section: 7.11 - Parts
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testPartDefinition() = testSession("Parts") {
        loadSysMLv2("""
        part def PartDef1;
        part def PartDef2 {
            /* members */
        }
        """)

        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val partDef1 = global.resolve<Element>("PartDef1")
        assertNotNull(partDef1)

        val partDef2 = global.resolve<PartDefinition>("PartDef2")
        assertNotNull(partDef2)
    }

    /**
     * This test checks the usage of parts in SysML v2.
     * It ensures that parts can be instantiated from a definition and used with no exceptions raised.
     * Refer to Section: 7.11 - Parts
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testPartUsage() = testSession("Parts") {
        loadSysMLv2("""
        part def PartDef1;
        part part1 : PartDef1;
        part part2 : PartDef1 {
            /* members */
        }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val partDef1 = global.resolve<Element>("PartDef1")
        assertNotNull(partDef1)

        val part1 = global.resolve<PartUsage>("part1")
        assertNotNull(part1)

        val part2 = global.resolve<Element>("part2")
        assertNotNull(part2)
    }

    /**
     * This test checks the definition and usage of parts with ports in SysML v2.
     * It ensures that parts can define ports, and nested ports can be used within a part.
     * Refer to Section: 7.11 - Part Definitions and Usages and Section 7.12.2 - Port Definitions and Usages - in Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testPartWithPorts() = testSession("Parts", "Ports") {
        loadSysMLv2("""
        port def PortDef0;
        port def PortDef1;
        port def PortDef2;
        port def PortDef3;
        port def PortDef4;
        port def PortDef5;
        
        part part1 {
            port p0 : PortDef0 {
                port p1 : PortDef1;
                port p2 : PortDef2;
                port p3 : PortDef3;
            }
            port p4 : PortDef4;
            port p5 : PortDef5;
        }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val p0 = global.resolve<Element>("part1::p0")
        assertNotNull(p0)

        val part1 = global.resolve<PartUsage>("part1")
        assertNotNull(part1)

        val p1 = global.resolve<PortUsage>("part1::p0::p1")
        assertNotNull(p1)

        val portDef1 = global.resolve<PortDefinition>("PortDef1")
        assertNotNull(portDef1)
    }
}
