package kermltests

import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InvariantTests {
    @Test
    fun testSyntax() = testSession {
        loadSysMD("""
            feature e : ScalarValues::Boolean; 
            inv a { e }
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val e = global.resolve<Feature>("e")!!.variable
        val a = global.resolve<Invariant>("a")
        assertNotNull(e)
        assertNotNull(a)
        assertTrue(e.vectorQuantity.value == XBool.True)
    }


    @Test @Disabled
    // Issue: Invariant without names not yet supported in SysML implementation
    fun testSyntaxNoName() = testSession {
        loadSysMD("""
            feature e : ScalarValues::Boolean; 
            inv { e }
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val e = global.resolve<Feature>("e")!!.variable
        val a = global.resolve<Invariant>("a")
        assertNotNull(e)
        assertNotNull(a)
        assertTrue(e.vectorQuantity.value == XBool.True)
    }
}
