package constraintnettests

import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.util.Assertions.assertSafeInclusion
import util.*
import util.mockup.loadKerML
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConstraintPropagationTests {

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun evalDownWithUnitsAddition() {
        testSession("ISQ") {
            // -30 .. 50 m == -1000..2000 cm + -10 .. 30 m
            // => -20 .. 50 m == ...
            loadKerML("feature b: ISQ::LengthValue  { :>> range = -1000 .. 2000 [cm] ;}")
            loadKerML("feature c: ISQ::LengthValue  { :>> range = -10.0..30.0 [m]; }")
            loadKerML("feature d: ISQ::LengthValue  { :>> range = 10.0..10 [m]; }")
            loadKerML("feature a: ISQ::LengthValue  = b+c+d {:>> range = -30.0 .. 50.0 [m];}")
            solver.propagate()
            assertEquals("m", solver.variable("a").vectorQuantity.unit.toString())
            // println(resolveName<Expression>("b")!!.quantity)
            // println(resolveName<Expression>("b")!!.quantity.valueIn("mm"))
            assertBounds(-1000.0 .. 2000.0, solver.variable("b"), unit = "cm")
            assertBounds(-10.0 .. 30.0, solver.variable("c"))
            assertNoIssues()
        }
    }

    /** Units shall be converted when computing bottom-up, considering the prefix. */
    @Test
    fun evalUpNoOperation() {
        testSession("ISQ") {
            loadKerML("feature a: ISQ::LengthValue = 1.0 m;", Runlevel.VARIABLES)
            solver.propagate()
            assertNoIssues()
            val a = solver.getVariable("a") !!
            a.ast!!.evalUpRec()
            assertEquals("m", solver.variable("a").vectorQuantity.unit.toString())
            assertBounds(0.001, solver.variable("a").vectorQuantity.valuesIn("km")[0].asAadd())
        }
    }


    @Test
    fun evalDownWithRange()  = testSession("ISQ", runlevel = Runlevel.ALL) {
        loadKerML("feature a: ISQ::ElectricPotentialDifferenceValue { :>> range = 1..20 [mV]; }")
        val a = solver.variable("a")
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0..20.0, a, unit = "mV")
    }

    /**
     * ConstNet shall support ranges in dependencies.
     **/
    @Test
    fun evalDownWithUnitsMultiplication()  = testSession( "Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange  {:>> range = 1.0 .. 30.0;} // 1..5
            feature c: Ranges::RealInRange  {:>> range = 2.0 .. 20.0;} // 2..10
            feature a: Ranges::RealInRange  = b*c {:>> range = 9.0 .. 10.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0 .. 5.0, solver.variable("b"))
        assertBounds(2.0 .. 10.0, solver.variable("c"))
    }

    /**
     * ConstNet shall compute top-down with scalars.
     * Division should here avoid including division by zero, which is a separate test case.
     */
    @Test
    fun evalDownWithUnitsDivision() = testSession("ISQ") {
        loadKerML("""
            feature b: ISQ::LengthValue  {:>> range = 1..20 [m]; }                         // 0.5..4, reduced to 1..4 m
            feature c: ISQ::LengthValue  {:>> range = 100 .. 200 [cm]; }    // 1..2 m
            feature a: Ranges::RealInRange  = b/c { :>> range = 1.0 .. 2.0; } 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertEquals("1", solver.variable("a").vectorQuantity.unit.toString())
        assertBounds(1.0 .. 4.0, solver.variable("b"), unit = "m")
        assertBounds(100.0 .. 200.0, solver.variable("c"), unit = "cm")
    }


    /**
     * ConstNet shall propagate top-down with scalars and the special case model.builder.Reals.
     **/
    @Test
    fun evalDownWithScalarsRealsTest() {
        testSession("Ranges") {
            loadKerML("""
                feature b: ScalarValues::Real;
                feature c: ScalarValues::Real = 2.0;
                feature d: ScalarValues::Real = 3.0;
                feature a: Ranges::RealInRange  = b+c*d {:>> range = 7.0 .. 7.0;} 
            """, Runlevel.ALL)
            solver.propagate()
            assertNoIssues()
            assertBounds(1.0, solver.variable("b"))
        }
    }

    @Test
    fun evalDownMultiplicationTest() = testSession("ISQ") {
        loadKerML("""
            classifier Baseplate {
                feature width: ISQ::LengthValue  { :>> range = 100 .. 300 [mm]; } 
                feature depth: ISQ::LengthValue  { :>> range = 100 .. 300 [mm]; } 
                feature area:  ISQ::AreaValue   = width * depth { :>> range = 800 .. 1000 [cm^2];} 
            }
        """, Runlevel.ALL)
        assertNoIssues()
        assertBounds(0.2666666666666666..0.30000000000000004, solver.variable("Baseplate::depth"))
        assertBounds(0.2666666666666666..0.30000000000000004, solver.variable("Baseplate::width"))
        assertNoIssues()
    }


    /**
     * We check that an evaluation downwards restricts the 'builder.range' of properties.
     */
    @Test
    fun evalDownWithRangesTest() = testSession("Ranges")  {
        loadKerML("""
            feature b: Ranges::RealInRange  {:>> range = -10.0 .. 20.0;} // Larger than needed to get results
            feature c: Ranges::RealInRange  {:>> range = 2.0 .. 3.0;}
            feature d: Ranges::RealInRange  {:>> range = 3.0 .. 4.0;}
            feature a: Ranges::RealInRange  = b+c*d {:>> range = 7 .. 14;} 
        """, Runlevel.VARIABLES) // b hence can only be from -5 to 8.
        val a = solver.getVariable("a") !!
        a.ast!!.evalDownRec()
        solver.propagate()
        assertNoIssues()
        assertBounds(-5.0..8.0, solver.variable("b"))
    }


    /** ConstNet shall compute bottom-up with ranges and units. */
    @Test
    fun evalUpWithDefinedResult() = testSession("ISQ")  {
        loadKerML("""
            feature b: ISQ::LengthValue {:>> range = 10.0 .. 100.0 [m]; }
            feature c: ISQ::MassValue {:>> range = 2.0..5.0 [kg]; }
            feature d: Quantities::ScalarQuantityValue { :>> range = 10.0 [s^2];} 
            feature a: ISQ::ForceValue = b*c/d { :>> range = *..* [kN];}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()

        // a = b * c / d = (10..100 m) * (2..5 kg) / 10 s^2 = 2..50 N = 0.002..0.05 kN
        assertEquals("0.002..0.05 kN", (solver.variable("a").vectorQuantity.toString()))
        assertBounds(2.0 .. 50.0, solver.variable("a"))
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with ranges and units. */
    @Test
    fun evalUpWithNoDefinedResult() = testSession("ISQ")  {
        loadKerML("""
            feature b: ISQ::LengthValue {:>> range = 10.0 .. 100.0 [m];}
            feature c: ISQ::MassValue {:>> range = 2.0..5.0 [kg];}
            feature d: Quantities::ScalarQuantityValue { :>> range = 10.0 [s^2];}
            feature a: ISQ::ForceValue = b*c/d;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        // Bug: The unit Kg is saved as g, but the value is kept in the original value.
        // Hence, 2..5 kg becomes 2..5 g.
        // In the SI unit system, kg would even be the correct base unit (?).
        assertBounds(2.0 .. 50.0, solver.variable("a"))
        assertEquals("kg m / s^2", solver.variable("a").vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /*  ################### EVAL DOWN SECTION ################### */

    /**
     * EvalDown from left side constraint to right side
     *  7..7 = -10..20 becomes to 7..7 = 7..7
     *  However, the constraint -10.0..20 shall be maintained as it is a separate field
     *  of the property.
     */
    @Test
    fun evalDownLeftToRightSideTest() = testSession("Ranges")  {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -10.0.. 20.0;}
            feature a: Ranges::RealInRange = b {:>> range = 7.0 .. 8.0;}
        """, Runlevel.SOLVED)
        val a = solver.getVariable("a") !!
        a.ast!!.evalDownRec()
        solver.propagate()
        assertNoIssues()
        assertBounds(-10.0..20.0, solver.variable("b").rangeSpecs.single())
        assertBounds(7.0..8.0, solver.variable("b"))
    }

    /**
     * ConstNet shall compute top-down with scalars.
     * a:7 = b:-100..200 + c:2
     * shall evaluate to a:7 = b:5+c:2
     */
    @Test
    fun evalDownOneStepTest() = testSession("Ranges")  {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -100.0..200.0;}
            feature c: Ranges::RealInRange {:>> range = 2..2;}
            feature a: Ranges::RealInRange = b+c {:>> range = 7.0 .. 7.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        solver.variable("a").ast!!.evalDownRec()
        assertBounds(5.0 .. 5.0, solver.variable("b"))
        assertNoIssues()
    }

    /* ################### EVAL UP SECTION ################### */

    /** ConstNet shall compute bottom-up with scalars, and overflow should result in inf results. */
    @Test
    fun evalUpWithScalarsNoOverflowTest() = testSession("ScalarValues")  {
        loadKerML("""
            feature b: ScalarValues::Real; 
            feature c: ScalarValues::Real; 
            feature d: ScalarValues::Real;
            feature a: ScalarValues::Real = b+c*d.
        """, Runlevel.SOLVED)
        val a = solver.variable("a")
        assertBounds(Double.NEGATIVE_INFINITY..Double.POSITIVE_INFINITY, a)
    }

    @Test
    fun evalUpWithScalars() = testSession("Ranges")  {
        loadKerML("""
            feature b: Ranges::RealInRange { :>> range=1.0; } 
            feature c: Ranges::RealInRange { :>> range=2.0; }
            feature d: Ranges::RealInRange { :>> range=3.0; }
            feature a: Ranges::RealInRange = b+c*d.
        """, Runlevel.ALL)
        val a = solver.variable("a")
        solver.propagate()
        assertNoIssues()
        assertBounds(7.0, solver.variable("a"))
        assertBounds(1.0, solver.variable("b"))
        assertBounds(2.0, solver.variable("c"))
        assertBounds(3.0, solver.variable("d"))
    }

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun evalDownWithScalarsTest() = testSession("Ranges")  {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -10..20;}
            feature c: Ranges::RealInRange {:>> range = 2.0; }
            feature d: Ranges::RealInRange {:>> range = 3.0; }
            feature a: Ranges::RealInRange = b+c*d {:>> range = 7.0 .. 7.0;}
        """, Runlevel.SOLVED)
        val a = solver.getVariable("a") !!
        a.ast!!.evalDownRec()
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0 .. 1.0, solver.variable("b"))
    }

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun solveWithUnitsSubtraction() = testSession("ISQ")  {
        loadKerML("""
            feature b: ISQ::LengthValue { :>> range = -100..200 [cm];}
            feature c: ISQ::LengthValue { :>> range = -10..30 [m];}
            feature a: ISQ::LengthValue = b-c {:>> range = -30.0 .. 60.0 [m];}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertEquals("m", solver.variable("a").vectorQuantity.unit.toString())
        assertBounds(-30.0 .. 12.0, solver.variable("a"))
        assertBounds(-100.0 .. 200.0, solver.variable("b"), unit = "cm")
        assertBounds(-10.0 .. 30.0, solver.variable("c"))
    }

    /** ConstNet shall compute bottom-up with ranges. */
    @Test
    fun evalUpWithRangesTest() = testSession("Ranges", runlevel = Runlevel.ALL)  {
        loadKerML("feature b: Ranges::RealInRange {:>> range =1.0..2.0;}")
        loadKerML("feature c: Ranges::RealInRange {:>> range =2.0..3.0;}")
        loadKerML("feature d: Ranges::RealInRange {:>> range =3.0..4.0;}")
        loadKerML("feature a: Ranges::RealInRange = b+c*d;")
        val a = solver.variable("a")
        solver.propagate()
        assertNoIssues()
        assertBounds(7.0 .. 14.0, solver.variable("a"))
        // central value depends on approximation schemes; might cause incorrect fault iff changed.
        // only outside tests display((displayTree("a", p.getVar("a").value)))
        // println(resolveName<Expression>("a")!!.quantity.value.toIteString())
    }

    /** ConstNet shall compute bottom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsDivision() {
        testSession("ISQ")  {
            loadKerML("feature b: ISQ::LengthValue { :>> range = 1.0..2.0 [km];}")
            loadKerML("feature c: ISQ::LengthValue {:>> range = 2.0..3.0 [m];}")
            loadKerML("feature d: ISQ::DurationValue {:>> range = 5.0..10.0 [s];}")
            loadKerML("feature a: ISQ::FrequencyValue = b/c/d;")
            solver.propagate()
            assertBounds(33.33333333333334..200.0, solver.variable("a"))
            assertEquals("1 / s", solver.variable("a").vectorQuantity.unit.toString())
            assertNoIssues()
        }
    }

    /** ConstNet shall compute buttom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsMultiplication() = testSession("ISQ")  {
        loadKerML("""
            feature d: ISQ::LengthValue { :>> range = 10.0 .. 20.0 [mm];}
            feature c: ISQ::LengthValue { :>> range = 2.0 .. 3.0 [m];}
            feature b: ISQ::LengthValue { :>> range = 1.0 .. 2.0 [km];}
            feature a: ISQ::VolumeValue = b*c*d;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(20.0 .. 120.0, solver.variable("a"))
        assertEquals("m^3", solver.variable("a").vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute buttom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsAddition() {
        testSession("ISQ")  {
            loadKerML("feature b: ISQ::LengthValue {:>> range = 1.0..2.0 [mm];}")
            loadKerML("feature c: ISQ::LengthValue { :>> range =2.0..3.0 [m];}")
            loadKerML("feature d: ISQ::LengthValue{ :>> range = 1.0..4.0 [km];}")
            loadKerML("feature a: ISQ::LengthValue = b+c+d;")
            solver.propagate()
            assertBounds(1002.001..4003.002, solver.variable("a"), unit = "m")
            assertEquals("m", solver.variable("a").vectorQuantity.unit.toString())
            assertNoIssues()
        }
    }

    /** ConstNet shall compute bottom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsSubtraction() {
        testSession("ISQ")  {
            loadKerML("feature b: ISQ::LengthValue = [1.0 .. 2.0] [m];")
            loadKerML("feature c: ISQ::LengthValue = [2.0 .. 3.0] [m];")
            loadKerML("feature d: ISQ::LengthValue = [1.0 .. 4.0] [m];")
            loadKerML("feature a: ISQ::LengthValue = b-c-d;")
            assertNoIssues()
            solver.propagate()
            assertBounds(-6.0 .. -1.0, solver.variable("a"))
            assertEquals("m", solver.variable("a").vectorQuantity.unit.toString())
            assertNoIssues()
        }
    }

    @Test
    fun notEquals() {
        testSession("ISQ")  {
            loadKerML("feature b: ISQ::LengthValue = [1.0 .. 2.0] [m];")
            loadKerML("feature c: ISQ::LengthValue = [3.0 .. 4.0] [m];")
            loadKerML("feature d: ScalarValues::Boolean = c!=b;")
            assertNoIssues()
            solver.propagate()
            assertEquals("True", solver.variable("d").bool().toString())
            assertNoIssues()
        }
    }

    @Test
    fun notEquals2() {
        testSession("ScalarValues")  {
            loadKerML("""
                feature b: ScalarValues::Integer = 1 ;
                feature c: ScalarValues::Integer = 1;
                feature d: ScalarValues::Boolean = b!=c;
                """)
            assertNoIssues()
            solver.propagate()
            assertEquals("False", solver.variable("d").bool().toString())
            assertNoIssues()
        }
    }

    @Test
    fun notEquals3() {
        testSession("ISQ")  {
            loadKerML("""
                feature b: ISQ::LengthValue = 1.0 m ;
                feature c: ISQ::LengthValue = 1.0 m;
                feature d: ScalarValues::Boolean = b!=c;
            """)
            assertNoIssues()
            solver.propagate()
            assertEquals("False", solver.variable("d").bool().toString())
            assertNoIssues()
        }
    }

    @Test
    fun equals3() {
        testSession("ISQ")  {
            loadKerML("feature b: ISQ::LengthValue = 1.0 m ;")
            loadKerML("feature c: ISQ::LengthValue = 1.0 m;")
            loadKerML("feature d: ScalarValues::Boolean = b==c;")
            assertNoIssues()
            solver.propagate()
            assertEquals("True", solver.variable("d").bool().toString())
            assertNoIssues()
        }
    }

    /**
     * Tests that a value can be assigned to ISQ units including non-SI units.
     * Previously it was not possible to assign a value without a formula to a non-SI unit.
     */
    @Test
    fun unitTransformTest() = testSession("ISQ") {
        loadKerML(input = """
            // It is not possible to assign a value without a formula to a non-SI unit
            //only test1 works
            type Test :> Base::Anything {
                feature test1: ISQ::ElectricCurrentValue = 1.0 A;
                feature test2: ISQ::ForceValue = 1.0 N;
                feature test3: ISQ::ResistanceValue = 1.0 [Ohm];
                feature test4: Quantities::ScalarQuantityValue( * [Ohm m]) = 1.0 [Ohm m];
            }
        """, Runlevel.ALL)
        assertNoIssues()
    }

    /**
     * Tests that ISQ units are propagated correctly across multiple solver iterations.
     * Uses mass density, radius and volume in a sphere-volume computation.
     */
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
    @Test
    fun additionTrivial2() = testSession("Occurrences", "ISQ") {
        loadKerML(""" 
            feature p: ISQ::LengthValue = 1.0 m + 1.0 km;
        """, Runlevel.ALL)
        assertNoIssues()
        assertBounds(1001.0, solver.variable("p"))
    }

    /**
     * Tests LengthValue addition between m and km values at the top-level namespace.
     * FIX: in evalDown, unit is not converted if unitSpec is empty string.
     */
    @Test
    fun fail2() = testSession("ISQ") {
        loadKerML("""
            feature p: ISQ::LengthValue = 1.0 m;
            feature p2: ISQ::LengthValue = 1.0 km; 
            feature p3: ISQ::LengthValue = p + p2;
        """, Runlevel.ALL)
        assertNoIssues()
        assertBounds(1001.0, solver.variable("p3"))
        assertNoIssues()
    }

    /**
     * Tests LengthValue addition between m and km values inside a package/class.
     * If for properties p, p2 a unit becomes known later and is not in UnitSpec,
     * it might not be considered properly in p3.
     */
    @Test
    fun fail3() = testSession("Occurrences", "ISQ") {
        loadKerML("""
            package p { 
                classifier a { 
                    feature p: ISQ::LengthValue = 1.0 [m];
                    feature p2: ISQ::LengthValue = 1.0 [km];
                    feature p3: ISQ::LengthValue = p + p2;
                }
            }
        """, Runlevel.ALL)
        assertNoIssues()
        assertBounds(1001.0, solver.variable("p::a::p3"))
    }

    /**
     * Tests that intersection in an intermediate result is computed on both up and down propagation.
     * Feature V from up-propagation should not be overwritten by the down-propagated value.
     * Uses I*R = V, I*V = P with ranges on I and R.
     */
    @Test
    fun simplePhysicsExample() = testSession("Ranges") {
        loadKerML(""" 
            feature I: Ranges::RealInRange {:>> range = 9.9 .. 10.1;}
            feature R: Ranges::RealInRange {:>> range = 1.9 .. 2.1;} 
            feature V: ScalarValues::Real = I * R; 
            feature P: ScalarValues::Real = I * V; 
        """, Runlevel.ALL)
        assertSafeInclusion(9.9 * 1.9..10.1 * 2.1, solver.variable("V").aadd(), 0.00001)
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
        assertSafeInclusion(9.9 * 1.9..21.21, solver.variable("V").aadd(), 0.00001)
    }

    @Test
    fun multiplicationNegative() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -10.0 .. -5.0;}
            feature c: Ranges::RealInRange {:>> range = -3.0 .. -2.0;}
            feature a: ScalarValues::Real = b * c;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(10.0 .. 30.0, solver.variable("a"))
    }

    @Test
    fun multiplicationMixed() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -2.0 .. 3.0;}
            feature c: Ranges::RealInRange {:>> range = -5.0 .. 1.0;}
            feature a: ScalarValues::Real = b * c;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(-15.0 .. 10.0, solver.variable("a"))
    }

    @Test
    fun evalDownMultiplicationMixed() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -3.0 .. 4.0;}
            feature c: Ranges::RealInRange {:>> range = 1.0 .. 2.0;}
            feature a: Ranges::RealInRange = b * c {:>> range = 2.0 .. 6.0;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val b = solver.variable("b")
        assertBounds(1.0 .. 4.0, b)
    }

    @Test
    fun divisionNegative() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -20.0 .. -10.0;}
            feature c: Ranges::RealInRange {:>> range = -5.0 .. -2.0;}
            feature a: ScalarValues::Real = b / c;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(2.0 .. 10.0, solver.variable("a"))
    }

    @Test
    fun divisionMixed() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = 1.0 .. 10.0;}
            feature c: Ranges::RealInRange {:>> range = -2.0 .. 5.0;}
            feature a: ScalarValues::Real = b / c;
        """, Runlevel.ALL)
        assertNoIssues()
        val a = solver.variable("a")
        assertSafeInclusion(Double.NEGATIVE_INFINITY..Double.POSITIVE_INFINITY, a.aadd(), 0.00001)
    }

    @Test
    fun evalUpAdditionNegative() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -5.0 .. -2.0;}
            feature c: Ranges::RealInRange {:>> range = -10.0 .. -3.0;}
            feature d: Ranges::RealInRange {:>> range = -1.0 .. -1.0;}
            feature a: Ranges::RealInRange = b + c + d;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(-16.0 .. -6.0, solver.variable("a"))
    }

    @Test
    fun evalUpSubtractionNegative() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -5.0 .. -2.0;}
            feature c: Ranges::RealInRange {:>> range = -10.0 .. -3.0;}
            feature d: Ranges::RealInRange {:>> range = -1.0 .. -1.0;}
            feature a: Ranges::RealInRange = b - c - d;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(-1.0 .. 9.0, solver.variable("a"))
    }

    /**
     * Regression tests for AstBinOp GT (>) evalDown false branch:
     * (l > r) == false means l <= r.
     * When l in [4..10], r in [2..6], and (l > r) is false:
     * l must be <= 6, so l in [4..6], and r must be >= 4, so r in [4..6].
     */
    @Test
    fun binOpGTFalseBranchRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature l: Ranges::IntegerInRange {:>> range = 4 .. 10;}
            feature r: Ranges::IntegerInRange {:>> range = 2 .. 6;}
            feature cmp: ScalarValues::Boolean = l > r;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val cmp = solver.variable("cmp")
        cmp.boolSpecs = mutableListOf(io.github.tukcps.aadd.values.bool.XBool.False)
        cmp.vectorQuantity = VectorQuantity(builder.Bool.False)
        cmp.ast!!.evalDownRec()
        val l = solver.variable("l")
        val r = solver.variable("r")
        assertBounds(4L .. 6L, l)
        assertBounds(4L .. 6L, r)
    }

    /**
     * Regression tests for AstBinOp GE (>=) evalDown false branch:
     * (l >= r) == false means l < r, so l <= r - 1.
     * When l in [1..10], r in [5..7], and (l >= r) is false:
     * l must be < 7, so maxL = min(10, 7-1) = 6.
     */
    @Test
    fun binOpGEFalseBranchStrictInequalityRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature l: Ranges::IntegerInRange {:>> range = 1 .. 10;}
            feature r: Ranges::IntegerInRange {:>> range = 5 .. 7;}
            feature cmp: ScalarValues::Boolean = l >= r;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val cmp = solver.variable("cmp")
        cmp.boolSpecs = mutableListOf(io.github.tukcps.aadd.values.bool.XBool.False)
        cmp.vectorQuantity = VectorQuantity(builder.Bool.False)
        cmp.ast!!.evalDownRec()
        val l = solver.variable("l")
        val r = solver.variable("r")
        assertBounds(1L .. 6L, l)
        assertBounds(5L .. 7L, r)
    }

    /**
     * Regression test for AstBinOp EE (==) evalDown with Reals:
     * Intersecting two real ranges when equality is true.
     */
    @Test
    fun binOpEERealIntersectionRegressionTest() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = 2.0 .. 8.0;}
            feature b: Ranges::RealInRange {:>> range = 5.0 .. 12.0;}
            feature eq: ScalarValues::Boolean = a == b;
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val eq = solver.variable("eq")
        eq.boolSpecs = mutableListOf(io.github.tukcps.aadd.values.bool.XBool.True)
        eq.vectorQuantity = VectorQuantity(builder.Bool.True)
        eq.ast!!.evalDownRec()
        val a = solver.variable("a")
        val b = solver.variable("b")
        assertBounds(5.0 .. 8.0, a)
        assertBounds(5.0 .. 8.0, b)
    }
}
