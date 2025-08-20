package kermltests

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.check.checkLibraryElementIds
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class RelationshipsTests {

    @Test
    fun dependenciesTestWithName() = testSession {
        loadKerML("""
                comment a /* a */ 
                comment b /* b */ 
                dependency d from a to b;
            """)
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val d = global.resolve<Dependency>("d")
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
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
        val d = global.resolve<Namespace>("n")?.getOwnedElementOfType<Dependency>()
        assertNotNull(d)
        assertEquals("a", d.client.first().name)
        assertEquals("b", d.supplier.first().name)
    }

}
