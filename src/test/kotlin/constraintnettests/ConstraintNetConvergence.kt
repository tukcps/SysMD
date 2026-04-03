package constraintnettests

import com.github.tukcps.sysmd.services.resolve.resolveVar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.math.PI

class ConstraintNetConvergence {

    /**
     * In multiplication and division, AADD has not yet full consideration of FP round-off.
     * Let's see how good this is handled in AST-BinOp where we add some slack.
     */
    @Test
    fun convergenceTest()  {
        testSession("ScalarValues") {
            loadKerML(input = """
                    feature r:       ScalarValues::Real = 1000.0;
                    feature mass:    ScalarValues::Real = density * volume;
                    feature volume:  ScalarValues::Real = 4.0/3.0 * 3.14159265359 * r*r*r; 
                    feature density: ScalarValues::Real = 1.0; 
                 """)
            assertNoIssues()
            solver.propagate()
            assertEquals(4.0/3.0*PI*1E9,
                global.resolveVar("volume")!!.vectorQuantity.getMinAsDouble(), 10000.0)
            assertEquals(4.0/3.0*PI*1E9,
                global.resolveVar("mass")!!.vectorQuantity.getMinAsDouble(), 10000.0)
        }
    }

    /**
     * Convergence of LP solver is vague if large and small numbers occur in an LP problem
     */
    @Test fun convergenceTest3()  = assertTimeoutPreemptively(Duration.ofMillis(800)) {
        testSession("ScalarValues") {
            loadKerML(input = """
                feature r:       ScalarValues::Real = 1000.0; 
                feature mass:    ScalarValues::Real = density * volume; 
                feature volume:  ScalarValues::Real = 4.0/3.0*3.141 * r * r; 
                feature density: ScalarValues::Real = 10.0;              
            """)
            solver.propagate()
        }
    }

    /**
     * In multiplication and division, AADD has not yet full consideration of FP round-off.
     * Let's see how good this is handled in AST-BinOp where we add some slack.
     */
    @Test @Timeout(value = 1, unit = TimeUnit.SECONDS)
    fun convergenceTest2() = assertTimeoutPreemptively(Duration.ofMillis(1000)) {
        testSession("ScalarValues") {
            settings.catchExceptions = false
            loadKerML("""
                    feature r:       ScalarValues::Real = 100.0; 
                    feature mass:    ScalarValues::Real = density * volume;
                    feature volume:  ScalarValues::Real = 4.0/3.0*3.141*r*r*r; 
                    feature density: ScalarValues::Real = 1.0; 
                    """)
            solver.propagate()
            assertEquals(0, status.issues.size, status.issues.toString())
            assertEquals(41.88749E5, global.resolveVar("volume")!!.vectorQuantity.getMinAsDouble(), 0.01E5)
        }
    }
}

