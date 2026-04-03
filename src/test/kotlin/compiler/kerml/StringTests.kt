package compiler.kerml

import com.github.tukcps.sysmd.model.kerml.Feature
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class StringTests {

    /**
     * Syntactic variants should be parsed correctly.
     */
    @Test
    fun stringSyntax() = testSession("ScalarValues") {
        loadKerML("""feature label1: ScalarValues::String = "string value1";""")
        solver.propagate()
        assertNoIssues()
        val label1 = global.resolve("label1")?.member<Feature>()
        assertEquals( "\"string value1\"", label1?.expression)
    }
}