package sysmdtests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Here are tests of something that do not work currently.
 * The tests are still passed, because a assertFailsWith
 * function is used, but these tests should work without assertFailsWith.
 *
 * Some tests are already fixed, but we keep them to avoid regression.
 */

class FailedTests {

    // It is not possible to assign a value without a formula to a non-SI unit
    @Test
    fun unitTransformTest() = testSession("ScalarValues") {
        loadSysMD(input = """
            // It is not possible to assign a value without a formula to a non-SI unit
            //only test1 works
            class Test {
                attribute test1: ScalarValues::Real [A]= 1.0 A;
                attribute test2: ScalarValues::Real [N]= 1.0 N;
                attribute test3: ScalarValues::Real [Ohm]= 1.0 [Ohm];
                attribute test4: ScalarValues::Real [Ohm m]= 1.0 [Ohm m];
            }
            """
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
    }

    // The operations exp, power2, sqrt, ln .. are not supported in combination with units yet
    @Test fun unitsWithOperations()  = testSession("ScalarValues") {
        loadSysMD(catchExceptions = false, input = """
            Package unitsWithOperation;

            // The operations exp, power2, sqrt, ln .. are not supported in combination with units yet         
            //test with V
            unitsWithOperation hasA
                 Value testV: ScalarValues::Real [V] = 5.0 [V];
                 Value testVSquare: ScalarValues::Real [V^2] = 49.0 [V^2]; 
                 Value test4: ScalarValues::Real [V] = sqrt(testVSquare). 
                //Property test5: ScalarValues::Real [V] = exp(testV)
                //Property test6: ScalarValues::Real [V] = power2(testV)
            """
        )
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test
    fun unitsInMultipleIterations() = testSession("Math", catchExceptions = false) {
        +"""package hello; 
            hello defines class world.
            hello::world hasA Value density: ScalarValues::Real [kg/l] = 1.0 [kg/l].
            hello::world hasA Value r:       ScalarValues::Real [km] = 1000.0 km.
            hello::world hasA Value volume:  ScalarValues::Real = 4.0/3.0 * r * r * r * Math::pi. 
            hello::world hasA Value mass:    ScalarValues::Real = density * volume."""
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /** Works ... */
    @Test
    fun additionTrivial()  = testSession("ScalarValues") {
        +"package p;"
        +"p defines class i."
        +"p::i hasA feature p: ScalarValues::Real  = 1.0 + 1000.0."
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        assertEquals(1001.0, global.resolveVar("p::i::p")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
    }

    /** Does not copy up-propagated value into quantity field */
    @Test // @Ignore
    fun additionTrivial2() = testSession("ScalarValues", catchExceptions = false) {
        loadSysMD(""" 
            package p; 
            p defines class i.
            p::i hasA Value p: ScalarValues::Real = 1.0 m + 1.0 km.
        """.trimIndent())
        // p::i::p is wrongly identified in initialization --> resolveName issue?
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        // println("p="+global.resolveName<Expression>("p::i::p"))
        assertEquals(1001.0, global.resolveVar("p::i::p")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
    }

    @Test
    fun additionTrivial3() = testSession {
        +"package p;"
        +"p defines class i."
        +"p::i hasA feature p: ScalarValues::Real [km] = 1.0 m + 1.0 km."
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        global.resolveVar("p::i::p")?.ast?.evalUpRec()
        assertEquals(1001.0, global.resolveVar("p::i::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        global.resolveVar("p::i::p")?.ast?.evalDownRec()
        assertEquals(1001.0, global.resolveVar("p::i::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        global.resolveVar("p::i::p")?.ast?.evalUpRec()
        propagate()
        global.resolveVar("p::i::p")?.ast?.evalDownRec()
        // println(resolveName<Expression>("p::i::p"))
    }

    /**
     * has Error on Unit subtraction
     * also value down has no unit.
     * FIX: in evalDown, unit is not converted if unitSpec is empty string.
     */
    @Test
    fun fail2() = testSession {
        loadSysMD("""
            Value p: ScalarValues::Real [m] = 1.0 m;
            Value p2: ScalarValues::Real [km]= 1.0 km; 
            Value p3: ScalarValues::Real = p + p2;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        // println(status.errors)
        // println(resolveName<Expression>("p::a::p3"))
        assertEquals(1001.0, global.resolveVar("p3")!!.vectorQuantity.getMinAsDouble(), 0.001)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        // Also check unit of down !!! it is not m.
    }

    /**
     *  Number of bugs fixed, remaining issue:
     *  If for properties i.e. p, p2, a unit becomes known later and is not in UnitSpec,
     *  it might not be considered properly in p3, hence problems with intersections????
     */
    @Test
    fun fail3() = testSession("SI") {
        loadSysMD(input = """
            package p { 
                class a { 
                    feature p: SI::Length = 1.0 m;
                    feature p2: SI::Length = 1.0 km;
                    feature p3: SI::Length = p + p2;
                }
            }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(1001.0, global.resolveVar("p::a::p3")!!.vectorQuantity.getMinAsDouble(), 0.001)
    }

    /**
     * Comparison of two AADD that cannot be equal must return FALSE.
     * Problem: correlation terms are lost, maybe by intersect operation?
     * ** likely: Always new created in each iteration, should be done once with fixed noise variable.
     *  -- fixed
     *  ** Remaining issue: Infeasible paths are not reduced; to avoid confusion of user, we convert them to
     *  **   True, False and drop Infeasible paths in toString method.
     */
    @Test
    fun fail4() = testSession {
        loadSysMD("""
            attribute p:  ScalarValues::Real(2 .. 4);
            attribute p2: ScalarValues::Real = p + 1.0;
            attribute p3: ScalarValues::Boolean = ( p > p2 ).
        """.trimIndent())
        propagate()

        val p = global.resolveVar("p")!!.vectorQuantity.aadd()
        val p2 = global.resolveVar("p2")!!.vectorQuantity.aadd()
        val p3 = global.resolveVar("p3")!!.vectorQuantity.bdd()

        assertEquals("2..4", p.toString())
        assertEquals("3..5", p2.toString())
        assertEquals("False", p3.toString())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }


    /**
     * Problem: Intersection in intermediate result is not computed on both way up and down.
     * Value ov V from up-propagation seems to get over-written by down-propagated value.
     */
    @Test
    fun simplePhysicsExample() = testSession {
        loadSysMD(""" 
            attribute I: ScalarValues::Real(9.9 .. 10.1); 
            attribute R: ScalarValues::Real(1.9 .. 2.1); 
            attribute V: ScalarValues::Real = I * R; 
            attribute P: ScalarValues::Real = I * V; 
        """)
        assertEquals(9.9*1.9, global.resolveVar("V")!!.min(), 0.00001)
        assertEquals(10.1*2.1, global.resolveVar("V")!!.max(), 0.00001)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        // println("V = " + global.resolveName<Expression>("V") + " ")
        assertEquals(9.9*1.9, global.resolveVar("V")!!.min(), 0.00001)
        assertEquals(21.21, global.resolveVar("V")!!.max(), 0.00001)
    }


    @Test
    fun sumHasAWithoutUnits() = testSession("Parts", "Ports") {
        loadSysMD(""" 
            import ScalarValues::*;
            package Smartgrid {
                class Microgrid;
                class Smokedetector; 
                class SmartParking; 
            }
           
            Smartgrid::SmartParking hasA
                feature powConsumption: Real(0..800).
           
            Smartgrid::Microgrid hasA  
                feature parking: Smartgrid::SmartParking.
           
            package Example {
                class gridSupply;
                class parking isA Smartgrid::SmartParking;
                class house1 isA Smartgrid::Microgrid;
                class Smoke isA Smartgrid::Smokedetector;
            }
                
            Example::Smoke hasA 
                feature powConsumption: Real(2..50).
            
            Example::house1 hasA 
                feature Smoke: Example::Smoke. 

            Example::gridSupply hasA 
                feature microconsumer1: Example::house1.
                
            Example::gridSupply hasA 
                feature totalpowConsumption: Real = sumOverParts(powConsumption).
            """)
        assertEquals(0, status.exceptions.size, "Error message: ${status.exceptions}")
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(2.0, global.resolveVar("Example::gridSupply::totalpowConsumption")!!.min(), 0.00001)
        // house1 is owned by gridsupply and
        // - has directly a Smoke with 2 .. 50 power consumption
        // - has inherited a Smart Parking as it is a Microgrid. with 0 .. 800 power consumption.
        assertEquals(850.0, global.resolveVar("Example::gridSupply::totalpowConsumption")!!.max(), 0.00001)
    }

    @Test fun evalDownReal() = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Real; 
            attribute b: ScalarValues::Real(3..5); 
            attribute sum: ScalarValues::Real(9..10) = a+b;""")
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(9.0, global.resolveVar("sum")!!.aadd().min, 0.00001)
        assertEquals(10.0, global.resolveVar("sum")!!.aadd().max, 0.000001)
        assertEquals(4.0, global.resolveVar("a")!!.aadd().getRange().min,0.0001)
        assertEquals(7.0, global.resolveVar("a")!!.aadd().getRange().max,0.0001)
        assertEquals(3.0, global.resolveVar("b")!!.aadd().getRange().min,0.0001)
        assertEquals(5.0, global.resolveVar("b")!!.aadd().getRange().max,0.0001)
    }

    /**
     * Integers do not well deal with overflows.
     * One overflow breaks the whole computation chain ...
     */
    @Test fun evalDownInt() = testSession {
        loadSysMD(""" 
           feature a: ScalarValues::Integer;
           feature b: ScalarValues::Integer(3..5);
           feature sum: ScalarValues::Integer(9..10) = a+b.
           """)
        propagate()
        // println(status.errors)
        assertEquals(9, global.resolveVar("sum")!!.idd().getRange().min)
        assertEquals(10, global.resolveVar("sum")!!.idd().getRange().max)
        assertEquals(4, global.resolveVar("a")!!.idd().getRange().min)
        assertEquals(7, global.resolveVar("a")!!.idd().getRange().max)
        assertEquals(3, global.resolveVar("b")!!.idd().getRange().min)
        assertEquals(5, global.resolveVar("b")!!.idd().getRange().max)
    }

    /**
     * Throws error "lateinit property downQuantity has not been initialized", this is caused by an evaluation
     * of the 3rd parameter of the sum_i function before 'i' is defined
     */
    @Test fun sumFunctionTestRangeExpr() = testSession {
        loadSysMD("""
            attribute i: ScalarValues::Real.
            attribute s: ScalarValues::Real = 10.0.
            attribute MAC_notb: ScalarValues::Real = sum_i( 0.0, 3.0, s*i )."""                  // eq. 12
        )
        propagate()
        assertEquals(60.0, global.resolveVar("MAC_notb")!!.aadd().getRange().min, 0.00001)
        assertEquals(60.0, global.resolveVar("MAC_notb")!!.aadd().getRange().max, 0.00001)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}


