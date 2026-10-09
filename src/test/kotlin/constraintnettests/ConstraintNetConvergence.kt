package constraintnettests

import util.variable
import util.assertBounds
import com.github.tukcps.sysmd.services.Runlevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Timeout
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.test.Test

class ConstraintNetConvergence {

    /**
     * In multiplication and division, AADD has not yet full consideration of FP round-off.
     * Let's see how good this is handled in AST-BinOp where we add some slack.
     */
    @Test
    fun convergenceTest()  {
        testSession("ScalarValues") {
            loadKerML("""
                feature r:       ScalarValues::Real = 1000.0;
                feature mass:    ScalarValues::Real = density * volume;
                feature volume:  ScalarValues::Real = 4.0/3.0 * 3.14159265359 * r*r*r; 
                feature density: ScalarValues::Real = 1.0; 
             """, Runlevel.ALL)
            assertNoIssues()
            assertBounds(4.1887902047866654E9..4.1887902047866683E9, solver.variable("volume"))
            assertBounds(4.1887902047866654E9..4.1887902047866683E9, solver.variable("mass"))
        }
    }

    /**
     * Convergence of LP solver is vague if large and small numbers occur in an LP problem
     */
    @Test fun convergenceTest3()  = assertTimeoutPreemptively(Duration.ofMillis(800)) {
        testSession("ScalarValues") {
            loadKerML("""
                feature r:       ScalarValues::Real = 1000.0; 
                feature mass:    ScalarValues::Real = density * volume; 
                feature volume:  ScalarValues::Real = 4.0/3.0*3.141 * r * r; 
                feature density: ScalarValues::Real = 10.0;              
            """, Runlevel.ALL)
            assertNoIssues()
        }
    }

    /**
     * In multiplication and division, AADD has not yet full consideration of FP round-off.
     * Let's see how good this is handled in AST-BinOp where we add some slack.
     */
    @Test @Timeout(value = 1, unit = TimeUnit.SECONDS)
    fun convergenceTest2() = assertTimeoutPreemptively(Duration.ofMillis(1000)) {
        testSession("ScalarValues") {
            loadKerML("""
                feature r:       ScalarValues::Real = 100.0; 
                feature mass:    ScalarValues::Real = density * volume;
                feature volume:  ScalarValues::Real = 4.0/3.0*3.141*r*r*r; 
                feature density: ScalarValues::Real = 1.0; 
            """)
            solver.propagate()
            assertNoIssues()
            assertBounds(4187999.9999999977..4188000.0000000005, solver.variable("volume"))
        }
    }
}
