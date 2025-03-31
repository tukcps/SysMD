package services

import com.github.tukcps.sysmd.cspsolver.propagate
import util.mockup.loadKerML
import org.junit.jupiter.api.Test
import util.testSession

class SessionMgrTests {
    @Test
    fun exportTest() = testSession {
        // checkConsistency(get())
        export()
    }

    @Test
    fun exportTestWithInitializePropagateDigital() = testSession("ScalarValues") {
        loadKerML(input = """
            feature x: Real(1.0..3.0);
            feature y: Real = x+0.1; 
            feature r: Requirement = x >= y; 
            """.trimIndent())
        propagate()
        export()
        // Export does some checks
    }
}