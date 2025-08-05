package compiler.kerml

import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.services.resolve.resolve
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
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
        val n = global.resolve<Namespace>("n")
        assertNotNull(n)
        val n2 = global.resolve<Namespace>("n::n2")
        assertNotNull(n2)
    }
}