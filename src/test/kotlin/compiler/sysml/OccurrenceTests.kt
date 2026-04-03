package sysmlv2tests

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test

class OccurrenceTests {
    @Test
    fun testOccurrene1() = testSession("Occurrences"){
        loadSysMLv2("""
            occurrence p;
        """)
        assertNoIssues()
    }

    @Test
    fun testOccurrene2() = testSession("Occurrences"){
        loadSysMLv2("""
            event occurrence p;
        """)
        assertNoIssues()
    }
}