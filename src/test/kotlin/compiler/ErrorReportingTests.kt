package compiler

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertIssue
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMD
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ErrorReportingTests {

    @Test
    fun inputInformationPassed() = testSession("") {
        val input = "  package Test;   "
        val exportFromCompiler = KerML(this).parse(input)
        val test = exportFromCompiler.find { it.declaredName == "Test" }
        assertNotNull(test)
        assertEquals(input, test.input)
        assertEquals(2..14, test.indices)
        assertEquals("package Test;", input.substring(test.indices!!))
    }

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
        val x = solver.getVariable("x")
        assertEquals(builder.Empty, x?.aadd())
        // shall we report an error? eventually, a user wants exactly this.
        // assertTrue(status.reports.isNotEmpty() )
    }

    /**
     * Actually, according to standard this is OK.
     * For SysMD solver, ... a problem.
     */
    @Test
    fun reportTypeIncompatible() = testSession("ScalarValues") {
        loadKerML("feature x: ScalarValues::Real(2.0 .. 3.0) = 1 + 2.0;", Runlevel.ALL)
        val x = solver.getVariable("x")
        assertIssue("CAST")
    }

    @Test
    fun createdElementsTest() = testSession(runlevel = Runlevel.MODEL) {
        loadSysMD("""
            xx::yyy hasA feature p: Base::Anything. 
        """)
        assertNoIssues()
        // SysMD works directly on model, hence 23 + 23
        val nr = if (settings.includeOwningRelationshipsToRoot) 23 else 23
        assertEquals(nr, get().size)
    }

    @Test
    fun unresolvedTypeTest() = testSession("ScalarValues") {
        loadKerML("""
            type t :> x;  
        """, Runlevel.MODEL)
        assertIssue("x")
        val x = status.issues.first()
        assertNotNull(x.input)
        assertNotNull(x.indices)
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