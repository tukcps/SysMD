package sysmlv2tests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class OccurrenceTests {
    @Test
    fun testOccurrene1() = testSession("Occurrences"){
        loadSysMLv2("""
            occurrence p;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun testOccurrene2() = testSession("Occurrences"){
        loadSysMLv2("""
            event occurrence p;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}