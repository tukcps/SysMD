package compiler.kerml

import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test

class StepTests {
    @Test
    fun testStep() = testSession("Performances") {
        loadKerML("""
            behavior b {
                step s; 
            } 
        """)
        assertNoIssues()
    }

}