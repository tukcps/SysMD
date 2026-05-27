package compiler.sysml

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test

class OccurrenceTests {
    @Test
    fun testOccurrence1() = testSession("Occurrences"){
        loadSysMLv2("""
            occurrence p;
        """)
        assertNoIssues()
    }

    @Test
    fun testOccurrence2() = testSession("Occurrences"){
        loadSysMLv2("""
            event occurrence p;
        """)
        assertNoIssues()
    }
}