package sysmdtests

import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
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
    fun unitTransformTest() = testSession("ISQ") {
        loadKerML(input = """
            // It is not possible to assign a value without a formula to a non-SI unit
            //only test1 works
            type Test :> Base::Anything {
                feature test1: ISQ::ElectricCurrentValue = 1.0 A;
                feature test2: ISQ::ForceValue = 1.0 N;
                feature test3: ISQ::ResistanceValue = 1.0 [Ohm];
                feature test4: Quantities::ScalarQuantityValue [Ohm m] = 1.0 [Ohm m];
            }
            """
        )
        solver.propagate()
        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
    }

    // The operations exp, power2, sqrt, ln .. are not supported in combination with units yet
    @Test fun unitsWithOperations()  = testSession("ISQ") {
        loadKerML("""
            package unitsWithOperation {
                 feature testV: ISQ::ElectricPotentialDifferenceValue = 5.0 [V];
                 feature testVSquare: Quantities::ScalarQuantityValue [V^2] = 49.0 [V^2]; 
                 feature test4: ISQ::ElectricPotentialDifferenceValue = sqrt(testVSquare); 
                //Property test5: ISQ::ElectricPotentialDifferenceValue = exp(testV)
                //Property test6: ISQ::ElectricPotentialDifferenceValue = power2(testV)
            }
            """
        )
        assertNoIssues()
    }

    @Test
    fun unitsInMultipleIterations() = testSession("Occurrences", "ISQ", "Math") {
        loadKerML("""package hello { 
            class world {
                feature density: ISQ::MassDensityValue = 1.0 [kg/l];
                feature r:       ISQ::LengthValue = 1000.0 km;
                feature volume:  ISQ::VolumeValue = 4.0/3.0 * r * r * r * Math::pi;
                feature mass:    ISQ::MassValue = density * volume;
                }
            }
            """)
        solver.propagate()
        assertNoIssues()
    }


    /** Does not copy up-propagated value into quantity field */
    @Test // @Ignore
    fun additionTrivial2() = testSession("Occurrences", "ISQ") {
        loadKerML(""" 
            package p { 
                class i {
                    feature p: ISQ::LengthValue = 1.0 m + 1.0 km;
                }
            }
        """)
        // p::i::p is wrongly identified in initialization --> resolveName issue?
        assertNoIssues()
        solver.propagate()
        // println("p="+global.resolveName<Expression>("p::i::p"))
        assertEquals(1001.0, global.resolveVar("p::i::p")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
    }

    /**
     * has Error on Unit subtraction
     * also value down has no unit.
     * FIX: in evalDown, unit is not converted if unitSpec is empty string.
     */
    @Test
    fun fail2() = testSession("ISQ") {
        loadKerML("""
            feature p: ISQ::LengthValue = 1.0 m;
            feature p2: ISQ::LengthValue = 1.0 km; 
            feature p3: ISQ::LengthValue = p + p2;
        """)
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
        // println(status.errors)
        // println(resolveName<Expression>("p::a::p3"))
        assertEquals(1001.0, global.resolveVar("p3")!!.vectorQuantity.getMinAsDouble(), 0.001)
        assertNoIssues()
        // Also check unit of down !!! it is not m.
    }

    /**
     *  Number of bugs fixed, remaining issue:
     *  If for properties i.e. p, p2, a unit becomes known later and is not in UnitSpec,
     *  it might not be considered properly in p3, hence problems with intersections????
     */
    @Test
    fun fail3() = testSession("Occurrences", "ISQ") {
        loadKerML("""
            package p { 
                class a { 
                    feature p: ISQ::LengthValue = 1.0 [m];
                    feature p2: ISQ::LengthValue = 1.0 [km];
                    feature p3: ISQ::LengthValue = p + p2;
                }
            }
        """)
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
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
    fun fail4() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature p:  Ranges::RealInRange {:>> range = "2 .. 4";}
            feature p2: ScalarValues::Real = p + 1.0;
            feature p3: ScalarValues::Boolean = ( p > p2 ).
        """)
        solver.propagate()

        val p = global.resolveVar("p")!!.vectorQuantity.aadd()
        val p2 = global.resolveVar("p2")!!.vectorQuantity.aadd()
        val p3 = global.resolveVar("p3")!!.vectorQuantity.bdd()

        assertEquals(2.0, p.min, 0.00001)
        assertEquals(3.0, p2.min, 0.00001)
        assertEquals("False", p3.toString())
        assertNoIssues()
    }


    /**
     * Problem: Intersection in an intermediate result is not computed on both way up and down.
     * Feature of V from up-propagation seems to get overwritten by down-propagated value.
     */
    @Test
    fun simplePhysicsExample() = testSession("ScalarValues", "Ranges") {
        loadKerML(""" 
            feature I: Ranges::RealInRange {:>> range = "9.9 .. 10.1";}
            feature R: Ranges::RealInRange {:>> range = "1.9 .. 2.1";} 
            feature V: ScalarValues::Real = I * R; 
            feature P: ScalarValues::Real = I * V; 
        """)
        assertEquals(9.9*1.9, global.resolveVar("V")!!.min(), 0.00001)
        assertEquals(10.1*2.1, global.resolveVar("V")!!.max(), 0.00001)
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
        // println("V = " + global.resolveName<Expression>("V") + " ")
        assertEquals(9.9*1.9, global.resolveVar("V")!!.min(), 0.00001)
        assertEquals(21.21, global.resolveVar("V")!!.max(), 0.00001)
    }


    @Test fun evalDownReal() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature a: ScalarValues::Real; 
            feature b: Ranges::RealInRange {:>> range = "3..5";}
            feature sum: Ranges::RealInRange = a+b {:>> range = "9..10";}""")
        solver.propagate()
        assertNoIssues()
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
    @Test fun evalDownInt() = testSession("ScalarValues", "Ranges") {
        loadKerML(""" 
           feature a: ScalarValues::Integer;
           feature b: Ranges::IntegerInRange {:>> range = "3..5";}
           feature sum: Ranges::IntegerInRange = a+b {:>> range = "9..10";}
           """)
        solver.propagate()
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
    @Test fun sumFunctionTestRangeExpr() = testSession("ScalarValues") {
        loadKerML("""
            feature i: ScalarValues::Real;
            feature s: ScalarValues::Real = 10.0;
            feature MAC_notb: ScalarValues::Real = sum_i( 0.0, 3.0, s*i );
        """)
        solver.propagate()
        assertEquals(60.0, global.resolveVar("MAC_notb")!!.aadd().getRange().min, 0.00001)
        assertEquals(60.0, global.resolveVar("MAC_notb")!!.aadd().getRange().max, 0.00001)
        assertNoIssues()
    }
}


