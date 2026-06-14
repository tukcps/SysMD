package compiler.sysml

import com.github.tukcps.sysmd.model.sysml.ItemDefinition
import com.github.tukcps.sysmd.model.sysml.ItemUsage
import com.github.tukcps.sysmd.model.sysml.OccurrenceDefinition
import com.github.tukcps.sysmd.model.sysml.OccurrenceUsage
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ItemAndOccurrenceUsageTests {


    /**
     * An item usage generates a feature of class "Items::Item".
     */
    @Test
    fun itemTest1() = testSession("Items") {
        loadSysMLv2(
            """
            item p; 
        """
        )
        assertNoIssues()
        val p = global.resolve("p")?.memberElement as ItemUsage?
        assertNotNull(p)
        // assertTrue(p.superclass?.ref?.qualifiedName == "Items::Item")
    }

    /**
     * A port usage generates a feature of class "Port".
     */
    @Test
    fun itemDefTestWithSpecialization() = testSession("Items") {
        loadSysMLv2(
            """
            item def p1; 
            item def p2 :> p1; 
        """
        )
        assertNoIssues()
        val p2 = global.resolve("p2")?.memberElement
        assertNotNull(p2 as? ItemDefinition)
        assertEquals(p2.allSupertypes().first().qualifiedName, "p1")
    }

    /**
     * A port usage generates a feature of class "Parts::Part".
     */
    @Test
    fun occurrenceTest1() = testSession("Occurrences") {
        loadSysMLv2(
            """
            occurrence p; 
        """
        )
        assertNoIssues()
        val p = global.resolve("p")?.memberElement
        assertTrue(p is OccurrenceUsage)
        assertEquals("Occurrences::Occurrence", p.allSupertypes().first().qualifiedName)
    }

    /**
     * An occurrence usage generates a feature of class "Port".
     */
    @Test
    fun occurrenceDefTestWithSpecialization() = testSession("Occurrences") {
        loadSysMLv2(
            """
            occurrence def p1; 
            occurrence def p2 :> p1; 
        """
        )
        assertNoIssues()
        val p2 = global.resolve("p2")?.memberElement
        assertNotNull(p2 as? OccurrenceDefinition)
        assertEquals(p2.allSupertypes().first().qualifiedName, "p1")
    }
}