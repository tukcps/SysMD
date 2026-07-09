package compiler

import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertIssue
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMD
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ErrorReportingTests {

    /**
     * Repeated execution of the same code does not add new errors.
     */
    @Test
    fun reportNotTwice() = testSession("ScalarValues") {
        loadKerML("""Global a b c.""".trimIndent())
        val nr = status.issues.size
        loadKerML("""Global a b c.""".trimIndent())
        assertEquals(nr, status.issues.size)
    }

    // Unsure whether we need this error or if we simply shall return an empty set.
    // @Ignore
    @Test
    fun reportConstraintWrong() = testSession("ScalarValues") {
        loadKerML("feature x: ScalarValues::Real(1.0 .. 0.0);", Runlevel.ALL)
        // x is empty
        val x = global.resolveVar("x")
        assertEquals(builder.Empty, x?.aadd())
        // shall we report an error? eventually, a user wants exactly this.
        // assertTrue(status.reports.isNotEmpty() )
    }

    @Test
    fun reportTypeIncompatible() = testSession("ScalarValues") {
        loadKerML("feature x: ScalarValues::Real(2.0 .. 3.0) = 1 + 2.0.", Runlevel.VARIABLES)
        assertIssue("incompatible")
    }

    @Test
    fun reportUnknownOwner() = testSession("ScalarValues", runlevel = Runlevel.ALL) {
        loadSysMD("""
            xx::yyy hasA feature p: Base::Anything. 
        """)
        assertNoIssues()
        assertEquals(3, status.createdElements.size)
    }

    @Test
    fun indicesTest() = testSession {
        loadSysMLv2("""
            package test;  
        """)
        val test = global.resolve("test")?.member<Package>()
        val token = test?.input?.substring(test.indices!!)
        assertEquals("test", token)
    }

    @Test
    fun unresolvedTypeTest() = testSession {
        loadKerML("""
            type t :> x;  
        """, Runlevel.MODEL)
        assertIssue("x")
        val x = status.issues.first()
        assertNotNull(x.input)
        assertNotNull(x.indices)
        // assertEquals("x", x.input?.substring(x.indices!!))
    }

    @Test
    fun unresolvedTargetTest() = testSession {
        loadKerML("""
            feature a; 
            dependency d from a to b; 
        """, Runlevel.MODEL)
        assertIssue("b")
        val b = status.issues.first()
        assertNotNull(b.input)
        assertNotNull(b.indices)
        // assertEquals("b", b.input?.substring(b.indices!!))
    }

    @Test
    fun unresolvedSourceTest() = testSession {
        loadKerML("""
            feature b; 
            dependency d from a to b; 
        """, Runlevel.MODEL)
        assertIssue("a")
        val a = status.issues.first()
        assertNotNull(a.input)
        assertNotNull(a.indices)
        // assertEquals("a", a.input?.substring(a.indices!!))
    }
}