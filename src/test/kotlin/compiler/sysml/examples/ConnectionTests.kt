package compiler.sysml.examples

import com.github.tukcps.sysmd.model.sysml.ConnectionDefinition
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.model.sysml.PartDefinition
import com.github.tukcps.sysmd.model.sysml.PartUsage
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConnectionTests {

    /**
     * This test checks the definition of a normal connection in SysML v2.
     * It verifies that connections can be defined using the `connection def` keyword and that no exceptions are raised.
     * Refer to Section: 7.13 Connections
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testNormalConnectionDefinition() = testSession("Connections") {
        loadSysMLv2("""
            connection def ConnectionDef1;
        """)
        assertNoIssues()

        val connectionDef1 = global.resolve("ConnectionDef1")?.memberElement as ConnectionDefinition
        assertNotNull(connectionDef1)
    }

    /**
     * This test checks the definition of a connection with parts in SysML v2.
     * It verifies that parts can be defined within a connection and linked through the `end` keyword.
     * Refer to Section: 7.13 Connections
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testConnectionDefinition() = testSession("Connections", "Parts", "Items") {
        loadSysMLv2("""
            connection def ConnectionDef1 {
                part def Part1;
                part def Part2;
                end end1 : Part1;
                end end2 : Part2;
            }
        """)
        assertNoIssues()

        val partDef1 = global.resolve("ConnectionDef1::Part1")?.memberElement
        assertTrue(partDef1 is PartDefinition)

        val partDef2 = global.resolve("ConnectionDef1::Part2")?.memberElement
        assertTrue(partDef2 is PartDefinition)

        val connectionDef1 = global.resolve("ConnectionDef1")?.memberElement
        assertTrue(connectionDef1 is ConnectionDefinition)
    }

    /**
     * This test checks the definition of a connection with multiplicities in SysML v2.
     * It verifies that parts can be connected using specific multiplicities for the `end` elements.
     * Refer to Section: 7.13 Connections
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testSeperatedConnectionDefinition() = testSession("Connections", "Parts") {
        loadSysMLv2("""
        connection def ConnectionDef1 {
            part def Part1;
            part def Part2;
            end end1 : Part1[0..1];
            end end2 : Part2[1..*];
        }
        """)
        assertNoIssues()

        val partDef1 = global.resolve("ConnectionDef1::Part1")?.memberElement
        assertTrue(partDef1 is PartDefinition)

        val partDef2 = global.resolve("ConnectionDef1::Part2")?.memberElement
        assertTrue(partDef2 is PartDefinition)

        val connectionDef1 = global.resolve("ConnectionDef1")?.memberElement
        assertTrue(connectionDef1 is ConnectionDefinition)
    }

    /**
     * This test checks the usage of a connection in SysML v2.
     * It verifies that connections can be instantiated and used to connect parts, with no exceptions raised during their usage.
     * Refer to Section: 7.13 Connections
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testConnectionUsage() = testSession("Parts", "Connections") {
        loadSysMLv2("""
            part def Part1;
            part def Part2;
            
            part part1 : Part1;
            part part2 : Part2;
             
            connection def ConnectionDef1 {
                end end1 ::> part1;
                end end2 ::> part2;
            }
        """)
        assertNoIssues()

        val connectionDef1 = global.resolve("ConnectionDef1")
        assertTrue(connectionDef1?.memberElement is ConnectionDefinition)
    }

    /**
     * This test checks the usage of a single connection between two parts in SysML v2.
     * It verifies that connections can be used to link parts with the `connect` keyword.
     * Refer to Section: 7.13 Connections
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testSingleConnectionUsage() = testSession("Connections", "Parts") {
        loadSysMLv2("""
            part def Part1;
            part def Part2;
            part part1 : Part1;
            part part2 : Part2;
            connection def ConnectionDef1;
            connection connection1 : ConnectionDef1 connect part1 to part2;
        """)
        assertNoIssues()

        val connectionDef1 = global.resolve("ConnectionDef1")
        assertTrue(connectionDef1?.memberElement is ConnectionDefinition)

        val connection1 = global.resolve("connection1")
        assertTrue(connection1?.memberElement is ConnectionUsage)
    }

    /**
     * This test checks the usage of a nested connection in SysML v2.
     * It verifies that nested parts can be connected using a connection inside another part definition.
     * Refer to Section: 7.13 Connections
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testNestedConnection() = testSession("Connections", "Parts") {
        loadSysMLv2("""
        part def Part2;
        part def Part3;
        part def Part4;
        part def Part5;
        
        part def Part1 {
        
            part part2 : Part2 {
                part part4 : Part4;
            }
            
             part part3 : Part3 {
                part part5 : Part5;
            }
            connection def ConnectionDef1;
            connection connection1 : ConnectionDef1 connect part2::part4 to part3::part5;
        }
        """)
        assertNoIssues()

        val partDef1 = global.resolve("Part1")
        assertTrue(partDef1?.memberElement is PartDefinition)

        val partDef2 = global.resolve("Part2")
        assertTrue(partDef2?.memberElement is PartDefinition)

        val part2 = global.resolve("Part1::part2")
        assertTrue(part2?.memberElement is PartUsage)

        val part4 = global.resolve("Part1::part2::part4")
        assertTrue(part4?.memberElement is PartUsage)

        val connection1 = global.resolve("Part1::connection1")
        assertTrue(connection1?.memberElement is ConnectionUsage)
    }

    /**
     * This test checks the binding of parts in SysML v2.
     * It verifies that a part can be bound to another part using the `bind` keyword, connecting references within a system.
     * Refer to Section: 7.13 Connections
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */

    @Test
    fun testBindingConnection() = testSession("Connections", "Parts") {
        loadSysMLv2("""
            part def Part1;
            part def Part2;
            part def Part3;
            part def Part4;
            part part1 : Part1 {
            
                part part2 : Part2 {
                    part part4R : Part4;
                }
                
                part part3 : Part3 {
                    part part4 : Part4;
                }
                
                bind part2::part4R = part3::part4;
            }
        """)
        assertNoIssues()

        val partDef1 = global.resolve("Part1")?.memberElement
        assertNotNull(partDef1)

        val partDef2 = global.resolve("Part2")?.memberElement
        assertNotNull(partDef2)

        val part2 = global.resolve("part1::part2")?.memberElement
        assertNotNull(part2)

        val part4 = global.resolve("Part4")?.memberElement
        assertNotNull(part4)
    }

    /**
     * This test checks the definition of a flow connection between actions in SysML v2.
     * It verifies that items can flow between actions through connections.
     * Refer to Section: 7.13 Connections
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testFlow() = testSession("Items", "States", "Connections", "Actions") {
        loadSysMLv2("""
            action def Action1;
            action def Action2;
            item def Item1;
            item def Item2;
            
            action action1 : Action1 {
                out item1:Item1;
            }
            action action2:Action2 {
                in item1:Item1;
            }
            flow action1::item1 to action2::item1;
        """)
        assertNoIssues()
    }
}
