package sysmlv2tests

import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.sysml.AllocationUsage
import com.github.tukcps.sysmd.model.sysml.ConnectionDefinition
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AllocationTests {

    @Test
    fun testSyntax1() = testSession("Connections", "Allocations") {
        loadSysMD("""
            feature a; 
            feature b;
            feature c; 
            allocate(a, b, c); 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.getOwnedElementOfType<AllocationUsage>()
        assertTrue(c != null)
        assertTrue(c.target.size == 3 && c.target.containsAll(listOf(c.target[0], c.target[1], c.target[2])))
    }

    @Test
    fun testSyntax2() = testSession("Connections", "Allocations") {
        loadSysMD("""
            feature a; 
            feature b;
            allocate a to b; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.getOwnedElementOfType<AllocationUsage>()
        assertTrue(c != null)
        assertEquals(1, c.from.size)
        assertEquals(1, c.from.size)
    }

    @Test
    fun testSyntax3() = testSession("Connections", "Allocations") {
        loadSysMD("""
            feature a; 
            feature b; 
            allocation c allocate a to b; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<AllocationUsage> ("c")
        assertNotNull(c)
        assertEquals(1, c.from.size)
        assertEquals(1, c.to.size)
    }

    @Test
    fun testAllocateThree() = testSession("Connections", "Allocations") {
        loadSysMD("""
            feature a; 
            feature b; 
            feature d; 
            allocation c allocate (a, b, d); 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<AllocationUsage> ("c")
        assertNotNull(c)
        assertEquals(3, c.to.size)
    }

    @Test
    fun testAllocationDefinition() = testSession("Connections", "Allocations", "Parts") {
        loadSysMD("""
            part a; 
            part b; 
            allocation def C; 
            allocation c : C allocate a to b;  
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<ConnectionDefinition> ("C")
        assertNotNull(c)
        val ci = global.resolve<AllocationUsage> ("c")
        assertNotNull(ci)
    }

    @Test
    fun testAllocationDefinition2() = testSession("Connections", "Allocations") {
        loadSysMD("""
            feature a; 
            feature b;
            allocation def C1; 
            allocation def C :> C1; 
            allocation c : C allocate a to b;  
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<ConnectionDefinition> ("C")
        assertNotNull(c)
        val ci = global.resolve<ConnectionUsage> ("c")
        assertNotNull(ci)
    }
}