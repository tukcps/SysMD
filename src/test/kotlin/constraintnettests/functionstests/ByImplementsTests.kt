package constraintnettests.functionstests

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Connector
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
            val fx = solver.getVariable("f::x")!!
            assertEquals(1.0, fx.min(), 0.0001)
            assertEquals(0, status.issues.size, status.issues.toString())
        }
}
