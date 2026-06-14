package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.values.Range
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession


class ConstraintPropagationTests {

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun evalDownWithUnitsAddition() {
        testSession("ISQ", "Ranges") {
            // -30 .. 50 m == -1000..2000 cm + -10 .. 30 m
            // => -20 .. 50 m == ...
            loadKerML("feature b: ISQ::LengthValue  {:>> unit = \"cm\"; :>> range = \"-1000 .. 2000\";}")
            loadKerML("feature c: ISQ::LengthValue  {:>> range = \"-10.0..30.0\";}")
            loadKerML("feature d: ISQ::LengthValue  {:>> range = \"10.0..10\";}")
            loadKerML("feature a: ISQ::LengthValue  = b+c+d {:>> range = \"-30.0 .. 50.0\";}")
            solver.propagate()
            assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            // println(resolveName<Expression>("b")!!.quantity)
            // println(resolveName<Expression>("b")!!.quantity.valueIn("mm"))
            assertEquals(-1000.0, global.resolveVar("b")!!.vectorQuantity.valuesIn("cm")[0].asAadd().getRange().min, 0.00001)
            assertEquals(2000.0, global.resolveVar("b")!!.vectorQuantity.valuesIn("cm")[0].asAadd().getRange().max, 0.00001)
            assertEquals(-10.0, global.resolveVar("c")!!.min(), 0.00001)
            assertEquals(30.0, global.resolveVar("c")!!.max(), 0.00001)
            assertNoIssues()
        }
    }

    /** Units shall be converted when computing bottom-up, considering the prefix. */
    @Test
    fun evalUpNoOperation() {
        testSession("ISQ") {
            loadKerML("feature a: ISQ::LengthValue = 1.0 m;", Runlevel.VARIABLES)
            assertNoIssues()
            val a = global.resolveVar("a") !!
            a.ast!!.evalUpRec()
            assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            assertEquals(0.001, global.resolveVar("a")!!.vectorQuantity.valuesIn("km")[0].asAadd().getRange().min, 0.00001)
        }
    }


    @Test
    fun evalDownWithRange()  = testSession("ISQ", "Ranges", runlevel = Runlevel.ALL) {
        loadKerML("feature a: ISQ::ElectricPotentialDifferenceValue {:>> unit = \"mV\"; :>> range = \"1..20\";}")
        val a = global.resolveVar("a")
        assertNotNull(a)
        assertEquals("1..20 mV", a!!.vectorQuantity.toString())
        assertNoIssues()
    }

    /**
     * ConstNet shall support ranges in dependencies.
     **/
    @Test
    fun evalDownWithUnitsMultiplication()  = testSession( "Ranges") {
        loadKerML("""
            feature b: Ranges::RealInRange  {:>> range = "1.0 .. 30.0";} // 1..5
            feature c: Ranges::RealInRange  {:>> range = "2.0 .. 20.0";} // 2..10
            feature a: Ranges::RealInRange  = b*c {:>> range = "9.0 .. 10.0";}""")
        solver.propagate()
        assertEquals(1.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(5.0, global.resolveVar("b")!!.max(), 0.00001)
        assertEquals(2.0, global.resolveVar("c")!!.min(), 0.00001)
        assertEquals(10.0, global.resolveVar("c")!!.max(), 0.00001)
        assertNoIssues()
    }

    /**
     * ConstNet shall compute top-down with scalars.
     * Division should here avoid including division by zero, which is a separate test case.
     */
    @Test
    fun evalDownWithUnitsDivision() = testSession("ISQ", "Ranges") {
        loadKerML("""
            feature b: ISQ::LengthValue  {:>> range = "1..20";}                         // 0.5..4, reduced to 1..4 m
            feature c: ISQ::LengthValue  {:>> unit = "cm"; :>> range = "100 .. 200";}    // 1..2 m
            feature a: Ranges::RealInRange  = b/c {:>> range = "1.0 .. 2.0";} 
        """, Runlevel.ALL)
        assertEquals("1", global.resolveVar("a")!!.vectorQuantity.unit.toString())
        assertEquals(1.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(4.0, global.resolveVar("b")!!.max(), 0.00001)
        assertEquals(100.0, global.resolveVar("c")!!.min(), 0.00001)
        assertEquals(200.0, global.resolveVar("c")!!.max(), 0.00001)
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
                feature a: Ranges::RealInRange  = b+c*d {:>> range = "7.0 .. 7.0";} 
            """, Runlevel.ALL)
            assertEquals(1.0, global.resolveVar("b")!!.min(), 0.00001)
            assertEquals(1.0, global.resolveVar("b")!!.max(), 0.00001)
            assertNoIssues()
        }
    }

    @Test
    fun evalDownMultiplicationTest() = testSession("Occurrences", "Links","ISQ", "Ranges") {
        loadKerML("""
            class Baseplate {
                feature width: ISQ::LengthValue  {:>> unit = "mm"; :>> range = "100 .. 300";} 
                feature depth: ISQ::LengthValue  {:>> unit = "mm"; :>> range = "100 .. 300";} 
                feature area:  ISQ::AreaValue   = width * depth {:>> unit = "cm^2"; :>> range = "800 .. 1000";} 
            }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(0.26666, global.resolveVar("Baseplate::depth")!!.vectorQuantity.getMinAsDouble(), 0.001)
        assertEquals(0.26666, global.resolveVar("Baseplate::width")!!.vectorQuantity.getMinAsDouble(), 0.001)
        assertNoIssues()
    }


    /**
     * We check that an evaluation downwards restricts the 'builder.range' of properties.
     */
    @Test
    fun evalDownWithRangesTest() = testSession("Ranges")  {
        loadKerML("""
            feature b: Ranges::RealInRange  {:>> range = "-10.0 .. 20.0";} // Larger than needed to get results
            feature c: Ranges::RealInRange  {:>> range = "2.0 .. 3.0";}
            feature d: Ranges::RealInRange  {:>> range = "3.0 .. 4.0";}
            feature a: Ranges::RealInRange  = b+c*d {:>> range = "7 .. 14";} 
        """, Runlevel.VARIABLES) // b hence can only be from -5 to 8.
        val a = global.resolveVar("a") !!
        a.ast!!.evalDown()
        assertTrue(Range(-5.0..8.0) in (global.resolveVar("b")!!.vectorQuantity.values[0] as AADD).getRange())
        assertNoIssues()
    }


    /** ConstNet shall compute bottom-up with ranges and units. */
    @Test
    fun evalUpWithDefinedResult() = testSession("ISQ", "Ranges")  {
        loadKerML("""
            feature b: ISQ::LengthValue {:>> range = "10.0 .. 100.0";}
            feature c: ISQ::MassValue {:>> range = "2.0..5.0";}
            feature d: Quantities::ScalarQuantityValue {:>> unit = "s^2";:>> range = "10.0";} 
            feature a: ISQ::ForceValue = b*c/d {:>> unit = "kN";}
        """, Runlevel.ALL)
        assertNoIssues()
        val pta = global.resolveVar("a")!!
        pta.ast!!.evalUpRec()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")

        // Bug: The unit Kg is saved as g, but the value is kept in the original value.
        // Hence, 2..5 kg becomes 2..5 g.
        // In the SI unit system, kg would even be the correct base unit (?).
        assertEquals("0.002..0.05 kN", (global.resolveVar("a")!!.vectorQuantity.toString()))
        assertEquals(0.002, (global.resolveVar("a")!!.min()), 0.000001)
        assertEquals(0.05, (global.resolveVar("a")!!.max()), 0.000001)
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with ranges and units. */
    @Test
    fun evalUpWithNoDefinedResult() = testSession("ISQ", "Ranges")  {
        loadKerML(catchExceptions = false, input = """
            feature b: ISQ::LengthValue {:>> range = "10.0 .. 100.0";}
            feature c: ISQ::MassValue {:>> range = "2.0..5.0";}
            feature d: Quantities::ScalarQuantityValue {:>> unit = "s^2"; :>> range = "10.0";}
            feature a: ISQ::ForceValue = b*c/d;""")
        solver.propagate()
        // Bug: The unit Kg is saved as g, but the value is kept in the original value.
        // Hence, 2..5 kg becomes 2..5 g.
        // In the SI unit system, kg would even be the correct base unit (?).
        assertEquals(2.0, global.resolveVar("a")!!.min(), 0.0001)
        assertEquals(50.0, global.resolveVar("a")!!.max(), 0.0001)
        assertEquals("kg m / s^2", global.resolveVar("a")!!.vectorQuantity.unit.toString())
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
            feature b: Ranges::RealInRange {:>> range = "-10.0.. 20.0";}
            feature a: Ranges::RealInRange = b {:>> range = "7.0 .. 8.0";}
        """, Runlevel.SOLVED)
        val a = global.resolveVar("a") !!
        a.ast!!.evalDownRec()
        assertEquals(Range(-10.0..20.0), global.resolveVar("b")!!.rangeSpecs[0])
        assertEquals(7.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(8.0, global.resolveVar("b")!!.max(), 0.00001)
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
            feature b: Ranges::RealInRange {:>> range = "-100.0..200.0";}
            feature c: Ranges::RealInRange {:>> range = "2..2";}
            feature a: Ranges::RealInRange = b+c {:>> range = "7.0 .. 7.0";}
        """, Runlevel.SOLVED)
        global.resolveVar("a")!!.ast!!.evalDownRec()
        assertEquals(5.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(5.0, global.resolveVar("b")!!.max(), 0.00001)
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
        val a = global.resolveVar("a")!!
        assertTrue(a.aadd().getRange().isReals())
    }

    @Test
    fun evalUpWithScalars() = testSession("Ranges")  {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range="1.0";} 
            feature c: Ranges::RealInRange {:>> range="2.0";}
            feature d: Ranges::RealInRange {:>> range="3.0";}
            feature a: ScalarValues::Real = b+c*d.
        """, Runlevel.SOLVED)
        val a = global.resolveVar("a")
        assertNotNull(a)
        assertEquals(7.0, (global.resolveVar("a")!!.aadd() as AADD.Leaf).central)
        assertEquals(7.0, global.resolveVar("a")!!.max(), 0.0001)
        assertEquals(1.0, global.resolveVar("b")!!.max(), 0.0001)
        assertEquals(2.0, global.resolveVar("c")!!.max(), 0.0001)
        assertEquals(3.0, global.resolveVar("d")!!.max(), 0.0001)
        assertNoIssues()
    }

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun evalDownWithScalarsTest() = testSession("Ranges")  {
        loadKerML("""
            feature b: Ranges::RealInRange {:>> range ="-10..20";}
            feature c: Ranges::RealInRange {:>> range ="2.0";}
            feature d: Ranges::RealInRange {:>> range ="3.0";}
            feature a: Ranges::RealInRange = b+c*d {:>> range = "7.0 .. 7.0";}
        """, Runlevel.SOLVED)
        val a = global.resolveVar("a") !!
        a.ast!!.evalDownRec()
        assertEquals(1.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(1.0, global.resolveVar("b")!!.max(), 0.00001)
        assertNoIssues()
    }

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun solveWithUnitsSubtraction() = testSession("ISQ", "Ranges")  {
        loadKerML("""
            feature b: ISQ::LengthValue {:>> unit = "cm"; :>> range = "-100..200";}
            feature c: ISQ::LengthValue {:>> range = "-10..30";}
            feature a: ISQ::LengthValue = b-c {:>> range = "-30.0 .. 60.0";}
        """, Runlevel.SOLVED)
        assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
        assertEquals(-30.0, global.resolveVar("a")!!.min(), 0.000001)
        assertEquals(12.0, global.resolveVar("a")!!.max(), 0.000001)
        assertEquals(-100.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(200.0, global.resolveVar("b")!!.max(), 0.00001)
        assertEquals(-10.0, global.resolveVar("c")!!.min(), 0.00001)
        assertEquals(30.0, global.resolveVar("c")!!.max(), 0.00001)
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with ranges. */
    @Test
    fun evalUpWithRangesTest() = testSession("Ranges", runlevel = Runlevel.ALL)  {
        loadKerML("feature b: Ranges::RealInRange {:>> range = \"1.0..2.0\";}")
        loadKerML("feature c: Ranges::RealInRange {:>> range = \"2.0..3.0\";}")
        loadKerML("feature d: Ranges::RealInRange {:>> range = \"3.0..4.0\";}")
        loadKerML("feature a: Ranges::RealInRange = b+c*d;")
        val a = global.resolveVar("a")
        assertNotNull(a)
        assertEquals(7.0, global.resolveVar("a")!!.min(), 0.00001)
        assertEquals(14.0, global.resolveVar("a")!!.max(), 0.00001)
        // central value depends on approximation schemes; might cause incorrect fault iff changed.
        // only outside tests display((displayTree("a", p.getVar("a").value)))
        // println(resolveName<Expression>("a")!!.quantity.value.toIteString())
        assertTrue( (global.resolveVar("a")!!.aadd() as AADD.Leaf).value.central in 10.0..12.0)
        assertNoIssues()
    }

    /** ConstNet shall compute bottom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsDivision() {
        testSession("ISQ", "Ranges")  {
            loadKerML("feature b: ISQ::LengthValue {:>> unit = \"km\"; :>> range = \"1.0..2.0\";}")
            loadKerML("feature c: ISQ::LengthValue {:>> range = \"2.0..3.0\";}")
            loadKerML("feature d: ISQ::DurationValue {:>> range = \"5.0..10.0\";}")
            loadKerML("feature a: ISQ::FrequencyValue = b/c/d;")
            solver.propagate()
            assertEquals(33.33333333333334, global.resolveVar("a")!!.vectorQuantity.getMinAsDouble(), 0.00000001)
            assertEquals(200.0, global.resolveVar("a")!!.vectorQuantity.getMaxAsDouble(), 0.00000001)
            assertEquals("1 / s", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            assertNoIssues()
        }
    }

    /** ConstNet shall compute buttom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsMultiplication() = testSession("ISQ", "Ranges")  {
        loadKerML("""
            feature d: ISQ::LengthValue {:>> unit = "mm"; :>> range = "10.0 .. 20.0";}
            feature c: ISQ::LengthValue {:>> range = "2.0 .. 3.0";}
            feature b: ISQ::LengthValue {:>> unit = "km"; :>> range = "1.0 .. 2.0";}
            feature a: ISQ::VolumeValue = b*c*d;"""
        )
        solver.propagate()
        assertEquals(20.0, global.resolveVar("a")!!.min(), 0.0001)
        assertEquals(120.0, global.resolveVar("a")!!.max(), 0.0001)
        assertEquals("m^3", global.resolveVar("a")!!.vectorQuantity.unit.toString())
    }

    /** ConstNet shall compute buttom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsAddition() {
        testSession("ISQ", "Ranges")  {
            loadKerML("feature b: ISQ::LengthValue {:>> unit = \"mm\"; :>> range = \"1.0..2.0\";}")
            loadKerML("feature c: ISQ::LengthValue {:>> range = \"2.0..3.0\";}")
            loadKerML("feature d: ISQ::LengthValue{:>> unit = \"km\"; :>> range = \"1.0..4.0\";}")
            loadKerML("feature a: ISQ::LengthValue = b+c+d;")
            solver.propagate()
            assertEquals(4003.002, global.resolveVar("a")!!.max(), 0.0001)
            assertEquals(1002.001, global.resolveVar("a")!!.min(), 0.0001)
            assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
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
            assertEquals(-6.0, global.resolveVar("a")!!.min(), 0.00001)
            assertEquals(-1.0, global.resolveVar("a")!!.max(), 0.00001)
            assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
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
            assertEquals("True", global.resolveVar("d")!!.bool().toString())
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
            assertEquals("False", global.resolveVar("d")!!.bool().toString())
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
            assertEquals("False", global.resolveVar("d")!!.bool().toString())
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
            assertEquals("True", global.resolveVar("d")!!.bool().toString())
            assertNoIssues()
        }
    }
}
