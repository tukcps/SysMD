package kermltests

import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.XBool
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class InvariantTests {
    @Test
    fun testSyntax() = testSession("ScalarValues") {
        loadKerML("""
            feature e : ScalarValues::Boolean; 
            inv a { e }
        """)
        solver.propagate()
        assertNoIssues()
        val e = global.resolveVar("e")!!
        val a = global.resolveVar("a")
        assertNotNull(e)
        assertNotNull(a)
        assertEquals(XBool.True, e.vectorQuantity.value as XBool)
    }


    @Test
    fun testSyntaxNoName() = testSession("ScalarValues") {
        loadKerML("""
            feature e : ScalarValues::Boolean; 
            inv { e }
        """)
        solver.propagate()
        assertNoIssues()
        val e = global.resolveVar("e")
        assertNotNull(e)
        val a = global.getOwnedElementOfType<Invariant>()
        assertNotNull(a)
        assertEquals(e.bool(), XBool.True)
    }
}
