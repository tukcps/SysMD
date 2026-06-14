package compiler.sysml

import com.github.tukcps.sysmd.model.kerml.Connector
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.sysml.ConnectionDefinition
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.XBool
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.*

class ConnectionTests {
    /**
     * Simple undirected connection of three elements a, b, c
     */
    @Test
    fun connectionTestMultipleTargets() = testSession("Parts", "Connections") {
        loadSysMLv2(
            """
            part a; 
            part b; 
            part c;
            connect(a, b, c); 
        """
        )
        val con = global.getOwnedElementOfType<ConnectionUsage>()
        assertNotNull(con)
        assertNoIssues()
    }

    /**
     * Directed connection of two elements from a to b
     */
    @Test
    fun testSyntax2() = testSession("Parts", "Connections") {
        loadSysMLv2(
            """
            part a; 
            part b;
            connect a to b; 
        """
        )
        assertNoIssues()
        val c = global.getOwnedElementOfType<ConnectionUsage>()
        assertTrue(c != null)
        assertEquals(1, c.from.size)
        assertEquals(1, c.from.size)
    }

    @Test
    fun testSyntax3() = testSession("Parts", "Connections") {
        loadSysMLv2(
            """
            part a;
            part b;
            connection c connect a to b; 
        """
        )
        assertNoIssues()
        val c = global.resolve("c")?.member<ConnectionUsage>()
        assertTrue(c != null)
        assertEquals(1, c.from.size)
        assertEquals(1, c.to.size)
    }

    @Test
    fun testConnectThree() = testSession("Parts", "Connections") {
        loadSysMLv2(
            """
            part a;
            part b;
            part c;
            connection d connect (a, b, c); 
        """
        )
        assertNoIssues()
        val d = global.resolve("d")?.member<ConnectionUsage>()
        assertTrue(d != null)
        assertEquals(3, d.to.size)
    }

    @Test
    fun testConnectionDefinition() = testSession("Parts", "Connections") {
        loadSysMLv2(
            """
            connection def C; 
            part a;
            part b;
            connection c : C connect a to b;  
        """
        )
        assertNoIssues()
        val c = global.resolve("C")?.member<ConnectionDefinition>()
        assertNotNull(c)
        val ci = global.resolve("c")?.member<ConnectionUsage>()
        assertNotNull(ci)
    }

    @Test
    fun testConnectionDefinition2() = testSession("Parts", "Connections") {
        loadSysMLv2(
            """
            part a;
            part b;
            connection def C1; 
            connection def C :> C1; 
            connection c : C connect a to b;  
        """
        )
        assertNoIssues()
        val c = global.resolve("C")?.member<ConnectionDefinition>()
        assertNotNull(c)
        val ci = global.resolve("c")?.member<ConnectionUsage>()
        assertNotNull(ci)
    }

    @Test
    fun testInterfaceUsage() = testSession("Parts", "Interfaces") {
        loadSysMLv2(
            """
            part a;
            part b;
            part c;
            interface d connect (a, b, c); 
        """
        )
        val c = global.resolve("d")?.member<ConnectionUsage>()
        assertNoIssues()
        assertTrue(c != null)
        assertEquals(3, c.to.size)
    }

    @Test
    fun testInterfaceDefinition2() = testSession("Parts", "Connections", "Interfaces") {
        loadSysMLv2(
            """
            part a;
            part b;
            interface def C1; 
            interface def C :> C1; 
            interface c : C connect a to b;  
        """
        )
        assertNoIssues()
        val c = global.resolve("C")?.member<ConnectionDefinition>()
        assertNotNull(c)
        val ci = global.resolve("c")?.member<ConnectionUsage>()
        assertNotNull(ci)
    }

    /**
     * Repeated loading must be able to deal with referenced features
     */
    @Test
    fun testRepeatedLoading() = testSession("Parts", "Connections") {
        loadSysMLv2(
            """
            package connection_example {
                part def A; 
                part def B; 
                part a: A; 
                part b: B; 
                connection def C1; // from A to B; 
                connection def C :> C1; 
                connection c : C connect a to b;  
            }
        """
        )
        assertNoIssues()

        val a = global.resolve("connection_example::a")?.member<PartUsage>()
        assertNotNull(a)
        val c = global.resolve("connection_example::c")?.member<ConnectionUsage>()
        assertNotNull(c)

        loadSysMLv2(
            """
            package connection_example {
                part def A; 
                part def B; 
                part a: A; 
                part b: B; 
                connection def C1; // from A to B; 
                connection def C :> C1; 
                connection c : C connect a to b;  
            }
        """
        )
        assertNoIssues()
    }


    /**
     * Check if Signals is correctly restored from the project.
     * If previous test runs, this is eventually the problem.
     */
    @Test
    fun testSignalsPkg() = testSession("Signals") {
        assertTrue(status.issues.isEmpty(), "Errors: ${status.issues}")
        initialize(Runlevel.ALL)
        val effectChain = global.resolveVar("Signals::EffectChain::inoutIsEqual")!!
        assertEquals(XBool.True, effectChain.boolSpecs.first())
    }

    @Test
    @Ignore
    fun testConnectEffectChainPropagation() = testSession("Signals", "Parts", "Ports", "Ranges") {
        loadSysMLv2(
            """       
            part def A {
                attribute x: Ranges::RealInRange {:>> range = "3 .. 3";}
            }
            part def B { 
                attribute y: Ranges::RealInRange {:>> range = "2 .. 4";}
            }
            part a: A; 
            part b: B; 
            connection c: Signals::EffectChain {
                end feature source: ScalarValues::Real references a::x; 
                end feature target: ScalarValues::Real references b::y; 
            }
        """
        )
        assertNoIssues()
        val a = global.resolve("a")?.member<Feature>()
        val b = global.resolve("b")?.member<Feature>()
        val c = global.resolve("c")?.member<Connector>()
        assertNotNull(a)
        assertNotNull(b)
        assertNotNull(c)
        val source2 = global.resolve("c::source")?.memberElement
        assertNotNull(source2)
        solver.propagate()
        assertNoIssues()
        assertEquals(3.0, global.resolveVar("a::x")!!.min(), 0.00001)
        assertEquals(3.0, global.resolveVar("b::y")!!.min(), 0.00001)
        assertEquals(3.0, global.resolveVar("b::y")!!.max(), 0.00001)
    }
}