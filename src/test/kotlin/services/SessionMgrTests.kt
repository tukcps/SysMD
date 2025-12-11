package services

import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test

class SessionMgrTests {
    @Test
    fun exportTest() = testSession {
        // checkConsistency(get())
        export()
    }

    @Test
    fun exportTestWithInitializePropagateDigital() = testSession("ScalarValues", "Ranges") {
        loadKerML(input = """
            feature x: RealInRange {:>> range = "1.0..3.0";}
            feature y: Real = x+0.1; 
            feature r: Requirement = x >= y; 
            """.trimIndent())
        solver.propagate()
        export()
        // Export does some checks
    }
}