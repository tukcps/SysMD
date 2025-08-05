package compiler.kerml

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Connector
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.services.findRelationshipsFrom
import com.github.tukcps.sysmd.services.findRelationshipsTo
import com.github.tukcps.sysmd.services.getRelationshipsFrom
import com.github.tukcps.sysmd.services.getRelationshipsTo
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConnectorTests {

    /**
     * A connector relates two or more features.
     * The related features a, b are added as features that
     * - reference a, b externally
     * - redefine the respective end features from the association
     */
    @Test
    fun connectorCreatedTest() = testSession("Links") {
        loadKerML("""
            feature a; 
            feature b; 
            connector c from a to b;
        """)
        assertNoIssues()
        val c = global.resolve<Connector>("c")
        assertNotNull(c)
        val b = global.resolve<Feature>("b")
        assertNotNull(b)
        val a = global.resolve<Feature>("a")
        assertNotNull(a)
        val cs = c.source.firstOrNull()
        val ct = c.target.firstOrNull()
        assertNotNull(cs)
        assertNotNull(ct)
        val cS = global.resolve<Element>("c::source")
        assertEquals(a, cS)
    }

    /**
     * For a repeated execution, connector ends should not be created again.
     * Not entirely sure where this should be handled. The best might be to
     * - in a single compiler run to provide warning/error if connector c is repeated.
     * - in a second or later compiler run to merge c, a, b.
     */
    @Test @Ignore
    fun connectorCreatesConnectorEndsNotTwiceTest() = testSession("Links") {
        loadKerML("""
            feature a; 
            feature b; 
            connector c from a to b;
            connector c from a to b; // Model should refuse to add c and its end features a, b
        """)
        assertNoIssues()
        val c = global.resolve<Connector>("c")
        assertNotNull(c)
        assertEquals(5, c.ownedElement.size)
    }

    @Test
    fun connectorWithAssocTest() = testSession("Links") {
        loadKerML("""
            assoc test; 
            feature a; 
            feature b; 
            connector c: test from a to b; 
        """)
        val c = global.resolve<Connector>("c")
        val b = global.resolve<Feature>("b")
        val a = global.resolve<Feature>("a")
        val cto = getRelationshipsTo(b!!, "*", c)
        val cFrom = getRelationshipsFrom(a!!, "*", c)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertTrue(cto.isNotEmpty())
        assertTrue(cFrom.isNotEmpty())
    }

    @Test
    fun assocTestFeaturesNeededNotClasses() = testSession( "Occurrences") {
        loadKerML("""
            class a;
            class b;
            connector c from a to b;
        """)
        assertTrue(status.issues.any { it.message.contains("feature") }, "Class as parameter of connector shall report that connector source, target must be features")
        val c = global.resolve<Connector>("c")
        assertNotNull(c)
    }

    /**
     * An effect chain or other similar signal owns a constraint that requires
     * that source and target are equal.
     */
    @Test
    fun connectorEndsAccessibleInDef() = testSession("Occurrences", "Ranges") {
        loadKerML("""
            package Signals {
                assoc EffectChain {
                    :>> source: ScalarValues::Real; 
                    :>> target: ScalarValues::Real; 
                    inv inoutIsEqual { source == target } // uses re-defined source, target
                }
            }
            
            feature a: ScalarValues::Real(1..4); 
            feature b: ScalarValues::Real(2..5);
            connector c: Signals::EffectChain from a to b; // source and target reference a resp. b 
            // Constraint-propagation makes a, b to 2..4! 
        """.trimIndent())
        propagate()
        val c = global.resolve<Connector>("c")
        assertNotNull(c)
        val sa = global.resolve<Feature>("c::source")
        assertNotNull(sa)
        val tb = global.resolve<Feature>("c::target")
        assertNotNull(tb)
        // assertNotNull(sa.referencedFeature)
        // assertNotNull(tb.referencedFeature)
        assertEquals(global.resolve<Feature>("a"), sa)
        assertEquals(global.resolve<Feature>("b"), tb)

        assertTrue(status.issues.isEmpty(), "Errors: ${status.issues}")
        val effectChain = global.resolve<Feature>("Signals::EffectChain::inoutIsEqual")
        val b = global.resolve<Feature>("b")!!.variable!!
        val a = global.resolve<Feature>("a")!!.variable!!
        assertEquals(4.0, b.max(), 0.0000001 )
        assertEquals(2.0, a.min(), 0.0000001 )
        assertNotNull(effectChain)
    }


    @Test
    fun testConnectFromToPropagation() = testSession("Signals", "Ranges") {
        loadKerML("""       
            class A {
                feature x: Ranges::RealInRange { :>> range = "3 .. 3";}
            }
            class B { 
                feature y: Ranges::RealInRange { :>> range = "2 .. 4";}
            }
            feature a: A;
            feature b: B;
            connector c: Signals::EffectChain from a.x to b.y; 
        """)
        propagate()
        val ax = global.resolveVar("a::x")
        assertNotNull(ax)
        val by = global.resolveVar("b::y")
        assertNotNull(by)
        val aax = global.resolveVar("A::x")
        assertNotNull(aax)
        val bby = global.resolveVar("B::y")
        assertNotNull(bby)
        val ec = global.resolve<Association>("Signals::EffectChain")
        assertNotNull(ec)
        val c = global.resolve<Connector>("c")
        assertNotNull(c)
        val cs = global.resolve<Element>("c::source")
        assertNotNull(cs)
        assertTrue(status.issues.isEmpty(), "${status.issues}")
        assertEquals(3.0, ax.min(),0.00001)
        assertEquals(3.0, by.min(),0.00001)
        assertEquals(3.0, by.max(),0.00001)
        assertEquals(2.0, bby.min(),0.00001)
        assertEquals(4.0, bby.max(),0.00001)
    }


    @Test
    fun connectorWithMultipleSourcesTargets() = testSession("Links", "Occurrences") {
        loadKerML("""
            class A; 
            class B; 
            feature aa: A;
            feature bb: B; 
            assoc rel {
                end feature b: B :>> source;
                end feature a: A :>> target;  
            }
            connector r: rel (aa, bb);
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val source = global.resolve<Feature>("bb")
        val rel = global.resolve<Association>("rel")
        val r = global.resolve<Connector>("r")
        assertNotNull(r)
        assertNotNull(rel)
        val relFromSource = findRelationshipsFrom(source!!, "*", rel).toList()
        val relToTarget = findRelationshipsTo(source, "*", rel).toList()
        assertEquals(1, relFromSource.first().source.size)
        assertEquals(2, relToTarget.first().target.size)
        assertEquals(0, r.source.size)
        assertEquals(2, r.target.size)
    }


    @Test
    fun relation3Test() = testSession("Links") {
        loadKerML("""
            feature f {
                feature a;
                feature b;  
            }
            connector c from f.a to f.b; 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val c = global.getOwned<Connector>("c")
        assertNotNull(c)
        assertEquals(1, c.target.size)
        assertEquals(1, c.source.size)
        val source = global.resolve<Feature>("c::source")
        val target = global.resolve<Feature>("c::target")
        assertEquals(global.resolve("f::a"), source)
        assertEquals(global.resolve("f::b"), target)
        //assertEquals(global.resolve("f::a"), c.source[0])
        //assertEquals(global.resolve("f::b"), c.target[0])
    }
}