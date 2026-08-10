package compiler.kerml.root

import com.github.tukcps.sysmd.model.kerml.Dependency
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.check.checkLibraryElementIds
import com.github.tukcps.sysmd.services.check.checkOwnership
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DependencyTests {

    @Test
    fun dependenciesTestWithName() = testSession {
        loadKerML("""
                comment a /* a */ 
                comment b /* b */ 
                dependency d from a to b;
            """, runlevel = Runlevel.ALL)
        assertNoIssues()
        val d = global.resolve("d")?.memberElement as Dependency?
        assertNotNull(d)
        assertEquals("d", d.name)
        assertEquals("a", d.client.first().name)
        assertEquals("b", d.supplier.first().name)
    }

    @Test
    fun dependenciesTestWithNoName() = testSession {
        loadKerML("""
            comment a /* a */ 
            comment b /* b */ 
            dependency a to b;
        """)
        assertNoIssues()
        val d = global.getOwnedElementOfType<Dependency>()
        assertNotNull(d)
        assertEquals("a", d.client.first().name)
        assertEquals("b", d.supplier.first().name)
    }

    @Test
    fun dependenciesTestWithNoNameInNamespace() = testSession {
        loadKerML("""
            namespace n {
                comment a /* a */ 
                comment b /* b */ 
                dependency a to b;
            }
        """)
        checkOwnership()
        checkLibraryElementIds()
        assertNoIssues()
        val d = global.resolve("n")?.member<Namespace>()?.getOwnedElementOfType<Dependency>()
        assertNotNull(d)
        assertEquals("a", d.client.first().name)
        assertEquals("b", d.supplier.first().name)
    }
}