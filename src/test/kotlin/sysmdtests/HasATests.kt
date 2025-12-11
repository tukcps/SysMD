package sysmdtests

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.IntegerRange
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMD
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


/**
 * Tests that check the ownership creation, in particular by "hasA";
 * This corresponds to SysML v2 curly braces.
 */
class HasATests {

    /**
     * Test adding a simple value to global namespace.
     */
    @Test fun sysMdCreatesValueFeature() = testSession("ScalarValues") {
        loadKerML("feature x: ScalarValues::Real;")
        assertNoIssues()
        assertNotNull(get().filterIsInstance<Feature>().find { it.declaredName == "x" })
    }

    /**
     * Test values with units
     */
    @Test  fun testHasACreation() = testSession("ISQ") {
        loadKerML(input = """
            private import ScalarValues; 
            type x :> Base::Anything {
                feature a: ISQ::VoltageValue (1.0 .. 3.0) [V] = 3000.0 mV; 
                feature b: ISQ::ElectricCurrentValue (2.0 .. 4.0) [A]; 
            }        
        """)
        assertNoIssues()
    }


    /**
     * Syntactic variants should be parsed correctly.
     */
    @Test fun hasARangeTest() = testSession("ScalarValues") {
        loadSysMD("""
            Global hasA package hasARange. 
            hasARange hasA class Reifen.
            hasARange hasA class Motor.
            hasARange hasA class Auto.
            hasARange hasA class Karosserie. 
            
            hasARange::Motor hasA 
                feature power: ScalarValues::Real(100).
            hasARange::Auto hasA
                feature räder:  Reifen [1 .. 4];
                feature motoren: Motor [1 .. 2]; 
                feature karosserie: Karosserie; 
                feature x: ScalarValues::Real = Motor::power.
        """)
        assertEquals(4, global.resolve("hasARange::Auto")?.memberElement?.getOwnedElementsOfType<Feature>()?.size)
        assertEquals(1, global.resolve("hasARange::Auto")?.member<Namespace>()?.getOwned<Feature>("motoren")?.multiplicityRange?.min )
        assertEquals(2, global.resolve("hasARange::Auto")?.member<Namespace>()?.getOwned<Feature>("motoren")?.multiplicityRange?.max )
        assertEquals(1, (global.resolveVar("hasARange::Auto::motoren::cardinality"))!!.intSpecs[0].min )
        assertEquals(2, (global.resolveVar("hasARange::Auto::motoren::cardinality"))!!.intSpecs[0].max )
        assertEquals(1.0,
            (global.resolveVar("hasARange::Auto::motoren::cardinality"))!!.min(), 0.0001 )
        assertEquals(2.0,
            (global.resolveVar("hasARange::Auto::motoren::cardinality"))!!.max(), 0.0001 )
    }


    /**
     * There exist no two elements with the same name in a namespace.
     * SysMD shall update an existing feature accordingly.
     */
    @Test fun hasATest1()  = testSession("ScalarValues") {
        loadKerML("""
               type a :> Base::Anything; 
               type b :> Base::Anything {  
                   feature x: a [1 .. 3]; 
               }
            """)
        val b1 = global.resolve("b::x")?.member<Feature>()
        assertEquals(IntegerRange(1, 3), b1?.multiplicityRange, "Multiplicity must be 1..3")

        loadSysMD("""b hasA feature x: a [1 .. 2].""")
        assertNoIssues()
        val b2 = global.resolve("b::x")?.member<Feature>()
        assertEquals(IntegerRange(1, 2), b2?.multiplicityRange, "An already existing feature shall be updated")
    }


    /**
     * Second load updates multiplicity
     */
    @Test fun multiplicityTest() = testSession("ScalarValues") {
        loadKerML("feature y: Base::Anything [2 .. 3]; ")
        assertNoIssues()
        val y: Feature? = global.resolve("y")?.member()
        loadKerML("feature y: Base::Anything [3 .. 4]; ")
        assertEquals(IntegerRange(3, 4), y?.multiplicityRange)
    }


    @Test
    fun valueFeatureTest() = testSession("ScalarValues") {
        loadKerML("""
            package X { feature pi: ScalarValues::Real = 3.14; }
        """)
        assertNoIssues()
        val x = global.getOwnedElement("X")
        assertNotNull(x)
        val pi = x.getOwnedElement("pi")
        assertNotNull(pi)
        assertTrue(x is Package)
        assertTrue(pi is Feature)
    }


    @Test
    fun iso26262Test() = testSession("ISO26262") {
        loadKerML("""
            feature funktion: ISO26262::Function; 
            feature x: ISO26262::Component {
                connector f: ISO26262::implements from x to funktion;
            } // = it implements funktion.
        """)
        assertNoIssues()
        val x = global.resolve("x")?.member<Feature>()
        assertNotNull(x)
        val f: Connector? = global.resolve("x::f")?.member()
        val func = global.resolve("funktion")?.memberElement
        assertNotNull(func)
        assertNotNull(f)
        assertEquals(1, f.target.size)
        assertEquals(1, f.source.size)
        val to = global.resolve("x::f::to")?.member<Feature>()?.referencedFeature
        assertEquals(global.resolve("funktion")?.memberElement, to)
        assertEquals(x, global.resolve("x::f::from")?.member<Feature>()?.referencedFeature)
    }
}
