package sysmlv2specificationtests

import com.github.tukcps.sysmd.model.sysml.AllocationDefinition
import com.github.tukcps.sysmd.model.sysml.AllocationUsage
import com.github.tukcps.sysmd.services.resolve.resolve
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
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

        val allocationDef1 = global.resolve<AllocationDefinition>("AllocationDef1")
        assertNotNull(allocationDef1)
    }

    /**
     * This test checks the usage of allocations.
     * It verifies that an allocation can be instantiated from an allocation definition.
     * Refer to Section: 7.15 - Allocations
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testAllocationUsage() = testSession("Connections", "Parts", "Allocations") {
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
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

        val allocationDef1 = global.resolve<AllocationDefinition>("AllocationDef1")
        assertNotNull(allocationDef1)

        val allocation1 = global.resolve<AllocationUsage>("allocation1")
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
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
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
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * This test checks nested allocations (sub-allocations).
     * It verifies that specific actions of parts can be allocated to each other.
     * Refer to Section: 7.15 - Allocations
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testAllocationWithSubAllocation() = testSession("Allocations", "Parts", "Actions") {
        loadSysMLv2("""
            part def Part1;
            part def Part2;
            
            action def Action1;
            action def Action2;
            
            action action1 : Action1;
            action action2 : Action2;
            
            part part1 : Part1 {
                perform action1;        // TODO: We have a cyclic recursion here in resolving action1. 
            }
            part part2 : Part2 {
                perform action2;
            }
            allocate part1 to part2 {
                allocate part1.action1 to part2.action2;
            }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}