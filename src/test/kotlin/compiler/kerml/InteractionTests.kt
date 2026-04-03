package compiler.kerml

import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test

class InteractionTests {
    @Test
    fun simpleTest() = testSession("Occurrences") {
        loadKerML(
            """
            interaction i; 
        """
        )
        assertNoIssues()
    }
}