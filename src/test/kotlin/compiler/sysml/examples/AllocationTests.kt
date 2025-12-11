package compiler.sysml.examples

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AllocationTests {

    /**
     *     This test checks the definition of allocations.
     *     Refer to Section: 7.15 - Allocations
     *     Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testAllocationDefinition() = testSession("Connections", "Allocations") {
        loadSysMLv2("""
        allocation def AllocationDef1;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val allocationDef1 = global.resolve("AllocationDef1")
        assertNotNull(allocationDef1)
    }

    /**
     * This test checks the usage of allocations.
     * It verifies that an allocation can be instantiated from an allocation definition.
     * Refer to Section: 7.15 - Allocations
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testAllocationUsage() = testSession("Parts", "Allocations") {
        loadSysMLv2("""
            allocation def AllocationDef1;
            
            part def Part1;
            part def Part2;
            part part1 : Part1;
            part part2 : Part2;
            
            allocation allocation1 : AllocationDef1 allocate part1 to part2 {
                part def Part3;
                part part3 :Part3;
            }
        """)
        assertNoIssues()

        val allocationDef1 = global.resolve("AllocationDef1")
        assertNotNull(allocationDef1)

        val allocation1 = global.resolve("allocation1")
        assertNotNull(allocation1)
    }

    /**
     *     This test checks allocation between parts using the allocated compartment.
     *     It verifies that parts can be allocated to other parts using the allocate keyword.
     *     Refer to Section: 7.15 - Allocations
     *     Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testAllocatedCompartment() = testSession("Allocations", "Parts") {
        loadSysMLv2("""
            part def Part1;
            part def Part2;
            part def Part3;
            part part1 : Part1;
            part part2 : Part2;
            part part3 {
                allocate part1 to part3;
                allocate part3 to part2;
            }
        """)
        assertNoIssues()
    }

    /**
     * This test checks a simple allocation between two parts.
     * It verifies that part1 can be allocated to part2.
     * Refer to Section: 7.15 - Allocations
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testAllocation() = testSession("Allocations", "Parts") {
        loadSysMLv2("""
            part def Part1;
            part def Part2;
            part part1 : Part1;
            part part2 : Part2;
            allocate part1 to part2;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * This test checks nested allocations (suballocations).
     * It verifies that specific actions of parts can be allocated to each other.
     * Refer to Section: 7.15 - Allocations
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testAllocationWithSubAllocation() = testSession( "Allocations", "Parts", "Actions") {
        loadSysMLv2("""
            part def Part1;
            part def Part2;
            
            action def Action1;
            action def Action2;
            
            action action1 : Action1;
            action action2 : Action2;
            
            part part1 : Part1 {
                perform action1;  // references action1, unnamed performance. 
            }
            part part2 : Part2 {
                perform action2;
            }
            allocate part1 to part2 {
                allocate part1.action1 to part2.action2;
            }
        """)
        val action1 = global.resolve("part1")
        assertNotNull(action1)
        assertNoIssues()
    }
}