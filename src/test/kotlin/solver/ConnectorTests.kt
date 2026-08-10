package solver

import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Connector
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConnectorTests {
    /**
     * An effect chain or other similar signal owns a constraint that requires
     * that source and target are equal.
     * TODO: Check test, potential causes. First check if feature is there in model.
     * TODO: Then, check if variable is generated properly from feature.
     */
    @Test
    fun connectorEndsAccessibleInDef() = testSession( "Links", "Ranges") {
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
        """, Runlevel.ALL)
        assertNoIssues()

        val c = global.resolve("c")?.member<Connector>()
        assertNotNull(c)
        val sa = solver.getVariable("c::source")
        assertNotNull(sa)
        val tb = solver.getVariable("c::target")
        assertNotNull(tb)
        assertEquals(solver.getVariable("a"), sa)
        assertEquals(solver.getVariable("b"), tb)

        val effectChain = global.resolve("Signals::EffectChain::inoutIsEqual")?.memberElement
        val b = solver.getVariable("b")!!
        val a = solver.getVariable("a")!!
        assertEquals(4.0, b.max(), 0.0000001)
        assertEquals(2.0, a.min(), 0.0000001)
        assertNotNull(effectChain)
    }

    @Test
    fun testConnectFromToPropagation() = testSession("Signals", "Ranges") {
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
        """, Runlevel.ALL)
        assertNoIssues()
        val ax = solver.getVariable("a::x")
        assertNotNull(ax)
        val by = solver.getVariable("b::y")
        assertNotNull(by)
        val aax = solver.getVariable("A::x")
        assertNotNull(aax)
        val bby = solver.getVariable("B::y")
        assertNotNull(bby)
        val ec = global.resolve("Signals::EffectChain")?.member<Association>()
        assertNotNull(ec)
        val c = global.resolve("c")?.member<Connector>()
        assertNotNull(c)
        val cs = global.resolve("c::source")?.memberElement
        assertNotNull(cs)
        assertEquals(3.0, ax.min(), 0.00001)
        assertEquals(3.0, by.min(), 0.00001)
        assertEquals(3.0, by.max(), 0.00001)
        assertEquals(2.0, bby.min(), 0.00001)
        assertEquals(4.0, bby.max(), 0.00001)
    }
}