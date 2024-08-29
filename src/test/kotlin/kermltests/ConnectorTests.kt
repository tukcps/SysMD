package kermltests

import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Connector
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.getRelationshipsFrom
import com.github.tukcps.sysmd.services.getRelationshipsTo
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConnectorTests {

    @Test
    fun assocTest1() = testSession {
        loadSysMD("""
            assoc test :> Links::Link; 
            feature a: Base::Anything; 
            feature b: Base::Anything; 
            connector test2: test from a to b; 
            """)
        val test = global.resolve<Association>("test")
        val b = global.resolve<Feature>("b")
        val a = global.resolve<Feature>("a")
        val rto = getRelationshipsTo(b!!, "*", test)
        val rfrom = getRelationshipsFrom(a!!, "*", test)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertTrue(rto.isNotEmpty())
        assertTrue(rfrom.isNotEmpty())
    }

    @Test
    fun assocTest2() = testSession {
        loadSysMD("""
            class a;
            class b;
            connector c from a to b;
        """)
        assertTrue(status.exceptions.isNotEmpty(), "Class as parameter of connector shall report error")
    }

    /**
     * An effect chain or other similar signal owns a constraint that requires
     * that source and target are equal.
     */
    @Test
    fun connectorEndsAccessibleInDef() = testSession {
        loadSysMD("""
            // Definition 
            package Signals {
                assoc EffectChain {
                    end feature source: ScalarValues::Real; 
                    end feature target: ScalarValues::Real; 
                    inv inoutIsEqual { source == target }; 
                }
            }
            
            // Usage
            feature a: ScalarValues::Real(1..4); 
            feature b: ScalarValues::Real(2..5);
            connector Signal: Signals::EffectChain {
                end feature source: ScalarValues::Real references a; // from a to b; 
                end feature target: ScalarValues::Real references b; 
            }
        """.trimIndent())
        propagate()
        val sa = global.resolve<Feature>("Signal::source")
        assertNotNull(sa)
        val sb = global.resolve<Feature>("Signal::target")
        assertNotNull(sb)
        assertTrue(status.exceptions.isEmpty(), "Errors: ${status.exceptions}")
        val effectChain = global.resolve<Feature>("Signals::EffectChain::inoutIsEqual")
        val b = global.resolve<Feature>("b")!!.variable!!
        val a = global.resolve<Feature>("a")!!.variable!!
        assertEquals(4.0, b.max(), 0.0000001 )
        assertEquals(2.0, a.min(), 0.0000001 )
        assertNotNull(effectChain)
    }

    /**
     * Check if Signals is correctly restored from the project.
     * If previous test runs, this is eventually the problem.
     */
    @Test
    fun testSignalsPkg() = testSession("Signals") {
        val effectChain = global.resolveVar("Signals::EffectChain::inoutIsEqual") !!
        assertEquals(XBool.True, effectChain.boolSpecs.first())
    }

    @Test
    fun testConnectEffectChainPropagation() = testSession("Signals", "Parts", "Ports") {
        loadSysMD("""       
            part def A {
                attribute x: ScalarValues::Real(3 .. 3); 
            }
            part def B { 
                attribute y: ScalarValues::Real(2 .. 4); 
            }
            part a: A; 
            part b: B; 
            connector c: Signals::EffectChain {
                end attribute source: ScalarValues::Real references a::x; 
                end attribute target: ScalarValues::Real references b::y; 
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
    fun testConnectFromToPropagation() = testSession("Signals", "Parts", "Ports") {
        loadSysMD("""       
            part def A {
                attribute x: ScalarValues::Real(3 .. 3); 
            }
            part def B { 
                attribute y: ScalarValues::Real(2 .. 4); 
            }
            part a: A; 
            part b: B; 
            connector c: Signals::EffectChain from a::x to b::y; 
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
        assertEquals(3.0, global.resolveVar("a::x")!!.min(),0.00001)
        assertEquals(3.0, global.resolveVar("b::y")!!.min(),0.00001)
        assertEquals(3.0, global.resolveVar("b::y")!!.max(),0.00001)
    }
}