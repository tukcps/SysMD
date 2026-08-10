package compiler.semantics

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.getOwnedElement
import util.assertNoIssues
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ActionContextTests {


    /**
     * To merge properly into a session of a notebook,
     * the UUID that are generated must be name-based, not random.
     * Here, we just check that two repeated executions
     * 1) create packages in a session as given by the prefix,
     * 2) merge into one package after execution (via Package.update() in import)
     */
    @Test
    fun initOwningPackagesTestInSessionWithMerge() = testSession {
        // cell 1 introduces some code ...
        var elements = KerML(this).parse("namespace n1;", "foo::bar")
        import(elements, "foo::bar")
        assertNoIssues()
        val foo = global.getOwnedElement("foo")
        assertNotNull(foo)
        val bar = global.resolve("foo::bar")?.member<Package>()
        assertNotNull(bar)

        // cell 2 introduces some additional code, result to be added to package foo::bar
        elements = KerML(this).parse("namespace n2;", "foo::bar")
        import(elements)
        val foo2 = global.getOwnedElement("foo")
        val bar2 = global.resolve("foo::bar")?.member<Package>()
        assertNotNull(foo2)
        assertNotNull(bar2)
        assertEquals(foo, foo2)
        assertEquals(bar, bar2)
        assertEquals(foo.elementId, foo2.elementId)
        assertEquals(bar.elementId, bar.elementId)
        assertNoIssues()

        assertEquals(1, bar.ownedElement.size)
        // TODO: fails because owning membership has same id for both runs (both have ../1/!) that must be different.
        /*
            METHOD:
            - !! get owning relationship # as offset from model/session/notebook !!
            - Use this as offset for generation of UUID of top-level owning memberships.
            --> overall prefix then: prefix/offset+index/... (only top-level owning memberships.
            --> others are unchanged, abut use path of owning prefix
         */
    }
}