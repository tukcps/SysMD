package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertBounds
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import util.variable
import kotlin.test.Test
import kotlin.test.assertTrue

class ChainPropagationTests {

    /** A constraint at the end of a long chain shall reach the start of the chain within a few iterations. */
    @Test
    fun backwardPropagationAlongChain() = testSession("Ranges") {
        val depth = 30
        val source = buildString {
            append("feature x0: Ranges::RealInRange { :>> range = -1000.0 .. 1000.0; }\n")
            for (i in 1..depth) {
                val range = if (i == depth) "100.0 .. 100.0" else "-1000.0 .. 1000.0"
                append("feature x$i: Ranges::RealInRange = x${i - 1} + 1.0 { :>> range = $range; }\n")
            }
        }
        loadKerML(source, Runlevel.ALL)
        assertNoIssues()
        assertBounds(70.0, solver.variable("x0"))
        assertTrue(status.numberOfPropagateIterations <= 6, "iterations: ${status.numberOfPropagateIterations}")
    }
}
