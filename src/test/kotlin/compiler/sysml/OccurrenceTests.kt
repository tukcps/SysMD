package compiler.sysml

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class OccurrenceTests {
    @Test
    fun testOccurrence1() = testSession("Occurrences"){
        loadSysMLv2("occurrence p;")
        assertNoIssues()
        val p = global.resolve("p")
        assertNotNull(p)
    }

    @Test
    fun testOccurrence2() = testSession("Occurrences"){
        loadSysMLv2("event occurrence p;")
        assertNoIssues()
        val p = global.resolve("p")
        assertNotNull(p)
    }
}