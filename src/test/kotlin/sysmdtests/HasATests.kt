package sysmdtests

import io.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.mockup.loadSysMD
import util.testSession
import kotlin.test.*


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
        assertEquals(0, status.issues.size, status.issues.toString())
        assertNotNull(get().filterIsInstance<Feature>().find { it.declaredName == "x" })
    }

    /**
     * Test values with units
     */
    @Test  fun testHasACreation() = testSession("SI") {
        loadKerML(input = """
        private import ScalarValues; 
        type x :> Base::Anything {
            feature a: SI::Voltage (1.0 .. 3.0) [V] = 3000.0 mV; 
            feature b: SI::ElectricCurrent (2.0 .. 4.0) [A]; 
        }        
        """)
        assertEquals(0, status.issues.size, status.issues.toString())
    }


    /**
     * Syntactic variants should be parsed correctly.
     */
    @Test fun hasARangeTest() = testSession("ScalarValues") {
        settings.catchExceptions = false
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
        assertEquals(4, global.resolve<Element>("hasARange::Auto")?.getOwnedElementsOfType<Feature>()?.size)
        assertEquals(1, global.resolve<Namespace>("hasARange::Auto")?.getOwned<Feature>("motoren")?.multiplicity?.min )
        assertEquals(2, global.resolve<Namespace>("hasARange::Auto")?.getOwned<Feature>("motoren")?.multiplicity?.max )
        assertEquals(1, (global.resolve<Feature>("hasARange::Auto::motoren::cardinality"))!!.variable!!.intSpecs[0].min )
        assertEquals(2, (global.resolve<Feature>("hasARange::Auto::motoren::cardinality"))!!.variable!!.intSpecs[0].max )
        assertEquals(1.0,
            (global.resolve<Feature>("hasARange::Auto::motoren::cardinality"))!!.variable!!.vectorQuantity.getMinAsDouble(), 0.0001 )
        assertEquals(2.0,
            (global.resolve<Feature>("hasARange::Auto::motoren::cardinality"))!!.variable!!.vectorQuantity.getMaxAsDouble(), 0.0001 )
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
        val b1 = global.resolve<Feature>("b::x")!!
        assertEquals(IntegerRange(1, 3), b1.multiplicity, "Multiplicity must be 1..3")

        loadSysMD("""b hasA feature x: a [1 .. 2].""")
        assertEquals(0, status.issues.size, status.issues.toString())
        val b2 = global.resolve<Feature>("b::x")!!
        assertEquals(IntegerRange(1, 2), b2.multiplicity, "An already existing feature shall be updated")
    }


    /**
     * Second load updates multiplicity
     */
    @Test fun multiplicityTest() = testSession("ScalarValues") {
        loadKerML("feature y: Base::Anything [2 .. 3]; ")
        assertEquals(0, status.issues.size, status.issues.toString())
        val y = global.resolve<Feature>("y")
        loadKerML("feature y: Base::Anything [3 .. 4]; ")
        assertEquals(IntegerRange(3, 4), y?.multiplicity)
    }


    @Test
    fun valueFeatureTest() = testSession("ScalarValues") {
        loadKerML("""
            package X { feature pi: ScalarValues::Real = 3.14; }
        """.trimIndent())
        assertEquals(0, status.issues.size, status.issues.toString())
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val x = global.getOwned<Feature>("x")
        assertNotNull(x)
        val f = x.getOwned<Connector>("f")
        val func = global.resolve<Feature>("funktion")
        assertNotNull(func)
        assertNotNull(f)
        assertEquals(1, f.target.size)
        assertEquals(1, f.source.size)
        val to = global.resolve<Feature>("x::f::to")
        assertEquals(global.resolve("funktion"), to)
        assertEquals(x, global.resolve("x::f::from"))
    }
}
