package compiler.sysml

import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AssertAndConstraintsTests {

    @Test
    fun assertTestSyntax1() = testSession("Constraints") {
        loadSysMLv2("assert { true or false }", Runlevel.MODEL)
        assertNoIssues()
        val a = global.getOwnedElementOfType<Feature>()
        assertNotNull(a)
    }

    @Test
    fun assertTestSyntax1b() = testSession("Constraints") {
        loadSysMLv2("assert constraint { true or false }", Runlevel.MODEL)
        assertNoIssues()
        val constraint = global.getOwnedElementOfType<Invariant>()
        assertNotNull(constraint)
    }

    @Test
    fun assertTestSyntax3() = testSession("Constraints") {
        loadSysMLv2("assert not { true and false }", Runlevel.MODEL)
        assertNoIssues()
        val a = global.getOwnedElementOfType<Invariant>()
        assertNotNull(a)
        assertTrue(a.isNegated)
    }
}