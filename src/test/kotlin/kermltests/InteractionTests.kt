package kermltests

import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue


class InteractionTests {
    @Test fun simpleTest() = testSession("Occurrences") {
        loadKerML("""
            interaction i; 
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}