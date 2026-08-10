package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

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
            assertEquals(5.0, solver.getVariable("a")!!.min(), 0.00001)
            assertEquals(5.0, solver.getVariable("a")!!.max(), 0.00001)
            assertEquals(1.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(5.0, solver.getVariable("b")!!.max(), 0.00001)
            assertEquals(0.0, solver.getVariable("c")!!.min(), 0.00001)
            assertEquals(1.0, solver.getVariable("c")!!.max(), 0.00001)
            assertEquals(0.0, solver.getVariable("d")!!.min(), 0.00001)
            assertEquals(0.0, solver.getVariable("d")!!.max(), 0.00001)
            assertEquals(0.0, solver.getVariable("e")!!.min(), 0.00001)
            assertEquals(5.0, solver.getVariable("e")!!.max(), 0.00001)
            assertEquals(0.0, solver.getVariable("f")!!.min(), 0.00001)
            assertEquals(5.0, solver.getVariable("f")!!.max(), 0.00001)
            assertEquals(0.0, solver.getVariable("g")!!.min(), 0.00001)
            assertEquals(5.0, solver.getVariable("g")!!.max(), 0.00001)
            assertEquals(1.0, solver.getVariable("h")!!.min(), 0.00001)
            assertEquals(5.0, solver.getVariable("h")!!.max(), 0.00001)
            assertEquals(0.0, solver.getVariable("i")!!.min(), 0.00001)
            assertEquals(5.0, solver.getVariable("i")!!.max(), 0.00001)
            assertEquals(5.0, solver.getVariable("j")!!.min(), 0.00001)
            assertEquals(5.0, solver.getVariable("j")!!.max(), 0.00001)
            assertNoIssues()
        }

        @Test
        fun absTestInteger() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = 5;}
                feature a: ScalarValues::Integer = abs(qa);
            """, Runlevel.ALL)
            assertNoIssues()
            assertEquals(5L, solver.getVariable("a")!!.min())
            assertEquals(5L, solver.getVariable("a")!!.max())
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
            assertEquals(2.0, result!!.min(), 0.000001)
            assertEquals(6.0, result.max(), 0.000001)
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
            assertEquals(3L, solver.getVariable("a")!!.min())
            assertEquals(3L, solver.getVariable("a")!!.max())
        }

        @Test
        fun absTestInteger_negative_range() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = -5 .. -3;}
                feature a: ScalarValues::Integer = abs(qa);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(3, solver.getVariable("a")!!.idd().getRange().min)
            assertEquals(5, solver.getVariable("a")!!.idd().getRange().max)
        }

        @Test
        fun absTestIntegerMixed() = testSession("Ranges") {
            loadKerML("""
                feature qa: Ranges::IntegerInRange {:>> range = -3 .. 3;}
                feature a: ScalarValues::Integer = abs(qa);
            """)
            solver.propagate()
            assertNoIssues()
            assertEquals(0L, solver.getVariable("a")!!.min())
            assertEquals(3L, solver.getVariable("a")!!.max())
        }

        @Test
        fun absTestEvalDownMixed() = testSession("Ranges") {
            loadKerML(input = """
                feature a: Ranges::RealInRange {:>> range = -8.0..8.0;}
                feature b: Ranges::RealInRange = abs(a) {:>> range = 6.0;}
            """, Runlevel.ALL)
            assertNoIssues()
            val result = solver.getVariable("a")!!
            assertEquals(-6.0, result.min(), 0.000001)
            assertEquals(6.0, result.max(), 0.000001)
        }
}
