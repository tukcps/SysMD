

import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.kerml.Membership
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * We keep here tests from daily works; they eventually might be moved to other tests if
 * considered generally useful.
 */
class WorkInProgress {

    /**
     * WiP: resolve as in standard ...
     */
    @Test
    fun work() = testSession {
        loadKerML("""
            namespace c {
                import Base::Anything;             
            } 
            namespace a {
                namespace x; 
                namespace b {
                    namespace x; 
                    namespace c; 
                    namespace y; 
                }
                namespace c; 
            }
            namespace x; 
            type t :> Base::Anything {
                feature f; 
            }
            feature f2: t; 
        """)
        val r = global.resolveNew("a::b::c")
        assertTrue(r is Membership)
        assertEquals("a::b::c", r.memberElement.qualifiedName)
        val anything = global.resolveNew("c::Anything")?.memberElement as Classifier
        assertEquals(anything, this.repo.anything)
        // val f2f = global.resolveNew("f2:f")
    }


}