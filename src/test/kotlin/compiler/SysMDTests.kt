package compiler

import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.check.getUnresolvedElements
import com.github.tukcps.sysmd.services.resolve.resolve
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        global.resolve<Element>("test")
        assertEquals(0, getUnresolvedElements().size)
    }

    /** Check syntax for declaration of a value feature */
    @Test
    fun parseValueTest() = testSession(initialize = false) {
        loadSysMD("""
            Global hasA package hello.
            hello hasA package car. 
            hello::car hasA 
                feature p: ScalarValues::Real = Global::hello::world::x + 2.0.
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertTrue(global.resolve<Feature>("hello::car::p") != null)

        assertEquals(4, astNodes.size)
        assertEquals(1, astNodes.count { it.value is AstRoot })
    }

    /**
     * The SysMD compiler creates for non-existing owners packages.
     */
    @Test fun parseFeature()  = testSession {
        loadSysMD("a::b hasA feature x : Base::Anything.")
        assertNoIssues()
        val abx = global.resolve<Feature>("a::b::x")
        assertNotNull(abx)
    }

    @Test
    fun importsSyntaxTest() = testSession(initialize = false) {
        loadSysMD("""
            Global hasA private import space.
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals("space", getUnresolvedElements().first().relativeName )
    }

    /**
     * Lexical comments are just ignored.
     */
    @Test
    fun commentTest() = testSession(initialize = false) {
        loadSysMD("""Global hasA feature x: Base::Anything. // comment""")
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertTrue(global.ownedElement.find { it.name == "x"} is Feature)

        assertEquals(0, astNodes.size)
    }


    /**
     * Check the syntax of if - else statement in expressions.
     */
    @Test
    fun ifElseTestSysMlV2() = testSession(initialize = false) {
        loadSysMD("""
                Global hasA feature x: ScalarValues::Boolean.
                Global hasA feature y: ScalarValues::Boolean = false or if x? true else false.
                Global hasA feature z: ScalarValues::Boolean = if x? true else false.
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        assertEquals(setOf("y", "z"), astNodes.mapNotNull { (it.value as? AstRoot)?.feature?.name }.toSet())
        assertEquals(2 + 2 + 2*4, astNodes.size)
    }
}
