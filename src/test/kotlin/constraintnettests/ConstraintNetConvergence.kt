package test.constraintnettests

import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.resolve.resolveVar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.math.PI

class ConstraintNetConvergence {

    /**
     * In multiplication and division, jAADD has not yet full consideration of FP round-off.
     * Lets seen how good this is handled in AST-BinOp where we add some slack.
     */
    @Test
    fun convergenceTest()  {
        testSession(catchExceptions = false) {
            loadSysMD(input = """
                    attribute r:       ScalarValues::Real = 1000.0;
                    attribute mass:    ScalarValues::Real = density * volume;
                    attribute volume:  ScalarValues::Real = 4.0/3.0 * 3.14159265359 * r*r*r; 
                    attribute density: ScalarValues::Real = 1.0; 
                 """)
            assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
            initialize()
            propagate()
            assertEquals(4.0/3.0*PI*1E9,
                global.resolveVar("volume")!!.vectorQuantity.getMinAsDouble(), 10000.0)
            assertEquals(4.0/3.0*PI*1E9,
                global.resolveVar("mass")!!.vectorQuantity.getMinAsDouble(), 10000.0)
        }
    }

    /**
     * Convergence of LP solver is vague if large and small numbers occur in LP problem
     */
    @Test fun convergenceTest3()  = assertTimeoutPreemptively(Duration.ofMillis(800)) {
        testSession {
            loadSysMD(input = """
                attribute r:       ScalarValues::Real = 1000.0; 
                attribute mass:    ScalarValues::Real = density * volume; 
                attribute volume:  ScalarValues::Real = 4.0/3.0*3.141 * r * r; 
                attribute density: ScalarValues::Real = 10.0;              
            """)
            propagate()
        }
    }

    /**
     * In multiplication and division, jAADD has not yet full consideration of FP round-off.
     * Lets seen how good this is handled in AST-BinOp where we add some slack.
     */
    @Test @Timeout(value = 1, unit = TimeUnit.SECONDS)
    fun convergenceTest2() = assertTimeoutPreemptively(Duration.ofMillis(1000)) {
        testSession {
            settings.catchExceptions = false
            loadSysMD("""
                    attribute r:       ScalarValues::Real = 100.0.
                    attribute mass:    ScalarValues::Real = density * volume.
                    attribute volume:  ScalarValues::Real = 4.0/3.0*3.141*r*r*r.
                    attribute density: ScalarValues::Real = 1.0.
                    """)
            propagate()
            assertEquals(0, status.exceptions.size, status.exceptions.toString())
            assertEquals(41.88749E5, global.resolveVar("volume")!!.vectorQuantity.getMinAsDouble(), 0.01E5)
        }
    }
}

