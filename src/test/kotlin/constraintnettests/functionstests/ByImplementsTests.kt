package constraintnettests.functionstests

import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Connector
import util.*
import util.mockup.loadKerML
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ByImplementsTests {

        @Test
        fun byImplementsTest() = testSession("ScalarValues", "Links") {
            loadKerML("""
                package ISO26262 {
                    assoc implements {
                       end feature 'from': Base::Anything redefines source;
                       end feature 'to':  Base::Anything redefines target;  
                    }
                }
                feature c {
                    feature x: ScalarValues::Real = 1.0; 
                } 
                feature f {
                    feature x: ScalarValues::Real = byImplements(x). 
                }
                connector r : ISO26262::implements from c to f; 
            """)
            solver.propagate()
            val impl = global.resolve("ISO26262::implements")?.member<Association>()
            assertNoIssues()
            assertNotNull(impl)
            val r = global.resolve("r")?.member<Connector>()
            assertNotNull(r)
            val fx = solver.variable("f::x")
            assertBounds(1.0, fx)
            assertNoIssues()
        }
}
