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

class ByPartsTests {

        @Test
        fun byParts() = testSession("Occurrences", "Ranges") {
            loadKerML("""
                class c {
                    feature a: Ranges::RealInRange {:>> range = "0..10";}
                }
                class c1 :> c {
                    feature a: Ranges::RealInRange {:>> range = "0..5";}
                }

                class c2 :> c {
                    feature a: Ranges::RealInRange {:>> range = "0..2";}
                }

                class b {
                    feature cElemem1: c1; 
                    feature cElemen2: c2; 
                    feature a: ScalarValues::Real = byParts(a); 
                }
            """, Runlevel.ALL)
            assertNoIssues()
            val result = solver.getVariable("b::a")
            assertEquals(0.0, result!!.min(), 0.000001)
            assertEquals(5.0, result.max(), 0.000001)
            assertNoIssues()
        }

        @Test
        fun byPartsEvalDown() = testSession("Occurrences", "Ranges") {
            loadKerML("""
                class c {
                    feature a: Ranges::RealInRange {:>> range = "0..10";}
                }
                class c1 :> c {
                    feature a: Ranges::RealInRange {:>> range = "0..5";}
                }

                class c2 :> c {
                    feature a: Ranges::RealInRange {:>> range = "0..2";}
                }

                class b {
                    feature cElemem1: c1; 
                    feature cElemen2: c2; 
                    feature a: Ranges::RealInRange = byParts(a) {:>> range = "1..3";} 
                }
            """, Runlevel.ALL)
            assertNoIssues()
            val a1 = solver.getVariable("b::cElemem1::a")!!
            val a2 = solver.getVariable("b::cElemen2::a")!!
            assertEquals(1.0, a1.min(), 0.000001)
            assertEquals(3.0, a1.max(), 0.000001)
            assertEquals(1.0, a2.min(), 0.000001)
            assertEquals(2.0, a2.max(), 0.000001)
        }
}
