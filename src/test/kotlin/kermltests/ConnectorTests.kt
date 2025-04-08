package kermltests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Connector
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.getRelationshipsFrom
import com.github.tukcps.sysmd.services.getRelationshipsTo
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConnectorTests {

    @Test
    fun connectorCreatedTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a; 
            feature b; 
            connector c from a to b;
        """)
        // assertTrue(status.reports.isEmpty(), status.reports.toString())
        val c = global.resolve<Connector>("c")
    }

    @Test
    fun connectorWithAssocTest() = testSession("Links") {
        loadKerML("""
            assoc test :> Links::Link; 
            feature a; 
            feature b; 
            connector test2: test from a to b; 
        """)
        val test = global.resolve<Association>("test")
        val b = global.resolve<Feature>("b")
        val a = global.resolve<Feature>("a")
        val rto = getRelationshipsTo(b!!, "*", test)
        val rFrom = getRelationshipsFrom(a!!, "*", test)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertTrue(rto.isNotEmpty())
        assertTrue(rFrom.isNotEmpty())
    }

    @Test
    fun assocTest2() = testSession("Occurrences", "Links") {
        loadKerML("""
            class a;
            class b;
            connector c from a to b;
        """)
        assertTrue(status.issues.isNotEmpty(), "Class as parameter of connector shall report error")
        val c = global.resolve<Connector>("c")
        assertNotNull(c)
    }

    /**
     * An effect chain or other similar signal owns a constraint that requires
     * that source and target are equal.
     */
    @Test
    fun connectorEndsAccessibleInDef() = testSession("Occurrences") {
        loadKerML("""
            package Signals {
                assoc EffectChain {
                    end feature source: ScalarValues::Real; 
                    end feature target: ScalarValues::Real; 
                    inv inoutIsEqual { source == target }
                }
            }
            
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
        assertTrue(status.issues.isEmpty(), "Errors: ${status.issues}")
        val effectChain = global.resolve<Feature>("Signals::EffectChain::inoutIsEqual")
        val b = global.resolve<Feature>("b")!!.variable!!
        val a = global.resolve<Feature>("a")!!.variable!!
        assertEquals(4.0, b.max(), 0.0000001 )
        assertEquals(2.0, a.min(), 0.0000001 )
        assertNotNull(effectChain)
    }


    @Test
    fun testConnectFromToPropagation() = testSession("Signals") {
        loadKerML("""       
            class A {
                feature x: ScalarValues::Real(3 .. 3); 
            }
            class B { 
                feature y: ScalarValues::Real(2 .. 4); 
            }
            feature a: A; 
            feature b: B; 
            connector c: Signals::EffectChain from a::x to b::y; 
        """)
        propagate()
        val ax = global.resolveVar("A::x")
        val bx = global.resolveVar("B::y")
        val c = global.resolve<Connector>("c")
        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
        assertEquals(3.0, global.resolveVar("a::x")!!.min(),0.00001)
        assertEquals(3.0, global.resolveVar("b::y")!!.min(),0.00001)
        assertEquals(3.0, global.resolveVar("b::y")!!.max(),0.00001)
    }
}