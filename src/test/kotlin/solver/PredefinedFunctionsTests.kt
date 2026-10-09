package solver

import com.github.tukcps.sysmd.services.Runlevel
import kotlin.test.assertEquals
import util.*
import util.mockup.loadKerML
import kotlin.math.*
import kotlin.test.Test
import kotlin.test.assertTrue

@Suppress("UNUSED_VARIABLE")
class PredefinedFunctionsTests {
    // The operations exp, log, pow2, sqrt, ln ... are supported
    // also to test: ITE function
    @Test
    fun operationsTest() = testSession("ScalarValues") {
        loadKerML(
            """
                feature test1: ScalarValues::Real = ln(5.0);
                feature test2: ScalarValues::Real = sqrt(5.0);
                feature test3: ScalarValues::Real = exp(5.0);
                feature test4: ScalarValues::Real = power2(5.0);
                feature test5: ScalarValues::Real = inverseSqr(5.0);
        """
        )
        solver.propagate()
        assertNoIssues()
        assertBounds(ln(5.0), solver.variable("test1"))
        assertBounds(sqrt(5.0), solver.variable("test2"))
        assertBounds(exp(5.0), solver.variable("test3"))
        assertBounds(32.0, solver.variable("test4"))
        assertBounds(-sqrt(5.0)..sqrt(5.0), solver.variable("test5"))
    }


    @Test
    fun rangeOperatorTest() = testSession("ScalarValues") {
        loadKerML(
            """
            feature p: ScalarValues::Real = [1.0 .. 2.0] + 2.0;
            """
        )
        solver.propagate()
        assertNoIssues()
        assertBounds(3.0 .. 4.0, solver.variable("p"))
    }

    /**
     * stress test number 7 --> Poles and zeroes ...
     * see: the stress tests for AADD data types in AADDTests.kt in the jAADD
     * 9x^4 - y^4 + 2 y^2 = 1
     */
    @Test
    fun testAgainstRumpEquation7() = testSession("ScalarValues", "Ranges", runlevel = Runlevel.VARIANCE_CHECKED) {
        loadKerML(
            """
            feature x: Ranges::RealInRange {:>> range = 2910.99 .. 2911.001;}
            feature y: Ranges::RealInRange {:>> range = 5041.999 .. 5042.001;}
            feature z: ScalarValues::Real = 9.0 * x^4.0 - y^4.0 + 2.0 * y^2.0
            """
        )
        assertBounds(-9.392990800793E9..1.4007394881680021E9, solver.variable("z"))
        // assertEquals(-1.7976931348623157E308, resolveName<Expression>(global, "z")!!.quantity.value.asAadd().getRange().min, tol)
        // assertEquals(1.7976931348623157E308, resolveName<Expression>(global, "z")!!.quantity.value.asAadd().getRange().max, tol)
    }

}