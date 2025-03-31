package parsertests

import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.mockup.loadSysMD
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import util.testSession

/**
 * Tests that focus on the SYNTAX implemented by the SysMD parser only.
 * Should basically only test if the parser throws syntax errors.
 * Semantics are tested in SysMDTests (w.r.t. simple propagation) and ConstraintNetTests (eval-up-down).
 */
class SyntaxTests {

    /** Check syntax for Package declaration */
    @Test
    fun parsePackageTest() = testSession {
        loadSysMD("""Global hasA package test.""")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(0, getUnownedElements().size)
        assertEquals(0, astNodes.size)
    }

    /**
     * Check syntax for single, global classification (isA) of an element.
     * A bare classification template is added to the list of unowned elements.
     */
    @Test
    fun parseClassification() = testSession( initialize = false) {
        loadKerML(
            input = """
                class x :> Base::Anything;
            """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertNotNull(getUnownedElements().find { it.element.declaredName == "x" })
        assertEquals(global, getUnownedElements().first().startOfPath)
        assertEquals(0, astNodes.size)
    }


    /** Short name is given in <> */
    @Test
    fun shortNameTest1() = testSession(initialize = false) {
        loadKerML("""class < abc >; """.trimIndent())
        val abc = getUnownedElements().first().element
        assertEquals("abc", abc.declaredShortName)
        assertEquals(null, abc.declaredName)
        assertEquals(0, status.exceptions.size, status.exceptions.toString() )
        assertEquals(0, astNodes.size)
    }

    /** Short name is given in <> */
    @Test
    fun shortNameTest2() = testSession(initialize = false) {
        loadKerML("class < shortName > longName;")
        val abc = getUnownedElements().first().element
        assertEquals("shortName", abc.declaredShortName)
        assertEquals("longName", abc.declaredName)
        assertEquals(0, status.exceptions.size, status.exceptions.toString() )
        assertEquals(0, astNodes.size)
    }

    /** Check syntax for declaration of a value feature */
    @Test
    fun parseValueTest() = testSession(initialize = false) {
        loadSysMD("""
            hello::car hasA 
                feature p: ScalarValues::Real = Global::hello::world::x + 2.0.
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertTrue("hello::car" in getUnownedElements().map { it.path })

        assertEquals(4, astNodes.size)
        assertEquals(1, astNodes.count { it.value is AstRoot })
    }

    /**
     * The parser creates Features and creates them as orphan features, without an owner and id.
     */
    @Test fun parseFeature()  = testSession(initialize = false) {
        loadSysMD("a::b hasA feature x : Base::Anything.")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertTrue(getUnownedElements().find { it.path?.endsWith("a::b") == true }?.element is Feature)
        assertEquals(0, astNodes.size)
    }

    /**
     * The parser creates Features and creates them as orphan features, without an owner and id.
     */
    @Test
    fun parseExpression()  = testSession(initialize = false) {
        loadSysMD("a::b hasA feature x: Real.")
        assertTrue("a::b" in getUnownedElements().map { it.path })
        assertEquals(0, astNodes.size)
    }

    @Test
    fun importsSyntaxTest() = testSession(initialize = false) {
        loadSysMD("""
                test::e hasA private import space.
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals("space", (getUnownedElements().first().element as Import).importedNamespace.str  )

        assertEquals(0, astNodes.size)
    }

    /**
     * Lexical comments are just ignored.
     */
    @Test
    fun commentTest() = testSession(initialize = false) {
        loadSysMD("""Global hasA feature x: Base::Anything. // comment""")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertTrue(getUnownedElements().find { it.startOfPath == global}?.element is Feature)

        assertEquals(0, astNodes.size)
    }



    /**
     * Check the syntax of if - else statement in expressions.
     */
    @Test
    fun ifElseTestSysMlV2() = testSession(initialize = false) {
        loadKerML("""
                feature x: ScalarValues::Boolean; 
                feature y: ScalarValues::Boolean = false or if x? true else false;
                feature z: ScalarValues::Boolean = if x? true else false;
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

        assertEquals(setOf("y", "z"), astNodes.mapNotNull { (it.value as? AstRoot)?.feature?.name }.toSet())
        assertEquals(2 + 2 + 2*4, astNodes.size)
    }

    @Test
    fun realRangeWithUnitTest() = testSession("Base", "SI") {
        loadKerML("""
            feature i: ScalarValues::Integer = 2;
            feature r: SI::Length [m] = oneOf(1.0 .. 2.0 m);
        """)
        val r = global.resolve<Feature>("r")!!
        val i = global.resolve<Feature>("i")!!
        assertEquals("m", r.unitConstraint)
        assertEquals("2", i.expression?.trim())
        assertEquals("SI::Length", r.type.first().str)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

        assertEquals(setOf("i", "r"), astNodes.mapNotNull { (it.value as? AstRoot)?.feature?.name }.toSet())
        assertEquals(2 + 2, astNodes.size)
    }
}
