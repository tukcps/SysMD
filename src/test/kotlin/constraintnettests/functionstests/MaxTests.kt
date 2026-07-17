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

class MaxTests {

        @Test
        fun maxTest1() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "0..1";}
                feature b: Ranges::RealInRange {:>> range = "1..2";}
                feature c: ScalarValues::Real = max(a,b);
            """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("c")!!
            assertEquals(1.0, result.min(), 0.000001)
            assertEquals(2.0, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTest1EvalDown() = testSession("Ranges") {
            loadKerML(input = """
                feature a: Ranges::RealInRange {:>> range = "1..7";}
                feature b: Ranges::RealInRange = max(7.0,2.0+a) {:>> range = "8.0..8.0";}
            """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("a")
            assertEquals(6.0, result!!.min(), 0.000001)
            assertEquals(6.0, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTest1bEvalDown() = testSession("Ranges") {
            loadKerML(input = """
                feature a: ScalarValues::Real = oneOf(1.0 .. 7.0);
                feature b: Ranges::RealInRange = max(8.0, 2.0+a) {:>> range = "8.0..8.0";}
                """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("a")
            assertEquals(1.0, result!!.min(), 0.000001)
            assertEquals(6.0, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTest2() = testSession("Ranges") {
            loadKerML(input = """
                feature a: Ranges::RealInRange = sqrt(9.0) {:>> range = "0..5";}
                feature b: Ranges::RealInRange = sqrt(4.0) {:>> range = "1..6";}
                feature c: Ranges::RealInRange = max(a,b);
                """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("c")!!
            assertEquals(3.0, result.min(), 0.000001)
            assertEquals(3.0, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTest2EvalDown() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange = sqrt(9.0) {:>> range = "0..6";}
                feature b: Ranges::RealInRange {:>> range = "1..6";}
                feature c: Ranges::RealInRange = max(a,b) {:>> range = "4.0..4.0";}
            """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("b")
            assertEquals(4.0, result!!.min(), 0.000001)
            assertEquals(4.0, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTest3() = testSession("Ranges") {
            loadKerML(input = """
                feature a: Ranges::RealInRange = 4.0 {:>> range = "0..5";}
                feature b: Ranges::RealInRange = 6.0 {:>> range = "1..6";}
                feature c: Ranges::RealInRange = max(sqrt(9.0)+a,sqrt(4.0)+b);
                """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("c")
            assertEquals(8.0, result!!.min(), 0.000001)
            assertEquals(8.0, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTest4() = testSession("ScalarValues") {
            loadKerML(input = """
                      feature c: ScalarValues::Real = max(sqrt(9.0)+3.0,sqrt(4.0)+5.0);
                """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("c")
            assertEquals(7.0, result!!.min(), 0.000001)
            assertEquals(7.0, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTestNegative() = testSession("ScalarValues") {
            loadKerML(input = """
                      feature c: ScalarValues::Real = max(-4.2,-1.3);
                """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("c")
            assertEquals(-1.3, result!!.min(), 0.000001)
            assertEquals(-1.3, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTestMultipleParams1() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = "0..1";}
                feature b: Ranges::RealInRange {:>> range = "1..2";}
                feature c: Ranges::RealInRange {:>> range = "3..4";}
                feature d: Ranges::RealInRange {:>> range = "4..5";}
                feature e: ScalarValues::Real = max(a, b, c, d);
            """, Runlevel.ALL)
            assertNoIssues()
            val result = solver.getVariable("e")
            assertEquals(4.0, result!!.min(), 0.000001)
            assertEquals(5.0, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTestMultipleParamsInt1() = testSession("Ranges") {
            loadKerML(
                input = """
                feature a: Ranges::IntegerInRange {:>> range = "0..1";}
                feature b: Ranges::IntegerInRange {:>> range = "1..2";}
                feature c: Ranges::IntegerInRange {:>> range = "3..4";}
                feature d: Ranges::IntegerInRange {:>> range = "4..5";}
                feature e: ScalarValues::Integer = max(a,b,c,d);
                """
            )
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("e")
            assertEquals(4, result!!.vectorQuantity.value.asIdd().min)
            assertEquals(5, result.vectorQuantity.value.asIdd().max)
            assertNoIssues()
        }

        @Test
        fun maxTestMultipleParamsReal1() = testSession("Ranges") {
            loadKerML(
                input = """
                feature a: Ranges::RealInRange {:>> range = "0.67..1.96";}
                feature b: Ranges::RealInRange {:>> range = "1.34..2.5";}
                feature c: Ranges::RealInRange {:>> range = "3.49..4.99";}
                feature d: Ranges::RealInRange {:>> range = "4.32..5.45";}
                feature e: ScalarValues::Real = max(a,b,c,d);
            """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("e")
            assertEquals(4.32, result!!.min(), 0.000001)
            assertEquals(5.45, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTestMultipleParamsRealNegative() = testSession("Ranges") {
            loadKerML(
                input = """
                feature a: Ranges::RealInRange {:>> range = "-1.67..-1.6";}
                feature b: Ranges::RealInRange {:>> range = "-2.34..-1.5";}
                feature c: Ranges::RealInRange {:>> range = "-4.49..-2.99";}
                feature d: Ranges::RealInRange {:>> range = "-6.32..-5.45";}
                feature e: ScalarValues::Real = max(a,b,c,d);
            """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("e")
            assertEquals(-1.67, result!!.min(), 0.000001)
            assertEquals(-1.5, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTestMultipleParamsIntegerNegative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = "-2..-1";}
                feature b: Ranges::IntegerInRange {:>> range = "-4..-2";}
                feature c: Ranges::IntegerInRange {:>> range = "-5..-3";}
                feature d: Ranges::IntegerInRange {:>> range = "-7..-5";}
                feature e: Ranges::IntegerInRange = max(a,b,c,d);
            """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("e")
            assertEquals(-2, result!!.vectorQuantity.value.asIdd().min)
            assertEquals(-1, result.vectorQuantity.value.asIdd().max)
            assertNoIssues()
        }

        @Ignore

        @Test
        fun maxTestMultipleParams2() = testSession("Ranges") {
            loadKerML(input ="""
                feature a: ScalarValues::Real {:>> range = "0..1";}
                feature b: ScalarValues::Real {:>> range = "1..2";}
                feature c: ScalarValues::Real {:>> range = "2..3";}
                feature d: ScalarValues::Real {:>> range = "3..7";}
                feature e: ScalarValues::Real = max(a,b,c,d) {:>> range = "4..4";}
            """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("d")
            assertEquals(4.0, result!!.min(), 0.000001)
            assertEquals(4.0, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTestMultipleParams2Integer() = testSession("Ranges") {
            loadKerML(
                input ="""
                feature a: Ranges::IntegerInRange {:>> range = "0..1";}
                feature b: Ranges::IntegerInRange {:>> range = "1..2";}
                feature c: Ranges::IntegerInRange {:>> range = "2..3";}
                feature d: Ranges::IntegerInRange {:>> range = "3..7";}
                feature e: Ranges::IntegerInRange = max(a,b,c,d) {:>> range = "4..4";}
            """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("d")
            assertEquals(4, result!!.vectorQuantity.value.asIdd().min)
            assertEquals(4, result.vectorQuantity.value.asIdd().max)
            assertNoIssues()
        }

        @Test
        fun maxTestMultipleParams3() = testSession("Ranges") {
            loadKerML(
                input = """
                feature a: Ranges::RealInRange {:>> range = "0..7";}
                feature b: Ranges::RealInRange {:>> range = "1..6";}
                feature c: Ranges::RealInRange {:>> range = "2..5";}
                feature d: Ranges::RealInRange {:>> range = "3..4";}
                feature e: Ranges::RealInRange = max(a,b,c,d) {:>> range = "3..4";}
                """.trimIndent(), catchExceptions = true
            )
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("a")
            assertEquals(0.0, result!!.min(), 0.000001)
            assertEquals(4.0, result.max(), 0.000001)
            val result1 = solver.getVariable("b")
            assertEquals(1.0, result1!!.min(), 0.000001)
            assertEquals(4.0, result1.max(), 0.000001)
            val result2 = solver.getVariable("c")
            assertEquals(2.0, result2!!.min(), 0.000001)
            assertEquals(4.0, result2.max(), 0.000001)
            val result3 = solver.getVariable("d")
            assertEquals(3.0, result3!!.min(), 0.000001)
            assertEquals(4.0, result3.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun maxTestMultipleParams3Integer() = testSession("Ranges") {
            loadKerML(
                input = """
                feature a: Ranges::IntegerInRange {:>> range = "0..7";}
                feature b: Ranges::IntegerInRange {:>> range = "1..6";}
                feature c: Ranges::IntegerInRange {:>> range = "2..5";}
                feature d: Ranges::IntegerInRange {:>> range = "3..4";}
                feature e: Ranges::IntegerInRange = max(a,b,c,d) {:>> range = "3..4";}
            """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("a")
            assertEquals(0, result!!.vectorQuantity.value.asIdd().min)
            assertEquals(4, result.vectorQuantity.value.asIdd().max)
            val result1 = solver.getVariable("b")
            assertEquals(1, result1!!.vectorQuantity.value.asIdd().min)
            assertEquals(4, result1.vectorQuantity.value.asIdd().max)
            val result2 = solver.getVariable("c")
            assertEquals(2, result2!!.vectorQuantity.value.asIdd().min)
            assertEquals(4, result2.vectorQuantity.value.asIdd().max)
            val result3 = solver.getVariable("d")
            assertEquals(3, result3!!.vectorQuantity.value.asIdd().min)
            assertEquals(4, result3.vectorQuantity.value.asIdd().max)
            assertNoIssues()
        }

        @Test
        fun maxTestOneValue1() = testSession("Ranges") {
            loadKerML(
                input ="""
                feature a: Ranges::RealInRange {:>> range = "0..1.5";}
                feature c: ScalarValues::Real = max(a);
            """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("c")
            assertEquals(1.5, result!!.min(), 0.000001)
            assertEquals(1.5, result.max(), 0.000001)
            assertEquals(1, result.vectorQuantity.values.size )
            assertNoIssues()
        }

        @Test
        fun maxTestOneValueInt1() = testSession("Ranges") {
            loadKerML(input = """
                feature a: Ranges::IntegerInRange {:>> range = "0..1";}
                feature c: ScalarValues::Integer = max(a);
            """, Runlevel.ALL)
            assertNoIssues()
            val result = solver.getVariable("c")
            assertEquals(1, result!!.vectorQuantity.value.asIdd().min)
            assertEquals(1, result.vectorQuantity.value.asIdd().max)
            assertEquals(1, result.vectorQuantity.values.size )
            assertNoIssues()
        }

        /** Max on Real scalars */
        @Test
        fun max_quantity() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Real = 1.0;
                feature b: ScalarValues::Real = 2.0; 
                feature y: ScalarValues::Real = max(a, b); 
            """, com.github.tukcps.sysmd.services.Runlevel.ALL)
            val y = solver.getVariable("y")!!
            assertEquals(2.0, y.min(), 0.0001)
            assertEquals(2.0, y.max(), 0.0001)
            assertNoIssues()
        }

        /** Max on Integer scalars */
        @Test
        fun max_quantity2() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Integer = 1;
                feature b: ScalarValues::Integer = 2;
                feature y: ScalarValues::Integer = max(a, b).
            """, com.github.tukcps.sysmd.services.Runlevel.ALL)
            val y = solver.getVariable("y")!!
            assertEquals(2.0, y.min(), 0.0001)
            assertEquals(2.0, y.max(), 0.0001)
            assertNoIssues()
        }
}
