package parsertests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests that focus on the SYNTAX implemented by the SysMD parser only.
 * Should basically only test if the parser throws syntax errors.
 * Semantics are tested in SysMDtests (w.r.t. simple propagation) and Constraintnettests (eval-up-down).
 */
class SyntaxTests {


    /** Check syntax for Package declaration */
    @Test
    fun parsePackageTest() = testSession {
        loadSysMD(input = """Global hasA Package test.""".trimIndent(), catchExceptions = false)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(0, getUnownedElements().size)
    }


    /**
     * Check syntax for single, global classification (isA) of an element.
     * A bare classification template is added to the list of unowned elements.
     */
    @Test
    fun parseClassification() = testSession( initialize = false, catchExceptions = false, loadKerML = false) {
        loadSysMD(
            input = """
                class x :> Base::Anything;
            """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertNotNull(getUnownedElements().find { it.element.declaredName == "x" })
        assertEquals("Global", getUnownedElements().first().path)
    }

    /**
     * Check syntax for classification (isA) of an element in a definition list
     */
    @Test
    fun parseClassification2() = testSession(initialize = false, catchExceptions = false, loadKerML = false) {
        loadSysMD(
            input = """
                package pkg; 
                pkg defines class x.
            """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertNotNull(getUnownedElements().find { it.path?.contains("pkg") == true })
    }

    /** Short name is given in <> */
    @Test
    fun shortNameTest1() = testSession(catchExceptions = false, initialize = false, loadKerML = false) {
        loadSysMD("""class < abc >; """.trimIndent())
        val abc = getUnownedElements().first().element
        assertEquals("abc", abc.declaredShortName)
        assertEquals(null, abc.declaredName)
        assertEquals(0, status.exceptions.size, status.exceptions.toString() )
    }

    /** Short name is given in <> */
    @Test
    fun shortNameTest2() = testSession(catchExceptions = false, initialize = false, loadKerML = false) {
        loadSysMD(input = """
            class < abc > abcd isA Base::Anything; 
        """.trimIndent())
        val abc = getUnownedElements().first().element
        assertEquals("abc", abc.declaredShortName)
        assertEquals("abcd", abc.declaredName)
        assertEquals(0, status.exceptions.size, status.exceptions.toString() )
    }

    /** Check syntax for declaration of a value feature */
    @Test
    fun parseValueTest() = testSession(catchExceptions = false, initialize = false, loadKerML = false) {
        loadSysMD("""
            package ScalarValues { 
                datatype ScalarValue; datatype Integer :> ScalarValue; datatype Real :> ScalarValue; 
            }
            hello::car hasA feature p: ScalarValues::Real = Global::hello::world::x + 2.0.
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertTrue("Global::hello::car" in getUnownedElements().map { it.path })
    }

    /**
     * The parser creates Features and creates them as orphan features, without an owner and id.
     */
    @Test fun parseFeature()  = testSession(initialize = false, catchExceptions = false, loadKerML = false) {
        loadSysMD("a::b hasA feature x: Base::Anything.")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertTrue(getUnownedElements().find {  it.path!!.endsWith("a::b")}?.element is Feature)
    }

    /**
     * The parser creates Features and creates them as orphan features, without owner and id.
     */
    @Test
    fun parseExpression()  = testSession(catchExceptions = false, initialize = false, loadKerML = false) {
        loadSysMD("a::b hasA feature x: Real.")
        assertTrue("Global::a::b" in getUnownedElements().map { it.path })
    }

    @Test
    fun importsSyntaxTest() = testSession(initialize = false, catchExceptions = false, loadKerML = false) {
        loadSysMD("""
                test::e hasA import space.
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals("space", (getUnownedElements().first().element as Import).importedNamespace.str  )
    }

    /**
     * Lexical comments are just ignored.
     */
    @Test
    fun commentTest() = testSession(initialize = false, catchExceptions = false, loadKerML = false) {
        loadSysMD("""Global hasA feature x: Base::Anything. // comment""")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertTrue(getUnownedElements().find { it.path == "Global"}?.element is Feature)
    }



    /**
     * Check the syntax of if - else statement in expressions.
     */
    @Test
    fun ifElseTestSysMlV2() = testSession(catchExceptions = false, initialize = false, loadKerML = false) {
        loadSysMD("""
            attribute x: ScalarValues::Boolean; 
            attribute y: ScalarValues::Boolean = false or if x? true else false;
            attribute z: ScalarValues::Boolean = if x? true else false;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test
    fun realRangeWithUnitTest() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; datatype Real :> ScalarValue; }
            attribute i: ScalarValues::Integer = 2;
            attribute r: ScalarValues::Real [m] = [1.0 .. 2.0] m;
        """.trimIndent()
        )
        val r = global.resolve<Feature>("r")!!
        val i = global.resolve<Feature>("i")!!
        assertEquals("m", r.unitConstraint)
        assertEquals("2", i.expression?.trim())
        assertEquals("ScalarValues::Real", r.type.first().str)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }


    @Test
    fun parseUnitTest() = testSession {
        loadSysMD("attribute x: ScalarValues::Real [km/s] = 10.0 [m/s].".trimIndent())
        propagate()
        val test= global.resolveVar("x")!!.vectorQuantity.unit
        assertEquals("m / s", test.toString() )
        assertEquals(0.01,
            global.resolveVar("x")!!.vectorQuantity.valuesIn("km/s")[0].asAadd().getRange().min, 0.000001)
    }
}
