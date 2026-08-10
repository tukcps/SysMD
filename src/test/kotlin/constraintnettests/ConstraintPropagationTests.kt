package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.values.Range
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
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
            assertEquals("m", solver.getVariable("a")!!.vectorQuantity.unit.toString())
            // println(resolveName<Expression>("b")!!.quantity)
            // println(resolveName<Expression>("b")!!.quantity.valueIn("mm"))
            assertEquals(-1000.0, solver.getVariable("b")!!.vectorQuantity.valuesIn("cm")[0].asAadd().getRange().min, 0.00001)
            assertEquals(2000.0, solver.getVariable("b")!!.vectorQuantity.valuesIn("cm")[0].asAadd().getRange().max, 0.00001)
            assertEquals(-10.0, solver.getVariable("c")!!.min(), 0.00001)
            assertEquals(30.0, solver.getVariable("c")!!.max(), 0.00001)
            assertNoIssues()
        }
    }

    /** Units shall be converted when computing bottom-up, considering the prefix. */
    @Test
    fun evalUpNoOperation() {
        testSession("ISQ") {
            loadKerML("feature a: ISQ::LengthValue = 1.0 m;", Runlevel.VARIABLES)
            assertNoIssues()
            val a = solver.getVariable("a") !!
            a.ast!!.evalUpRec()
            assertEquals("m", solver.getVariable("a")!!.vectorQuantity.unit.toString())
            assertEquals(0.001, solver.getVariable("a")!!.vectorQuantity.valuesIn("km")[0].asAadd().getRange().min, 0.00001)
        }
    }


    @Test
    fun evalDownWithRange()  = testSession("ISQ", runlevel = Runlevel.ALL) {
        loadKerML("feature a: ISQ::ElectricPotentialDifferenceValue { :>> range = 1..20 [mV]; }")
        val a = solver.getVariable("a")
        assertNotNull(a)
        assertEquals("1..20 mV", a.vectorQuantity.toString())
        assertNoIssues()
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
        assertEquals(1.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals(2.0, solver.getVariable("c")!!.min(), 0.00001)
        assertEquals(10.0, solver.getVariable("c")!!.max(), 0.00001)
        assertNoIssues()
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
        assertEquals("1", solver.getVariable("a")!!.vectorQuantity.unit.toString())
        assertEquals(1.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(4.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals(100.0, solver.getVariable("c")!!.min(), 0.00001)
        assertEquals(200.0, solver.getVariable("c")!!.max(), 0.00001)
        assertNoIssues()
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
            assertEquals(1.0, solver.getVariable("b")!!.min(), 0.00001)
            assertEquals(1.0, solver.getVariable("b")!!.max(), 0.00001)
            assertNoIssues()
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
        assertEquals(0.26666, solver.getVariable("Baseplate::depth")!!.vectorQuantity.getMinAsDouble(), 0.001)
        assertEquals(0.26666, solver.getVariable("Baseplate::width")!!.vectorQuantity.getMinAsDouble(), 0.001)
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
        a.ast!!.evalDown()
        assertTrue(Range(-5.0..8.0) in (solver.getVariable("b")!!.vectorQuantity.values[0] as AADD).getRange())
        assertNoIssues()
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
        assertNoIssues()

        // Bug: The unit Kg is saved as g, but the value is kept in the original value.
        // Hence, 2..5 kg becomes 2..5 g.
        // In the SI unit system, kg would even be the correct base unit (?).
        assertEquals("0.002..0.05 kN", (solver.getVariable("a")!!.vectorQuantity.toString()))
        assertEquals(0.002, (solver.getVariable("a")!!.min()), 0.000001)
        assertEquals(0.05, (solver.getVariable("a")!!.max()), 0.000001)
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
        // Bug: The unit Kg is saved as g, but the value is kept in the original value.
        // Hence, 2..5 kg becomes 2..5 g.
        // In the SI unit system, kg would even be the correct base unit (?).
        assertEquals(2.0, solver.getVariable("a")!!.min(), 0.0001)
        assertEquals(50.0, solver.getVariable("a")!!.max(), 0.0001)
        assertEquals("kg m / s^2", solver.getVariable("a")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "${status.issues}")
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
        assertEquals(Range(-10.0..20.0), solver.getVariable("b")!!.rangeSpecs[0])
        assertEquals(7.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(8.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals(0, status.issues.size, "${status.issues}")
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
        assertNoIssues()
        solver.getVariable("a")!!.ast!!.evalDownRec()
        assertEquals(5.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("b")!!.max(), 0.00001)
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
        val a = solver.getVariable("a")!!
        assertTrue(a.aadd().getRange().isReals())
    }

    @Test
    fun evalUpWithScalars() = testSession("Ranges")  {
        loadKerML("""
            feature b: Ranges::RealInRange { :>> range=1.0; } 
            feature c: Ranges::RealInRange { :>> range=2.0; }
            feature d: Ranges::RealInRange { :>> range=3.0; }
            feature a: Ranges::RealInRange = b+c*d.
        """, Runlevel.ALL)
        val a = solver.getVariable("a")
        assertNotNull(a)
        assertEquals(7.0, (solver.getVariable("a")!!.aadd() as AADD.Leaf).central)
        assertEquals(7.0, solver.getVariable("a")!!.max(), 0.0001)
        assertEquals(1.0, solver.getVariable("b")!!.max(), 0.0001)
        assertEquals(2.0, solver.getVariable("c")!!.max(), 0.0001)
        assertEquals(3.0, solver.getVariable("d")!!.max(), 0.0001)
        assertNoIssues()
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
        assertEquals(1.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(1.0, solver.getVariable("b")!!.max(), 0.00001)
        assertNoIssues()
    }

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun solveWithUnitsSubtraction() = testSession("ISQ")  {
        loadKerML("""
            feature b: ISQ::LengthValue { :>> range = -100..200 [cm];}
            feature c: ISQ::LengthValue { :>> range = -10..30 [m];}
            feature a: ISQ::LengthValue = b-c {:>> range = -30.0 .. 60.0 [m];}
        """, Runlevel.ALL)
        assertEquals("m", solver.getVariable("a")!!.vectorQuantity.unit.toString())
        assertEquals(-30.0, solver.getVariable("a")!!.min(), 0.000001)
        assertEquals(12.0, solver.getVariable("a")!!.max(), 0.000001)
        assertEquals(-100.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(200.0, solver.getVariable("b")!!.max(), 0.00001)
        assertEquals(-10.0, solver.getVariable("c")!!.min(), 0.00001)
        assertEquals(30.0, solver.getVariable("c")!!.max(), 0.00001)
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with ranges. */
    @Test
    fun evalUpWithRangesTest() = testSession("Ranges", runlevel = Runlevel.ALL)  {
        loadKerML("feature b: Ranges::RealInRange {:>> range =1.0..2.0;}")
        loadKerML("feature c: Ranges::RealInRange {:>> range =2.0..3.0;}")
        loadKerML("feature d: Ranges::RealInRange {:>> range =3.0..4.0;}")
        loadKerML("feature a: Ranges::RealInRange = b+c*d;")
        val a = solver.getVariable("a")
        assertNotNull(a)
        assertEquals(7.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(14.0, solver.getVariable("a")!!.max(), 0.00001)
        // central value depends on approximation schemes; might cause incorrect fault iff changed.
        // only outside tests display((displayTree("a", p.getVar("a").value)))
        // println(resolveName<Expression>("a")!!.quantity.value.toIteString())
        assertTrue( (solver.getVariable("a")!!.aadd() as AADD.Leaf).value.central in 10.0..12.0)
        assertNoIssues()
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
            assertEquals(33.33333333333334, solver.getVariable("a")!!.vectorQuantity.getMinAsDouble(), 0.00000001)
            assertEquals(200.0, solver.getVariable("a")!!.vectorQuantity.getMaxAsDouble(), 0.00000001)
            assertEquals("1 / s", solver.getVariable("a")!!.vectorQuantity.unit.toString())
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
        assertEquals(20.0, solver.getVariable("a")!!.min(), 0.0001)
        assertEquals(120.0, solver.getVariable("a")!!.max(), 0.0001)
        assertEquals("m^3", solver.getVariable("a")!!.vectorQuantity.unit.toString())
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
            assertEquals(4003.002, solver.getVariable("a")!!.max(), 0.0001)
            assertEquals(1002.001, solver.getVariable("a")!!.min(), 0.0001)
            assertEquals("m", solver.getVariable("a")!!.vectorQuantity.unit.toString())
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
            assertEquals(-6.0, solver.getVariable("a")!!.min(), 0.00001)
            assertEquals(-1.0, solver.getVariable("a")!!.max(), 0.00001)
            assertEquals("m", solver.getVariable("a")!!.vectorQuantity.unit.toString())
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
            assertEquals("True", solver.getVariable("d")!!.bool().toString())
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
            assertEquals("False", solver.getVariable("d")!!.bool().toString())
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
            assertEquals("False", solver.getVariable("d")!!.bool().toString())
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
            assertEquals("True", solver.getVariable("d")!!.bool().toString())
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
            package p { 
                class i {
                    feature p: ISQ::LengthValue = 1.0 m + 1.0 km;
                }
            }
        """)
        // p::i::p is wrongly identified in initialization --> resolveName issue?
        assertNoIssues()
        solver.propagate()
        assertEquals(1001.0, solver.getVariable("p::i::p")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
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
        """)
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
        assertEquals(1001.0, solver.getVariable("p3")!!.vectorQuantity.getMinAsDouble(), 0.001)
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
        assertEquals(1001.0, solver.getVariable("p::a::p3")!!.vectorQuantity.getMinAsDouble(), 0.001)
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
        assertEquals(9.9*1.9, solver.getVariable("V")!!.min(), 0.00001)
        assertEquals(10.1*2.1, solver.getVariable("V")!!.max(), 0.00001)
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
        assertEquals(9.9*1.9, solver.getVariable("V")!!.min(), 0.00001)
        assertEquals(21.21, solver.getVariable("V")!!.max(), 0.00001)
    }

    @Test
    fun multiplicationNegative() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -10.0 .. -5.0;}
            feature c: Ranges::RealInRange {:>> range = -3.0 .. -2.0;}
            feature a: ScalarValues::Real = b * c;
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(10.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(30.0, solver.getVariable("a")!!.max(), 0.00001)
    }

    @Test
    fun multiplicationMixed() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -2.0 .. 3.0;}
            feature c: Ranges::RealInRange {:>> range = -5.0 .. 1.0;}
            feature a: ScalarValues::Real = b * c;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(-15.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(10.0, solver.getVariable("a")!!.max(), 0.00001)
    }

    @Test
    fun evalDownMultiplicationMixed() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -3.0 .. 4.0;}
            feature c: Ranges::RealInRange {:>> range = 1.0 .. 2.0;}
            feature a: Ranges::RealInRange = b * c {:>> range = 2.0 .. 6.0;}
        """)
        solver.propagate()
        assertNoIssues()
        val b = solver.getVariable("b")!!
        assertEquals(1.0, b.min(), 0.00001)
        assertEquals(4.0, b.max(), 0.00001)
    }

    @Test
    fun divisionNegative() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -20.0 .. -10.0;}
            feature c: Ranges::RealInRange {:>> range = -5.0 .. -2.0;}
            feature a: ScalarValues::Real = b / c;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(2.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(10.0, solver.getVariable("a")!!.max(), 0.00001)
    }

    @Test
    fun divisionMixed() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = 1.0 .. 10.0;}
            feature c: Ranges::RealInRange {:>> range = -2.0 .. 5.0;}
            feature a: ScalarValues::Real = b / c;
        """)
        solver.propagate()
        assertNoIssues()
        val a = solver.getVariable("a")!!
        assertEquals(Double.NEGATIVE_INFINITY, a.min(), 0.00001)
        assertEquals(Double.POSITIVE_INFINITY, a.max(), 0.00001)
    }

    @Test
    fun evalUpAdditionNegative() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -5.0 .. -2.0;}
            feature c: Ranges::RealInRange {:>> range = -10.0 .. -3.0;}
            feature d: Ranges::RealInRange {:>> range = -1.0 .. -1.0;}
            feature a: Ranges::RealInRange = b + c + d;
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(-16.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(-6.0, solver.getVariable("a")!!.max(), 0.00001)
    }

    @Test
    fun evalUpSubtractionNegative() = testSession("Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range = -5.0 .. -2.0;}
            feature c: Ranges::RealInRange {:>> range = -10.0 .. -3.0;}
            feature d: Ranges::RealInRange {:>> range = -1.0 .. -1.0;}
            feature a: Ranges::RealInRange = b - c - d;
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(-1.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(9.0, solver.getVariable("a")!!.max(), 0.00001)
    }
}
