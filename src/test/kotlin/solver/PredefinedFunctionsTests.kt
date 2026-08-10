package solver

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("UNUSED_VARIABLE")
class PredefinedFunctionsTests {

    val tol = 0.0001

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
        """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(ln(5.0), solver.getVariable("test1")!!.min(), 0.00001)
        assertEquals(sqrt(5.0), solver.getVariable("test2")!!.min(), 0.00001)
        assertEquals(exp(5.0), solver.getVariable("test3")!!.min(), 0.00001)
        assertEquals(32.0, solver.getVariable("test4")!!.min(), 0.00001)
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
        assertEquals(3.0, solver.getVariable("p")!!.min(), 0.001)
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
        val z = solver.getVariable("z")!!.vectorQuantity.value.asAadd()
        assertTrue(1.0 in z)
        // assertEquals(-1.7976931348623157E308, resolveName<Expression>(global, "z")!!.quantity.value.asAadd().getRange().min, tol)
        // assertEquals(1.7976931348623157E308, resolveName<Expression>(global, "z")!!.quantity.value.asAadd().getRange().max, tol)
    }

}