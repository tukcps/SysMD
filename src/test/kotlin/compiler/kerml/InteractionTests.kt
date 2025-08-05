package compiler.kerml

import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class InteractionTests {
    @Test
    fun simpleTest() = testSession("Occurrences") {
        loadKerML(
            """
            interaction i; 
        """
        )
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}