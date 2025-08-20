package compiler.sysml.examples

import com.github.tukcps.sysmd.model.sysml.OccurrenceDefinition
import com.github.tukcps.sysmd.model.sysml.OccurrenceUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadSysMLv2
import org.junit.jupiter.api.Disabled
import util.assertNoIssues
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OccurrenceTests {

    /**
     * This test checks the definition of occurrences in SysML v2.
     * It verifies that occurrences can be defined using the `occurrence def` keyword and that no exceptions are raised.
     * Refer to Section: 7.9 Occurrences
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testOccurrenceDefinition() = testSession("Occurrences") {
        loadSysMLv2("""
        occurrence def OccurrenceDef1;
        occurrence def OccurrenceDef2 {
            /* members */
        }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val occurrenceDef1 = global.resolve<OccurrenceDefinition>("OccurrenceDef1")
        assertNotNull(occurrenceDef1)

        val occurrenceDef2 = global.resolve<OccurrenceDefinition>("OccurrenceDef2")
        assertNotNull(occurrenceDef2)
    }

    /**
     * This test checks the usage of occurrences in SysML v2.
     * It ensures that occurrences can be instantiated and used after being defined, with no exceptions raised.
     * Refer to Section: 7.9 Occurrences
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testOccurrenceUsage() = testSession("Occurrences") {
        loadSysMLv2("""
            occurrence def OccurrenceDef1;
            occurrence occurrence1 : OccurrenceDef1;
            occurrence occurrence2 : OccurrenceDef1 {
                /* members */
            }
        """)
        assertNoIssues()

        val occurrenceDef1 = global.resolve<OccurrenceDefinition>("OccurrenceDef1")
        assertNotNull(occurrenceDef1)

        val occurrence1 = global.resolve<OccurrenceUsage>("occurrence1")
        assertNotNull(occurrence1)

        val occurrence2 = global.resolve<OccurrenceUsage>("occurrence2")
        assertNotNull(occurrence2)
    }

    /**
     * This test checks the definition of individual occurrences in SysML v2.
     * It verifies that individual occurrences can be defined using the `individual def` keyword.
     * Refer to Section: 7.9 Occurrences
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Test
    fun testIndividualOccurrenceDefinition() = testSession("Occurrences") {
        loadSysMLv2("""
            occurrence def OccurrenceDef1;
            individual def 'OccurrenceDef1-1' :> OccurrenceDef1;
        """)
        assertNoIssues()

        val occurrenceDef1 = global.resolve<OccurrenceDefinition>("OccurrenceDef1")
        assertNotNull(occurrenceDef1)

        val occurrenceDef11 = global.resolve<OccurrenceDefinition>("OccurrenceDef1-1")
        assertNotNull(occurrenceDef11)
    }

    /**
     * This test checks the creation of a timeslice for an occurrence in SysML v2.
     * It verifies that a timeslice can be created to represent a point in time for a specific occurrence.
     * Refer to Section: 7.9 Occurrences
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Ignore
    @Test
    fun testTimeslice() = testSession("Occurrences") {
        loadSysMLv2("""
            occurrence def OccurrenceDef1;
            timeslice timeslice1 : OccurrenceDef1;
        """)
        assertNoIssues()
    }

    /**
     * This test checks the creation of a snapshot for an occurrence in SysML v2.
     * It verifies that a snapshot can be taken to capture the state of an occurrence at a specific time.
     * Refer to Section: 7.9 Occurrences
     * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
     */
    @Disabled
    @Test
    fun testSnapshot() = testSession("Occurrences") {
        loadSysMLv2("""
            occurrence def OccurrenceDef1;
            snapshot snapshot1 : OccurrenceDef1;
        """)
        assertNoIssues()
    }
}
