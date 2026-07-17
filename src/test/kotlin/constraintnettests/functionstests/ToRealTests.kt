package constraintnettests.functionstests

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.IDD
import kotlin.test.Test
import kotlin.test.Ignore
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.math.*
import kotlin.test.*

class ToRealTests {

        /**
         * Test of the function to Real(x) : Boolean -> ScalarValues::Real.
         */
        @Test
        fun toRealTest() = testSession("ScalarValues") {
            loadKerML("""
                    feature a: ScalarValues::Boolean = true; 
                     feature b: ScalarValues::Real = toReal(a); 
                    feature c: ScalarValues::Boolean = false; 
                    feature d: ScalarValues::Real = toReal(c);  
                    feature e: ScalarValues::Boolean;
                    feature f: ScalarValues::Real = toReal(e);
            """, Runlevel.ALL)
            assertNoIssues()
            val b = solver.getVariable("b")!!
            val d = solver.getVariable("d")!!
            val f = solver.getVariable("f")!!
            assertEquals(1.0, b.aadd().min, 0.00000001)
            assertEquals(1.0, b.aadd().max, 0.00000001)
            assertEquals(0.0, d.aadd().min, 0.00000001)
            assertEquals(0.0, d.aadd().max, 0.00000001)
            assertEquals(0, d.vectorQuantity.value.height())
            assertEquals(0.0, f.aadd().min, 0.00000001)
            assertEquals(1.0, f.aadd().max, 0.00000001)
            assertEquals(1, f.vectorQuantity.value.height())
        }

        @Test @Ignore
        fun toRealEvalDownTest() = testSession("Ranges") {
            loadKerML("""
                    feature a: ScalarValues::Boolean; 
                    feature b: Ranges::RealInRange  = toReal(a) {:>> range = "1.0 .. 1.0";}  
            """)
            assertNoIssues()
            val a = solver.getVariable("a")
            assertTrue((a!!.vectorQuantity.value === builder.True))

        }


        @Test
        fun realFunctionTest() = testSession("ScalarValues") {
            loadKerML("""
                feature i: ScalarValues::Integer = [2 .. 3].
                feature r: ScalarValues::Real = ToReal(i).
                """)
            solver.propagate()
            val r = solver.getVariable("r")!!
            assertNoIssues()
            assertEquals(2.0, r.min(), 0.00001)
            assertEquals(3.0, r.vectorQuantity.getMaxAsDouble(), 0.00001)
        }
}
