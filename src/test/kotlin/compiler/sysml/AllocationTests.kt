package compiler.sysml

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.sysml.AllocationUsage
import com.github.tukcps.sysmd.model.sysml.ConnectionDefinition
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import util.assertNoIssues
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
        assertNoIssues()
        val alloc = global.getOwnedElementOfType<AllocationUsage>()
        assertTrue(alloc != null)
        assertEquals(3, alloc.target.size)
        val a = global.resolve<Element>("a")
        val b = global.resolve<Element>("b")
        val c = global.resolve<Element>("c")
        assertTrue(alloc.target.map { (it as Feature).referencedFeature }.containsAll(listOf(a, b, c)))
    }

    @Test
    fun testSyntax2() = testSession("Parts", "Allocations") {
        loadSysMLv2("""
            part a; 
            part b;
            allocate a to b; 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val c = global.getOwnedElementOfType<AllocationUsage>()
        assertTrue(c != null)
        assertEquals(1, c.from.size)
        assertEquals(1, c.from.size)
    }

    @Test
    fun testSyntax3() = testSession("Parts", "Allocations") {
        loadSysMLv2(
            """
            part a; 
            part b; 
            allocation c allocate a to b; 
        """)
        assertNoIssues()
        val c = global.resolve<AllocationUsage>("c")
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val c = global.resolve<AllocationUsage>("c")
        assertNotNull(c)
        assertEquals(3, c.to.size)
    }

    @Test
    fun testAllocationDefinition() = testSession("Parts", "Allocations") {
        loadSysMLv2("""
            part a; 
            part b; 
            allocation def C; 
            allocation c : C allocate a to b;  
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val c = global.resolve<ConnectionDefinition>("C")
        assertNotNull(c)
        val ci = global.resolve<AllocationUsage>("c")
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val c = global.resolve<ConnectionDefinition>("C")
        assertNotNull(c)
        val ci = global.resolve<ConnectionUsage>("c")
        assertNotNull(ci)
    }
}