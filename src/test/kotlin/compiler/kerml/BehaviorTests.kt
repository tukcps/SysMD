package compiler.kerml

import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test

class BehaviorTests {
    @Test
    fun testBehavior() = testSession("Performances") {
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
    }
}