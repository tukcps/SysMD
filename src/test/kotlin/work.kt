
import com.github.tukcps.sysmd.model.kerml.Anything
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Membership
import com.github.tukcps.sysmd.model.kerml.implementation.ElementImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.MembershipImplementation
import com.github.tukcps.sysmd.services.check.checkLibraryElementIds
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.loadLibrary
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * We keep here tests from daily works; they eventually might be moved to other tests if
 * considered generally useful.
 */
class WorkInProgress {

    @Test fun work() = testSession {
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
        val anything = global.resolveNew("c::Anything")?.memberElement as Anything
        assertEquals(anything, this.anything)
        // val f2f = global.resolveNew("f2:f")
    }


    @Ignore
    @Test
    fun benchmarkLoading() = testSession {
        val start = System.currentTimeMillis()
        loadLibrary("Base")
        loadLibrary("ScalarValues")
        loadLibrary("Occurrences")
        loadLibrary("Links")
        loadLibrary("KerML")
        initialize(5)
        val end = System.currentTimeMillis()
        val duration = end - start
        // reset
        val start2 = System.currentTimeMillis()
        loadLibrary("Base.md")
        loadLibrary("ScalarValues.md")
        loadLibrary("Occurrences.md")
        loadLibrary("Links.md")
        loadLibrary("KerML.md")
        assertNoIssues()
        val end2 = System.currentTimeMillis()
        initialize(5)
        assertNoIssues()
        val end3 = System.currentTimeMillis()
        val duration2 = end2 - start2
        val duration3 = end3 - end2
        println("loading libraries from resources: $duration")
        println("loading libraries from cache    : $duration2")
        println("only initialization             : $duration3")
    }

    @Test
    fun ownershipIssue() = testSession {
        loadKerML("""
            feature foo;
            feature bar;
        """.trimIndent())
        checkOwnership()
        assertNoIssues()

        val data = export().map { it.payloadElementSnapshot!! }

        testSession {
            import(data)
            checkOwnership()
            assertNoIssues()

            val foo = global.resolve("foo")!!.member<Feature>()!!
            val bar = global.resolve("bar")!!.member<Feature>()!!

            addOwnedRelationship(MembershipImplementation(
                membershipOwningNamespace = foo,
                memberElement = bar
            ).apply(ElementImplementation::generateUUID))
            checkOwnership()
            assertNoIssues()

            import(data)
            checkOwnership()
            assertNoIssues()
        }
    }
}