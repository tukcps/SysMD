package services

import com.github.tukcps.sysmd.services.Runlevel
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
    fun exportTestWithInitializePropagateDigital() = testSession("Ranges") {
        loadKerML(input = """
            feature x: Ranges::RealInRange {:>> range = 1.0..3.0;}
            feature y: ScalarValues::Real = x+0.1; 
            feature r: ScalarValues::Boolean = x >= y; 
            """, Runlevel.ALL)
        export()
        // Export does some checks
    }
}