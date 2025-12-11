package compiler.kerml

import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.OwningMembership
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
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
        """)
        assertNoIssues()
        val n = global.resolve("n")?.memberElement
        assertNotNull(n)
        val n2 = global.resolve("n::n2")?.memberElement
        assertNotNull(n2)
    }

    @Test
    fun namespaceVisibilityKindTest() = testSession {
        loadKerML("""
            private namespace n; 
        """)
        assertNoIssues()
        val n = global.getOwned<Namespace>("n")
        assertNotNull(n)
        assertEquals(Import.VisibilityKind.Private, (n.owningRelationship as OwningMembership).visibility)
    }
}