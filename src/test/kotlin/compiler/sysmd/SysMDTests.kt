package compiler.sysmd

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.MembershipImport
import com.github.tukcps.sysmd.model.util.Unresolved
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.check.getUnresolvedElements
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMD
import util.testSession
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * Tests that focus on the SYNTAX implemented by the SysMD parser only.
 * Should basically only test if the parser throws syntax errors.
 * Semantics are tested in SysMDTests (w.r.t. simple propagation) and ConstraintNetTests (eval-up-down).
 */
class SysMDTests {

    /** Check syntax for Package declaration */
    @Test
    fun parsePackageTest() = testSession {
        loadSysMD("""Global hasA package test.""")
        assertNoIssues()
        global.resolve("test")
        Assertions.assertEquals(0, getUnresolvedElements().size)
    }

    /** Check syntax for declaration of a value feature */
    @Test
    fun parseValueTest() = testSession("ScalarValues", runlevel = Runlevel.MODEL) {
        loadSysMD("""
            Global hasA package hello.
            hello hasA package car. 
            hello::car hasA 
                feature p: ScalarValues::Real.
        """)
        assertNoIssues()
        assertNotNull(global.resolve("hello::car::p"))
    }

    /**
     * The SysMD compiler creates for non-existing owners packages.
     */
    @Test
    fun parseFeature()  = testSession {
        loadSysMD("a::b hasA feature x : Base::Anything.", Runlevel.NAMES_RESOLVED)
        assertNoIssues()
        val abx = global.resolve("a::b::x")
        assertNotNull(abx)
    }

    @Test
    fun importsSyntaxTest() = testSession(runlevel = Runlevel.NAMES_RESOLVED) {
        loadKerML("""
            private import space;
        """)
        assertNoIssues()

        val i = get().filterIsInstance<MembershipImport>().single()
        assertSame(global, i.owningRelatedElement)
        val space = assertIs<Unresolved>(i.importedElement).also {
            assertEquals("space", it.relativeName)
        }

        assertEquals(2, i.relatedElements.size)
        assertEquals(setOf(space, global), i.relatedElements.toSet())


        getUnresolvedElements().also {
            assertEquals(1, it.size, "Too many unresolved elements, got $it")
            assertSame(i.importedElement, it.single())
        }
    }

    /**
     * Lexical comments are just ignored.
     */
    @Test
    fun commentTest() = testSession(runlevel = Runlevel.NAMES_RESOLVED) {
        loadSysMD("""Global hasA feature x: Base::Anything. // comment""")
        assertNoIssues()
        Assertions.assertTrue(global.ownedElement.find { it.name == "x" } is Feature)
    }


    /**
     * Check the syntax of if - else statement in expressions.
     */
    @Test
    fun ifElseTestSysMlV2() = testSession("ScalarValues", runlevel = Runlevel.MODEL) {
        loadSysMD("""
            Global hasA feature x: ScalarValues::Boolean.
            Global hasA feature y: ScalarValues::Boolean = false or if x? true else false.
            Global hasA feature z: ScalarValues::Boolean = if x? true else false.
        """)
        assertNoIssues()
    }
}