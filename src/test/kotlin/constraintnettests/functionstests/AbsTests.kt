package constraintnettests.functionstests

import util.variable
import util.assertEmpty
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test

class AbsTests {

        @Test
        fun absTestReal() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::RealInRange {:>> range = 5.0 .. 5.0;}
                feature qb: Ranges::RealInRange {:>> range = 1.0 .. 5.0;} 
                feature qc: Ranges::RealInRange {:>> range = 0.0 .. 1.0;}
                feature qd: Ranges::RealInRange {:>> range = 0.0 .. 0.0;} 
                feature qe: Ranges::RealInRange {:>> range = -1.0 .. 5.0;} 
                feature qf: Ranges::RealInRange {:>> range = -5.0 .. 5.0;}
                feature qg: Ranges::RealInRange {:>> range = -5.0 .. 1.0;}
                feature qh: Ranges::RealInRange {:>> range = -5.0 .. -1.0;}
                feature qi: Ranges::RealInRange {:>> range = -5.0 .. 0.0;}
                feature qj: Ranges::RealInRange {:>> range = -5.0 .. -5.0;} 
                feature a: ScalarValues::Real = abs(qa); 
                feature b: ScalarValues::Real = abs(qb); 
                feature c: ScalarValues::Real = abs(qc); 
                feature d: ScalarValues::Real = abs(qd); 
                feature e: ScalarValues::Real = abs(qe); 
                feature f: ScalarValues::Real = abs(qf); 
                feature g: ScalarValues::Real = abs(qg); 
                feature h: ScalarValues::Real = abs(qh); 
                feature i: ScalarValues::Real = abs(qi); 
                feature j: ScalarValues::Real = abs(qj);
            """)
            solver.propagate()
            assertBounds(5.0 .. 5.0, solver.variable("a"))
            assertBounds(1.0 .. 5.0, solver.variable("b"))
            assertBounds(0.0 .. 1.0, solver.variable("c"))
            assertBounds(0.0 .. 0.0, solver.variable("d"))
            assertBounds(0.0 .. 5.0, solver.variable("e"))
            assertBounds(0.0 .. 5.0, solver.variable("f"))
            assertBounds(0.0 .. 5.0, solver.variable("g"))
            assertBounds(1.0 .. 5.0, solver.variable("h"))
            assertBounds(0.0 .. 5.0, solver.variable("i"))
            assertBounds(5.0 .. 5.0, solver.variable("j"))
            assertNoIssues()
        }

        @Test
        fun absTestInteger() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = 5;}
                feature a: ScalarValues::Integer = abs(qa);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(5L .. 5L, solver.variable("a"))
        }

        @Test
        fun absTestEvalDown() = testSession("Ranges") {
            loadKerML(input = """
                feature a: Ranges::RealInRange {:>> range = 2.0..8.0;}
                feature b: Ranges::RealInRange = abs(a) {:>> range = 6.0..6.0;}
            """)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("a")
            assertBounds(2.0 .. 6.0, result!!)
            assertNoIssues()
        }

        @Test
        fun absTestIntegerNegative() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = -3 .. -3;}
                feature a: ScalarValues::Integer = abs(qa);
            """)
            solver.propagate()
            assertNoIssues()
            assertBounds(3L .. 3L, solver.variable("a"))
        }

        @Test
        fun absTestInteger_negative_range() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = -5 .. -3;}
                feature a: ScalarValues::Integer = abs(qa);
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(3L..5L, solver.variable("a"))
        }

        @Test
        fun absTestIntegerMixed() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = -3 .. 3;}
                feature a: ScalarValues::Integer = abs(qa);
            """)
            solver.propagate()
            assertNoIssues()
            assertBounds(0L..3L, solver.variable("a"))
        }

        @Test
        fun absTestEvalDownMixed() = testSession("Ranges") {
            loadKerML(input = """
                feature a: Ranges::RealInRange {:>> range = -8.0..8.0;}
                feature b: Ranges::RealInRange = abs(a) {:>> range = 6.0;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val result = solver.variable("a")
            assertBounds(-6.0 .. 6.0, result)
        }

        @Test
        fun absTestEvalDownNegative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = -10.0 .. 10.0;}
                feature b: Ranges::RealInRange = abs(a) {:>> range = -5.0 .. -1.0;}
            """)
            solver.propagate()
            val b = solver.variable("b")
            assertEmpty(b)
        }

        @Test
        fun absTestEvalDownIntNegative() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = -10 .. 10;}
                feature b: Ranges::IntegerInRange = abs(a) {:>> range = -5 .. -1;}
            """)
            solver.propagate()
            val a = solver.variable("a")
            assertEmpty(a)
        }

        /**
         * Regression test for abs evalDown:
         * abs(a) in [0..5] generates [-5..5].
         * When 'a' is initially in [-10..-1], constraining with [-5..5] yields [-5..-1].
         */
        @Test
        fun absEvalDownPreservesPriorSignConstraintReal() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::RealInRange {:>> range = -10.0 .. -1.0;}
                feature b: Ranges::RealInRange = abs(a) {:>> range = 0.0 .. 5.0;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val a = solver.variable("a")
            assertBounds(-5.0 .. -1.0, a)
        }

        @Test
        fun absEvalDownPreservesPriorSignConstraintInt() = testSession("Ranges") {
            loadKerML("""
                feature a: Ranges::IntegerInRange {:>> range = 1 .. 10;}
                feature b: Ranges::IntegerInRange = abs(a) {:>> range = 0 .. 5;}
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val a = solver.variable("a")
            assertBounds(1L .. 5L, a)
        }
}
