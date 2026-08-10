package compiler.kerml.kernel

import com.github.tukcps.sysmd.model.kerml.Interaction
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class InteractionTests {
    @Test
    fun simpleTest() = testSession("Occurrences") {
        loadKerML("interaction i;")
        assertNoIssues()
        val i = global.resolve("i")?.member<Interaction>()
        assertNotNull(i)
    }
}