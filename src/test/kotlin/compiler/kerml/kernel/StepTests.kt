package compiler.kerml.kernel

import com.github.tukcps.sysmd.model.kerml.Behavior
import com.github.tukcps.sysmd.model.kerml.Step
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class StepTests {

    @Test
    fun behaviorTest() = testSession("Performances") {
        loadKerML("behavior b; ")
        assertNoIssues()
        val b = global.resolve("b")?.member<Behavior>()
        assertNotNull(b)
    }

    @Test
    fun testStep() = testSession("Performances") {
        loadKerML("""
            behavior b {
                step s; 
            } 
        """)
        assertNoIssues()
        val b = global.resolve("b")?.member<Behavior>()
        assertNotNull(b)
        val s = b.resolve("s")?.member<Step>()
        assertNotNull(s)
    }
}