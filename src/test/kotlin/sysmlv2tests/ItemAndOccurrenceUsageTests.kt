package sysmlv2tests

import com.github.tukcps.sysmd.model.sysml.*
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.resolve.resolve
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ItemAndOccurrenceUsageTests {


    /**
     * An item usage generates a feature of class "Items::Item".
     */
    @Test
    fun itemTest1() = testSession("Items") {
        loadSysMD("""
            item p; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val p = global.resolve<ItemUsage>("p")
        assertNotNull(p)
        // assertTrue(p.superclass?.ref?.qualifiedName == "Items::Item")
    }

    /**
     * A port usage generates a feature of class "Port".
     */
    @Test
    fun itemDefTestWithSpecialization() = testSession("Items") {
        loadSysMD("""
            item def p1; 
            item def p2 :> p1; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val p2 = global.resolve<ItemDefinition>("p2")
        assertNotNull(p2)
        assertTrue(p2.allSupertypes().first().qualifiedName == "p1")
    }

    /**
     * A port usage generates a feature of class "Parts::Part".
     */
    @Test
    fun occurrenceTest1() = testSession("Occurrences") {
        loadSysMD("""
            occurrence p; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val p = global.resolve<OccurrenceUsage>("p")
        assertNotNull(p)
        assertTrue(p.allSupertypes().first().qualifiedName == "Occurrences::Occurrence")
    }

    /**
     * A occurrence usage generates a feature of class "Port".
     */
    @Test
    fun occurrenceDefTestWithSpecialization() = testSession("Occurrences") {
        loadSysMD("""
            occurrence def p1; 
            occurrence def p2 :> p1; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val p2 = global.resolve<OccurrenceDefinition>("p2")
        assertNotNull(p2)
        assertTrue(p2.allSupertypes().first().qualifiedName == "p1")
    }

}