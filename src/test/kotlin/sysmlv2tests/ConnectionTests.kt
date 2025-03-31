package sysmlv2tests

import io.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Connector
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.sysml.ConnectionDefinition
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConnectionTests {
    /**
     * Simple undirected connection of three elements a, b, c
     */
    @Test
    fun testSyntax1() = testSession("Parts", "Connections") {
        loadSysMLv2("""
            part a; 
            part b; 
            part c;
            connect(a, b, c); 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * Directed connection of two elements from a to b
     */
    @Test
    fun testSyntax2() = testSession("Parts", "Connections") {
        loadSysMLv2("""
            part a; 
            part b;
            connect a to b; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.getOwnedElementOfType<ConnectionUsage>()
        assertTrue(c != null)
        assertEquals(1, c.from.size)
        assertEquals(1, c.from.size)
    }

    @Test
    fun testSyntax3() = testSession("Parts", "Connections") {
        loadSysMLv2("""
            part a;
            part b;
            connection c connect a to b; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<ConnectionUsage> ("c")
        assertTrue(c != null)
        assertEquals(1, c.from.size)
        assertEquals(1, c.to.size)
    }

    @Test
    fun testConnectThree() = testSession("Parts", "Connections") {
        loadSysMLv2("""
            part a;
            part b;
            part c;
            connection d connect (a, b, c); 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val d = global.resolve<ConnectionUsage> ("d")
        assertTrue(d != null)
        assertEquals(3, d.from.size)
    }

    @Test
    fun testConnectionDefinition() = testSession("Parts", "Connections") {
        loadSysMLv2("""
            connection def C; 
            part a;
            part b;
            connection c : C connect a to b;  
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<ConnectionDefinition> ("C")
        assertNotNull(c)
        val ci = global.resolve<ConnectionUsage> ("c")
        assertNotNull(ci)
    }

    @Test
    fun testConnectionDefinition2() = testSession("Parts", "Connections") {
        loadSysMLv2("""
            part a;
            part b;
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
    fun testInterfaceUsage() = testSession("Parts", "Connections", "Interfaces") {
        loadSysMLv2("""
            part a;
            part b;
            part c;
            interface d connect (a, b, c); 
        """.trimIndent())
        val c = global.resolve<ConnectionUsage> ("d")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertTrue(c != null)
        assertEquals(3, c.from.size)
    }

    @Test
    fun testInterfaceDefinition2() = testSession("Parts", "Connections", "Interfaces") {
        loadSysMLv2("""
            part a;
            part b;
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
    fun testRepeatedLoading() = testSession("Parts", "Connections") {
        loadSysMLv2("""
            package connection_example {
                part def A; 
                part def B; 
                part a: A; 
                part b: B; 
                connection def C1; // from A to B; 
                connection def C :> C1; 
                connection c : C connect a to b;  
            }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

        val a = global.resolve<PartUsage> ("connection_example::a")
        assertNotNull(a)
        val c = global.resolve<ConnectionUsage> ("connection_example::c")
        assertNotNull(c)

        loadSysMLv2("""
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


    /**
     * Check if Signals is correctly restored from the project.
     * If previous test runs, this is eventually the problem.
     */
    @Test
    fun testSignalsPkg() = testSession("Signals") {
        assertTrue(status.exceptions.isEmpty(), "Errors: ${status.exceptions}")
        initialize()
        val effectChain = global.resolveVar("Signals::EffectChain::inoutIsEqual") !!
        assertEquals(XBool.True, effectChain.boolSpecs.first())
    }

    @Test @Ignore
    fun testConnectEffectChainPropagation() = testSession("Signals", "Parts", "Ports") {
        loadSysMLv2("""       
            part def A {
                attribute x: ScalarValues::Real(3 .. 3); 
            }
            part def B { 
                attribute y: ScalarValues::Real(2 .. 4); 
            }
            part a: A; 
            part b: B; 
            connection c: Signals::EffectChain {
                end feature source: ScalarValues::Real references a::x; 
                end feature target: ScalarValues::Real references b::y; 
            }
        """)
        assertTrue(status.exceptions.isEmpty(), "Errors: ${status.exceptions}")
        val a = global.resolve<Feature>("a")
        val b = global.resolve<Feature>("b")
        val c = global.resolve<Connector>("c")
        assertNotNull(a)
        assertNotNull(b)
        assertNotNull(c)
        val source2 = global.resolve<Element>("c::source")
        assertNotNull(source2)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
        assertEquals(3.0, global.resolve<Feature>("a::x")!!.variable!!.min(),0.00001)
        assertEquals(3.0, global.resolve<Feature>("b::y")!!.variable!!.min(),0.00001)
        assertEquals(3.0, global.resolve<Feature>("b::y")!!.variable!!.max(),0.00001)
    }


    @Test
    fun testConnection() = testSession(  "Parts", "Connections") {
        loadSysMLv2("""
            package connection_example {
                part def A; 
                part def B; 
                part a: A; 
                part b: B; 
                connection c : C connect a to b;  
                connection def C :> C1; 
                connection def C1; // from A to B; 
                connection def C :> C1; 
                connection c : C connect a to b;  
            } 
        """)
        val c = global.resolve<ConnectionUsage>("connection_example::c")
        assertNotNull(c)
        assertEquals(3, c.ownedElement.size)
        assertTrue(c.specializes(repo.links))
        assertTrue(status.exceptions.isEmpty(), "Errors: ${status.exceptions}")
    }
}