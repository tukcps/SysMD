package sysmlv2tests

import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.sysml.AllocationUsage
import com.github.tukcps.sysmd.model.sysml.ConnectionDefinition
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AllocationTests {

    @Test
    fun testSyntax1() = testSession("Parts", "Allocations") {
        loadSysMLv2("""
            part a; 
            part b;
            part c; 
            allocate(a, b, c); 
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.getOwnedElementOfType<AllocationUsage>()
        assertTrue(c != null)
        assertEquals(3, c.source.size)
        assertTrue( c.source.containsAll(listOf(c.source[0], c.source[1], c.source[2])))
    }

    @Test
    fun testSyntax2() = testSession("Parts", "Allocations") {
        loadSysMLv2("""
            part a; 
            part b;
            allocate a to b; 
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.getOwnedElementOfType<AllocationUsage>()
        assertTrue(c != null)
        assertEquals(1, c.from.size)
        assertEquals(1, c.from.size)
    }

    @Test
    fun testSyntax3() = testSession("Parts", "Allocations") {
        loadSysMLv2("""
            part a; 
            part b; 
            allocation c allocate a to b; 
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<AllocationUsage> ("c")
        assertNotNull(c)
        assertEquals(1, c.from.size)
        assertEquals(1, c.to.size)
    }

    @Test
    fun testAllocateThree() = testSession("Parts", "Allocations") {
        loadSysMLv2("""
            part a; 
            part b; 
            part d; 
            allocation c allocate (a, b, d); 
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<AllocationUsage> ("c")
        assertNotNull(c)
        assertEquals(3, c.from.size)
    }

    @Test
    fun testAllocationDefinition() = testSession("Parts", "Allocations") {
        loadSysMLv2("""
            part a; 
            part b; 
            allocation def C; 
            allocation c : C allocate a to b;  
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<ConnectionDefinition> ("C")
        assertNotNull(c)
        val ci = global.resolve<AllocationUsage> ("c")
        assertNotNull(ci)
    }

    @Test
    fun testAllocationDefinition2() = testSession("Parts", "Allocations") {
        loadSysMLv2("""
            part a; 
            part b;
            allocation def C1; 
            allocation def C :> C1; 
            allocation c : C allocate a to b;  
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<ConnectionDefinition> ("C")
        assertNotNull(c)
        val ci = global.resolve<ConnectionUsage> ("c")
        assertNotNull(ci)
    }
}