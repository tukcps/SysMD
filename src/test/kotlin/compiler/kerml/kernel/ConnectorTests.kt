package compiler.kerml.kernel

import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Connector
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.services.*
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class ConnectorTests {

    @Test
    fun basicConnector() = testSession("Links") {
        loadKerML("connector c;", Runlevel.MODEL)
        assertNoIssues()
    }

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
        """, Runlevel.MODEL)
        assertNoIssues()
        val c = global.resolve("c")?.memberElement as Connector
        assertNotNull(c)
        val b = global.resolve("b")?.memberElement as Feature
        assertNotNull(b)
        val a = global.resolve("a")?.memberElement
        assertNotNull(a)
        val cs = c.source.firstOrNull()
        val ct = c.target.firstOrNull()
        assertNotNull(cs)
        assertNotNull(ct)
        val cS = solver.getVariable("c::source")
        assertEquals(solver.getVariable("a"), cS)
    }

    /**
     * For a repeated execution, connector ends should not be created again.
     * Not entirely sure where this should be handled. The best might be to
     * - in a single compiler run to provide warning/error if connector c is repeated.
     * - in a second or later compiler run to merge c, a, b.
     */
    @Test
    @Ignore
    fun connectorCreatesConnectorEndsNotTwiceTest() = testSession("Links") {
        loadKerML("""
            feature a; 
            feature b; 
            connector c from a to b;
            connector c from a to b; // Model should refuse to add c and its end features a, b
        """)
        assertNoIssues()
        val c = global.resolve("c")?.memberElement
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
        val c: Connector? = global.resolve("c")?.member()
        val b: Feature? = global.resolve("b")?.member()
        val a: Feature? = global.resolve("a")?.member()
        val cto = getRelationshipsTo(b!!, "*", c)
        val cFrom = getRelationshipsFrom(a!!, "*", c)
        assertNoIssues()
        assertTrue(cto.isNotEmpty())
        assertTrue(cFrom.isNotEmpty())
    }

    @Test
    fun assocTestFeaturesNeededNotClasses() = testSession("Occurrences") {
        loadKerML("""
            class a;
            class b;
            connector c from a to b;
        """)
        assertTrue(
            status.issues.any { it.message.contains("feature") },
            "Class as parameter of connector shall report that connector source, target must be features"
        )
        val c = global.resolve("c")?.member<Connector>()
        assertNotNull(c)
    }

    /**
     * An effect chain or other similar signal owns a constraint that requires
     * that source and target are equal.
     */
    @Test
    fun connectorEndsAccessibleInDef() = testSession("Links") {
        assertNotNull(global.resolve("Links::BinaryLink"))
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
        """, Runlevel.MODEL)
        assertNoIssues()

        val c = global.resolve("c")?.member<Connector>()
        assertNotNull(c)
        val effectChain = global.resolve("Signals::EffectChain::inoutIsEqual")?.memberElement
        assertNotNull(effectChain)
    }

    @Test
    fun testConnectFromToFeature() = testSession("Signals", "Ranges") {
        loadKerML("""       
            class A {
                feature x: Ranges::RealInRange { :>> range = 3 .. 3;}
            }
            class B { 
                feature y: Ranges::RealInRange { :>> range = 2 .. 4;}
            }
            feature a: A;
            feature b: B;
            connector c: Signals::EffectChain from a.x to b.y;
        """, Runlevel.MODEL)
        assertNoIssues()
        val ec = global.resolve("Signals::EffectChain")?.member<Association>()
        assertNotNull(ec)
        val c = global.resolve("c")?.member<Connector>()
        assertNotNull(c)
        val cs = global.resolve("c::source")?.memberElement
        assertNotNull(cs)
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
        assertNoIssues()
        val source = global.resolve("bb")?.member<Feature>()
        val rel = global.resolve("rel")?.member<Association>()
        val r = global.resolve("r")?.member<Connector>()
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
        assertNoIssues()
        val c = global.getOwned<Connector>("c")
        assertNotNull(c)
        assertEquals(1, c.target.size)
        assertEquals(1, c.source.size)
        val source = solver.getVariable("c::source")
        val target = solver.getVariable("c::target")
        assertEquals(solver.getVariable("f::a"), source)
        assertEquals(solver.getVariable("f::b"), target)
    }
}