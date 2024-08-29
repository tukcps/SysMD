package sysmlv2tests

import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.sysml.ConnectionDefinition
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConnectionTests {
    /**
     * Simple undirected connection of three elements a, b, c
     */
    @Test
    fun testSyntax1() = testSession("Connections") {
        loadSysMD("""
            feature a; 
            feature b; 
            feature c;
            connect(a, b, c); 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * Directed connection of two elements from a to b
     */
    @Test
    fun testSyntax2() = testSession("Connections") {
        loadSysMD("""
            feature a; 
            feature b;
            connect a to b; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.getOwnedElementOfType<ConnectionUsage>()
        assertTrue(c != null)
        assertEquals(1, c.from.size)
        assertEquals(1, c.from.size)
    }

    @Test
    fun testSyntax3() = testSession("Connections") {
        loadSysMD("""
            feature a;
            feature b;
            connection c connect a to b; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<ConnectionUsage> ("c")
        assertTrue(c != null)
        assertEquals(1, c.from.size)
        assertEquals(1, c.to.size)
    }

    @Test
    fun testConnectThree() = testSession("Connections") {
        loadSysMD("""
            feature a;
            feature b;
            feature c;
            connection d connect (a, b, c); 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val d = global.resolve<ConnectionUsage> ("d")
        assertTrue(d != null)
        assertEquals(3, d.from.size)
    }

    @Test
    fun testConnectionDefinition() = testSession("Connections") {
        loadSysMD("""
            connection def C; 
            feature a;
            feature b;
            connection c : C connect a to b;  
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<ConnectionDefinition> ("C")
        assertNotNull(c)
        val ci = global.resolve<ConnectionUsage> ("c")
        assertNotNull(ci)
    }

    @Test
    fun testConnectionDefinition2() = testSession("Connections") {
        loadSysMD("""
            feature a;
            feature b;
            connection def C1; 
            connection def C :> C1; 
            connection c : C connect a to b;  
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<ConnectionDefinition> ("C")
        assertNotNull(c)
        val ci = global.resolve<ConnectionUsage> ("c")
        assertNotNull(ci)
    }

    @Test
    fun testInterfaceUsage() = testSession("Connections", "Interfaces") {
        loadSysMD("""
            feature a;
            feature b;
            feature c;
            interface d connect (a, b, c); 
        """.trimIndent())
        val c = global.resolve<ConnectionUsage> ("d")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertTrue(c != null)
        assertEquals(3, c.from.size)
    }

    @Test
    fun testInterfaceDefinition2() = testSession("Connections", "Interfaces") {
        loadSysMD("""
            feature a;
            feature b;
            interface def C1; 
            interface def C :> C1; 
            interface c : C connect a to b;  
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<ConnectionDefinition> ("C")
        assertNotNull(c)
        val ci = global.resolve<ConnectionUsage> ("c")
        assertNotNull(ci)
    }

    /**
     * Repeated loading must be able to deal with referenced features
     */
    @Test
    fun testRepeatedLoading() = testSession("Parts", "Connections", "Interfaces") {
        loadSysMD("""
            package connection_example {
                part def A; 
                part def B; 
                part a: A; 
                part b: B; 
                connection def C1; // from A to B; 
                connection def C :> C1; 
                connection c : C connect a to b;  
            }
        """.trimIndent())

        val a = global.resolve<PartUsage> ("connection_example::a")
        val c = global.resolve<ConnectionUsage> ("connection_example::c")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

        loadSysMD("""
            package connection_example {
                part def A; 
                part def B; 
                part a: A; 
                part b: B; 
                connection def C1; // from A to B; 
                connection def C :> C1; 
                connection c : C connect a to b;  
            }
        """.trimIndent())

        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}