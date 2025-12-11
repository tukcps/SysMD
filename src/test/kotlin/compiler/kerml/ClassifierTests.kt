package compiler.kerml

import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.services.check.checkOwnership
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ClassifierTests {
    @Test
    fun classifierTest() = testSession {
        loadKerML("""
            abstract classifier a; 
            classifier b :> a; 
            classifier c :> a, b; 
        """)
        checkOwnership()
        assertNoIssues()
        val a = global.resolve("a")?.member<Classifier>()
        assertEquals(a?.isAbstract, true)
        val c = global.resolve("c")?.member<Classifier>()
        assertNotNull(c)
        assertTrue(c.specializes(global.resolve("b")?.member<Classifier>()))
        assertTrue(c.specializes(global.resolve("a")?.member<Classifier>()))
    }
}