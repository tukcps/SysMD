package kermltests

import io.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InvariantTests {
    @Test
    fun testSyntax() = testSession("ScalarValues") {
        loadKerML("""
            feature e : ScalarValues::Boolean; 
            inv a { e }
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val e = global.resolve<Feature>("e")!!.variable
        val a = global.resolve<Invariant>("a")
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
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val e = global.resolve<Feature>("e")
        assertNotNull(e)
        val a = global.getOwnedElementOfType<Invariant>()
        assertNotNull(a)
        assertTrue(e.variable?.vectorQuantity?.value == XBool.True)
    }
}
