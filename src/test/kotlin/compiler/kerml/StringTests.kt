package compiler.kerml

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StringTests {

    /**
     * Syntactic variants should be parsed correctly.
     */
    @Test
    fun stringSyntax() = testSession("ScalarValues") {
        loadKerML("""feature label1: ScalarValues::String = "string value1";""")
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val label1 = global.resolve<Feature>("label1")!!.variable!!
        assertEquals(label1.dependency, "\"string value1\"")
    }
}