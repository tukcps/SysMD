package kermltests

import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FunctionTests {

    @Test
    fun testSyntax() = testSession {
        loadSysMD("""
        function f {
            in feature a: ScalarValues::Real; 
            out feature x: ScalarValues::Real = a*a; 
        }
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val f = global.resolve<Element>("f")
        val a = global.resolve<Feature>("f::a")
        assertNotNull(f)
        assertNotNull(a)
    }
}