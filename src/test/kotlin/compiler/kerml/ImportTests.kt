package compiler.kerml

import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.services.resolve.resolve
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull


class ImportTests {
    @Test
    fun importCreatedTest() = testSession {
        loadKerML("""
            namespace a {
                namespace b;
            }
            private import a::b::*; 
        """)
        assertNoIssues()
        val imp = global.getOwnedElementOfType<Import>()
        assertNotNull(imp)
    }

    @Test
    fun importCreatedTest2() = testSession {
        loadKerML("""
            namespace a {
                public import Base::*; 
            }
        """)
        assertNoIssues()
        val a = global.resolve<Namespace>("a")
        assertNotNull(a)
        val imp = a.getOwnedElementOfType<Import>()
        assertNotNull(imp)
    }
}