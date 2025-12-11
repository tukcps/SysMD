package compiler.sysml.examples

import com.github.tukcps.sysmd.model.sysml.PartDefinition
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.sysml.PortDefinition
import com.github.tukcps.sysmd.model.sysml.PortUsage
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

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

        assertNoIssues()
        val partDef1 = global.resolve("PartDef1")?.member<PartDefinition>()
        assertNotNull(partDef1)

        val partDef2 = global.resolve("PartDef2")?.member<PartDefinition>()
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
        assertNoIssues()

        val partDef1 = global.resolve("PartDef1")?.member<PartDefinition>()
        assertNotNull(partDef1)

        val part1 = global.resolve("part1")?.member<PartUsage>()
        assertNotNull(part1)

        val part2 = global.resolve("part2")?.member<PartUsage>()
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
        assertNoIssues()

        val p0 = global.resolve("part1::p0")?.member<PortUsage>()
        assertNotNull(p0)

        val part1 = global.resolve("part1")?.member<PartUsage>()
        assertNotNull(part1)

        val p1 = global.resolve("part1::p0::p1")?.member<PortUsage>()
        assertNotNull(p1)

        val portDef1 = global.resolve("PortDef1")?.member<PortDefinition>()
        assertNotNull(portDef1)
    }
}
