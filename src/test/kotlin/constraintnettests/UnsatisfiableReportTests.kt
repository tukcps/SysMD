package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class UnsatisfiableReportTests {

    /** An unsatisfiable dependency is reported, if no constraint is reported as not satisfiable. */
    @Test
    fun dependencyReportedWithoutConstraint() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange { :>> range = 1.0 .. 3.0; }
            feature y: Ranges::RealInRange = x + 10.0 { :>> range = 0.0 .. 5.0; }
        """, Runlevel.ALL)
        println(status.issues.map { it.message })
        assertTrue(status.issues.any { it.message.startsWith("dependency of ") && it.message.endsWith("is not satisfiable") })
    }

    /** If a constraint is reported as not satisfiable, the reports about its unsatisfiable dependencies are redundant. */
    @Test
    fun dependencyNotReportedIfConstraintIsReported() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange { :>> range = 1.0 .. 3.0; }
            feature y: ScalarValues::Real = x * 2.0;
            inv r { y >= 10.0 }
        """, Runlevel.ALL)
        println(status.issues.map { it.message })
        assertTrue(status.issues.any { it.message.startsWith("Constraint ") && it.message.contains("not satisfiable") })
        assertTrue(status.issues.none { it.message.startsWith("dependency of ") })
    }
}
