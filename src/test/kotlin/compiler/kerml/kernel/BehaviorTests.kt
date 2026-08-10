package compiler.kerml.kernel

import com.github.tukcps.sysmd.model.kerml.Behavior
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BehaviorTests {
    @Test
    fun testBehavior() = testSession {
        loadKerML("""
            feature f1; 
            feature f2;
            feature f3;
            feature f4;
            behavior b {
                in feature f1; 
                in feature f2;
                out feature f3; 
                out feature f4;
            }
        """)
        assertNoIssues()
        val b = global.resolve("b")?.member<Behavior>()
        assertNotNull(b)
        assertTrue(b.specialization.isNotEmpty())
        assertEquals(4, b.member.size)
    }
}