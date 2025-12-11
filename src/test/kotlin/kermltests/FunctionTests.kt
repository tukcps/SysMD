package kermltests

import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FunctionTests {

    @Test
    fun testSyntax() = testSession("ScalarValues") {
        loadKerML("""
            function f {
                in a: ScalarValues::Real; 
                out feature x: ScalarValues::Real = a*a; 
            }
        """)
        solver.propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val f = global.resolve("f")?.memberElement
        val a = global.resolve("f::a")?.memberElement
        assertNotNull(f)
        assertNotNull(a)
    }

    @Test
    fun testUndefinedFunction() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Real;
            feature f: ScalarValues::Real = undefined(x); 
        """.trimIndent())
        assertTrue(status.issues.isNotEmpty(), "An unknown function should be reported as error.")
    }
}