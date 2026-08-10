package compiler.kerml.root

import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.OwningMembership
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NamespaceTests {
    @Test
    fun namespaceTest1() = testSession {
        loadKerML("""
            namespace n {
                namespace n2;
            } 
        """, Runlevel.MODEL)
        assertNoIssues()
        val n = global.resolve("n")?.memberElement
        assertNotNull(n)
        val n2 = global.resolve("n::n2")?.memberElement
        assertNotNull(n2)
        assertEquals(2, global.member.size) // n, Base
    }

    @Test
    fun namespaceVisibilityKindTest() = testSession {
        loadKerML("""
            namespace n {
                private namespace n; 
            }
        """, Runlevel.MODEL)
        assertNoIssues()
        val n = global.getOwned<Namespace>("n")
        val nn = n?.membership?.firstOrNull()
        assertNotNull(nn)
        assertEquals(Import.VisibilityKind.Private, (nn as OwningMembership).visibility)
    }
}