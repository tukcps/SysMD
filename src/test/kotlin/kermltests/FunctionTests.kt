package kermltests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
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
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val f = global.resolve<Element>("f")
        val a = global.resolve<Feature>("f::a")
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