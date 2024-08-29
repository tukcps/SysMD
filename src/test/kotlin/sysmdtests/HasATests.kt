package sysmdtests

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


/**
 * Tests that check the ownership creation, in particular by "hasA";
 * This corresponds to SysML v2 curly braces.
 */
class HasATests {

    /**
     * Test adding a simple value to global namespace.
     */
    @Test fun sysMdCreatesValueFeature() = testSession {
        loadSysMD("feature x: ScalarValues::Real;")
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertNotNull(get().filterIsInstance<Feature>().find { it.declaredName == "x" })
    }

    /**
     * Test values with units
     */
    @Test  fun testHasACreation() = testSession {
        loadSysMD(input = """
        import ScalarValues; 
        class x {
            feature a: ScalarValues::Real (1.0 .. 3.0) [V] = 3000.0 mV; 
            feature b: ScalarValues::Real (2.0 .. 4.0) [A]; 
        }        """.trimIndent())
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }


    /**
     * Syntactic variants should be parsed correctly.
     */
    @Test fun hasASyntax() = testSession {
        loadSysMD( input = """
            package hasARange.
            hasARange defines
                class Reifen isA Base::Anything;
                class Motor isA Base::Anything;
                class Karosserie isA Base::Anything;
                class Auto isA Base::Anything.
            hasARange::Auto hasA feature räder: hasARange::Reifen[1..4] .
            hasARange::Auto hasA feature motor: hasARange::Motor[1..2] .
            hasARange::Auto hasA feature karosserie: hasARange::Karosserie.  // 1..1 is default
            """, catchExceptions = false)
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(3, global.resolve<Element>("hasARange::Auto")?.getOwnedElementsOfType<Feature>()?.size)
        assertEquals(1, global.resolve<Feature>("hasARange::Auto::motor")?.multiplicity?.min)
    }

    /**
     * Syntactic variants should be parsed correctly.
     */
    @Test fun hasARangeTest() = testSession {
        settings.catchExceptions = false
        loadSysMD("""
            package hasARange. 
            hasARange defines 
                class Reifen;
                class Motor;
                class Auto;
                class Karosserie.
            hasARange::Motor hasA Value power: ScalarValues::Real(100).
            hasARange::Auto hasA
                    part räder:  Reifen [1 .. 4];
                    part motoren: Motor [1 .. 2]; 
                    part karosserie: Karosserie; 
                    feature x: ScalarValues::Real = Motor::power.
            """)
        assertEquals(4, global.resolve<Element>("hasARange::Auto")?.getOwnedElementsOfType<Feature>()?.size)
        assertEquals(1, global.resolve<Element>("hasARange::Auto")?.getOwned<Feature>("motoren")?.multiplicity?.min )
        assertEquals(2, global.resolve<Element>("hasARange::Auto")?.getOwned<Feature>("motoren")?.multiplicity?.max )
        assertEquals(1, (global.resolve<Feature>("hasARange::Auto::motoren::multiplicity"))!!.variable!!.intSpecs[0].min )
        assertEquals(2, (global.resolve<Feature>("hasARange::Auto::motoren::multiplicity"))!!.variable!!.intSpecs[0].max )
        assertEquals(1.0,
            (global.resolve<Feature>("hasARange::Auto::motoren::multiplicity"))!!.variable!!.vectorQuantity.getMinAsDouble(), 0.0001 )
        assertEquals(2.0,
            (global.resolve<Feature>("hasARange::Auto::motoren::multiplicity"))!!.variable!!.vectorQuantity.getMaxAsDouble(), 0.0001 )
    }


    /**
     * there exist no two elements with the same name in a namespace.
     * SysMD shall update an existing feature accordingly.
     */
    @Test fun hasATest1()  = testSession(loadKerML = false) {
        loadSysMD("""
           package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; }
           class a; 
           class b; 
           b hasA feature x: a [1 .. 3].           
        """)
        +"""b hasA feature x: a [1 .. 2]."""
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val b = global.resolve<Feature>("b::x")!!
        assertEquals(IntegerRange(1, 2), b.multiplicity, "An already existing feature shall be updated")
    }


    /**
     * Second load updates multiplicity
     */
    @Test fun multiplicityTest() = testSession {
        loadSysMD("feature y: Base::Anything [2 .. 3] .")
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val y = global.resolve<Feature>("y")
        loadSysMD("feature y: Base::Anything [3 .. 4].")
        assertEquals(IntegerRange(3, 4), y?.multiplicity)
    }


    @Test
    fun valueFeatureTest() = testSession(catchExceptions = false) {
        loadSysMD("""
            package X { feature pi: ScalarValues::Real = 3.14; }
        """.trimIndent())
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val x = global.getOwnedElement("X")
        assertNotNull(x)
        val pi = x!!.getOwnedElement("pi")
        assertNotNull(pi)
        assertTrue(x is Package)
        assertTrue(pi is Feature)
    }

    @Test
    fun relationTest() = testSession("ISO26262") {
        loadSysMD("""
            feature f: ISO26262::Function; 
            feature c: ISO26262::Component { 
                connector conn: ISO26262::implements from c to f; 
            }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<Feature>("c")
        assertNotNull(c)
        val f = global.resolve<Feature>("f")
        assertNotNull(f)
        val conn = global.resolve<Connector>("c::conn")
        assertEquals(1, conn!!.target.size)
        assertEquals(1, conn.source.size)
        assertEquals(f, conn.target[0].ref)
        assertEquals(c, conn.source[0].ref)
    }

    @Test
    fun relation2Test() = testSession("ISO26262") {
        loadSysMD("""
            feature funktion: ISO26262::Function; 
            feature x: ISO26262::Component {
                connector f: ISO26262::implements = from x to funktion;
            } // = it implements funktion.
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val x = global.getOwned<Element>("x")
        assertNotNull(x)
        val f = x!!.getOwned<Connector>("f")
        val func = global.resolve<Feature>("funktion")
        assertNotNull(func)
        assertNotNull(f)
        assertTrue(x is Feature)
        assertTrue(f is Connector)
        assertEquals(1, f!!.target.size)
        assertEquals(1, f.source.size)
        assertEquals(global.resolve("funktion"), f.target[0].ref)
        assertEquals(x, f.source[0].ref)
    }


    @Test
    fun relation3Test() = testSession {
        loadSysMD("""
            assoc Signal {
                end feature x: ScalarValues::Real;
                end feature y: ScalarValues::Real; 
            }
            feature a: ScalarValues::Real;
            feature b: ScalarValues::Real;  
            connector c: Signal = from a to b; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.getOwned<Connector>("c")
        assertNotNull(c)
        assertTrue(c is Connector)
        assertEquals(1, c!!.target.size)
        assertEquals(1, c.source.size)
        assertEquals(global.resolve("a"), c.source[0].ref)
        assertEquals(global.resolve("b"), c.target[0].ref)
    }
}
