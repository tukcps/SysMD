package compiler

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.check.getUnresolvedElements
import com.github.tukcps.sysmd.services.initialize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import util.assertNoIssues
import util.mockup.loadSysMD
import util.testSession

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
        assertEquals(0, getUnresolvedElements().size)
    }

    /** Check syntax for declaration of a value feature */
    @Test
    fun parseValueTest() = testSession("ScalarValues") {
        loadSysMD("""
            Global hasA package hello.
            hello hasA package car. 
            hello::car hasA 
                feature p: ScalarValues::Real.
        """)
        initialize()
        assertNoIssues()
        assertNotNull(global.resolve("hello::car::p"))
        assertEquals(2, solver.getVariables().size)
    }

    /**
     * The SysMD compiler creates for non-existing owners packages.
     */
    @Test fun parseFeature()  = testSession {
        loadSysMD("a::b hasA feature x : Base::Anything.")
        assertNoIssues()
        val abx = global.resolve("a::b::x")
        assertNotNull(abx)
    }

    @Test
    fun importsSyntaxTest() = testSession(initialize = false) {
        loadSysMD("""
            Global hasA private import space.
        """)
        assertNoIssues()
        assertEquals("space", getUnresolvedElements().first().relativeName )
    }

    /**
     * Lexical comments are just ignored.
     */
    @Test
    fun commentTest() = testSession(initialize = false) {
        loadSysMD("""Global hasA feature x: Base::Anything. // comment""")
        assertNoIssues()
        assertTrue(global.ownedElement.find { it.name == "x"} is Feature)
    }


    /**
     * Check the syntax of if - else statement in expressions.
     */
    @Test
    fun ifElseTestSysMlV2() = testSession("ScalarValues") {
        loadSysMD("""
                Global hasA feature x: ScalarValues::Boolean.
                Global hasA feature y: ScalarValues::Boolean = false or if x? true else false.
                Global hasA feature z: ScalarValues::Boolean = if x? true else false.
            """)
        assertNoIssues()
        assertEquals(4, solver.getVariables().size, solver.getVariables().joinToString("\n"))
    }
}
