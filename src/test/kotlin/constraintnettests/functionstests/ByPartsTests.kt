package constraintnettests.functionstests

import util.variable
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class ByPartsTests {

        @Test
        fun byParts() = testSession("Occurrences", "Ranges") {
            loadKerML("""
                class c {
                    feature a: Ranges::RealInRange {:>> range = 0..10;}
                }
                class c1 :> c {
                    feature a: Ranges::RealInRange {:>> range = 0..5;}
                }

                class c2 :> c {
                    feature a: Ranges::RealInRange {:>> range = 0..2;}
                }

                class b {
                    feature cElemem1: c1; 
                    feature cElemen2: c2; 
                    feature a: ScalarValues::Real = byParts(a); 
                }
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val result = solver.getVariable("b::a")
            assertBounds(0.0 .. 5.0, result!!)
            assertNoIssues()
        }

        @Test
        fun byPartsEvalDown() = testSession("Occurrences", "Ranges") {
            loadKerML("""
                class c {
                    feature a: Ranges::RealInRange {:>> range = 0..10;}
                }
                class c1 :> c {
                    feature a: Ranges::RealInRange {:>> range = 0..5;}
                }

                class c2 :> c {
                    feature a: Ranges::RealInRange {:>> range = 0..2;}
                }

                class b {
                    feature cElemem1: c1; 
                    feature cElemen2: c2; 
                    feature a: Ranges::RealInRange = byParts(a) {:>> range = 1..3;} 
                }
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            val a1 = solver.variable("b::cElemem1::a")
            val a2 = solver.variable("b::cElemen2::a")
            assertBounds(1.0 .. 3.0, a1)
            assertBounds(1.0 .. 2.0, a2)
        }
}
