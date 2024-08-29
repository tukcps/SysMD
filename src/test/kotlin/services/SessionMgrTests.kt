package services

import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.compiler.loadSysMDFromFile
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.cspsolver.propagate
import org.junit.jupiter.api.Test

class SessionMgrTests {
    @Test
    fun exportTest() = testSession {
        loadSysMDFromFile("ISO26262.md")
        export()
        // Export does some checks
    }

    @Test
    fun exportTestWithInitializePropagateDigital() = testSession("ISO26262") {
        loadSysMD(input = """
            expression x: Real(1.0..3.0).
            expression y: Real = x+0.1 .
            expression r: Requirement = x >= y.
            """.trimIndent())
        propagate()
        export()
        // Export does some checks
    }
}