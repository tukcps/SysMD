package constraintnettests

import util.testSession
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class ConstraintPropagationTests {

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun evalDownWithUnitsAddition() {
        testSession("SI") {
            // -30 .. 50 m == -1000..2000 cm + -10 .. 30 m
            // => -20 .. 50 m == ...
            loadKerML("feature b: SI::Length {:>> unit = \"cm\"; :>> range = \"-1000 .. 2000\";}")
            loadKerML("feature c: SI::Length {:>> range = \"-10.0..30.0\";}")
            loadKerML("feature d: SI::Length {:>> range = \"10.0..10\";}")
            loadKerML("feature a: SI::Length = b+c+d {:>> range = \"-30.0 .. 50.0\";}")
            propagate()
            assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            // println(resolveName<Expression>("b")!!.quantity)
            // println(resolveName<Expression>("b")!!.quantity.valueIn("mm"))
            assertEquals(-1000.0, global.resolveVar("b")!!.vectorQuantity.valuesIn("cm")[0].asAadd().getRange().min, 0.00001)
            assertEquals(2000.0, global.resolveVar("b")!!.vectorQuantity.valuesIn("cm")[0].asAadd().getRange().max, 0.00001)
            assertEquals(-10.0, global.resolveVar("c")!!.aadd().getRange().min, 0.00001)
            assertEquals(30.0, global.resolveVar("c")!!.aadd().getRange().max, 0.00001)
            assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        }
    }

    /** Units shall be converted when computing bottom-up, considering the prefix. */
    @Test
    fun evalUpNoOperation() {
        testSession("SI") {
            loadKerML("feature a: SI::Length = 1.0 m;")
            assertEquals(0, status.issues.size, status.issues.toString())
            val a = global.resolveVar("a") !!
            a.ast!!.evalUpRec()
            assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            assertEquals(0.001, global.resolveVar("a")!!.vectorQuantity.valuesIn("km")[0].asAadd().getRange().min, 0.00001)
        }
    }


    @Test
    fun evalDownWithRange()  = testSession("SI") {
        loadKerML("feature a: SI::Voltage {:>> unit = \"mV\"; :>> range = \"1..20\";}")
        val a = global.resolveVar("a")
        assertNotNull(a)
        initialize()
        propagate()
        assertEquals("1..20 mV", global.resolveVar("a")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /**
     * ConstNet shall support ranges in dependencies.
     **/
    @Test
    fun evalDownWithUnitsMultiplication()  = testSession("ScalarValues") {
        loadKerML("""
            feature b: ScalarValues::Real {:>> range = "1.0 .. 30.0";} // 1..5
            feature c: ScalarValues::Real {:>> range = "2.0 .. 20.0";} // 2..10
            feature a: ScalarValues::Real = b*c {:>> range = "9.0 .. 10.0";}""")
        propagate()
        assertEquals(1.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(5.0, global.resolveVar("b")!!.max(), 0.00001)
        assertEquals(2.0, global.resolveVar("c")!!.aadd().getRange().min, 0.00001)
        assertEquals(10.0, global.resolveVar("c")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /**
     * ConstNet shall compute top-down with scalars.
     * Division should here avoid including division by zero, which is a separate test case.
     */
    @Test
    fun evalDownWithUnitsDivision() = testSession("SI") {
        loadKerML("""
            feature b: SI::Length {:>> range = "1..20";}                         // 0.5..4, reduced to 1..4 m
            feature c: SI::Length {:>> unit = "cm"; :>> range = "100 .. 200";}    // 1..2 m
            feature a: ScalarValues::Real = b/c {:>> range = "1.0 .. 2.0";} 
        """)
        propagate()
        assertEquals("1", global.resolveVar("a")!!.vectorQuantity.unit.toString())
        assertEquals(1.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(4.0, global.resolveVar("b")!!.max(), 0.00001)
        assertEquals(100.0, global.resolveVar("c")!!.min(), 0.00001)
        assertEquals(200.0, global.resolveVar("c")!!.max(), 0.00001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }


    /**
     * ConstNet shall propagate top-down with scalars and the special case model.builder.Reals.
     **/
    @Test
    fun evalDownWithScalarsRealsTest() {
        testSession("ScalarValues") {
            loadKerML("""
                feature b: ScalarValues::Real;
                feature c: ScalarValues::Real = 2.0;
                feature d: ScalarValues::Real = 3.0;
                feature a: ScalarValues::Real = b+c*d {:>> range = "7.0 .. 7.0";} 
            """)
            propagate()
            assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
            assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
            assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        }
    }

    @Test
    fun evalDownMultiplicationTest() = testSession("Occurrences", "Links","SI") {
        loadKerML("""
                class Baseplate {
                    feature width: SI::Length {:>> unit = "mm"; :>> range = "100 .. 300";} 
                    feature depth: SI::Length {:>> unit = "mm"; :>> range = "100 .. 300";} 
                    feature area:  SI::Area  = width * depth {:>> unit = "cm^2"; :>> range = "800 .. 1000";} 
                }
            """.trimIndent())
        propagate()
        assertEquals(0.26666, global.resolveVar("Baseplate::depth")!!.vectorQuantity.getMinAsDouble(), 0.001)
        assertEquals(0.26666, global.resolveVar("Baseplate::width")!!.vectorQuantity.getMinAsDouble(), 0.001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }


    /**
     * We check that an evaluation downwards restricts the 'builder.range' of properties.
     */
    @Test
    fun evalDownWithRangesTest() = testSession("ScalarValues")  {
        loadKerML(input ="""
            feature b: ScalarValues::Real {:>> range = "-10.0 .. 20.0";} // Larger than needed to get results
            feature c: ScalarValues::Real {:>> range = "2.0 .. 3.0";}
            feature d: ScalarValues::Real {:>> range = "3.0 .. 4.0";}
            feature a: ScalarValues::Real = b+c*d {:>> range = "7 .. 14";} """) // b hence can only be from -5 to 8.
        val a = global.resolveVar("a") !!
        a.ast!!.evalDown()
        assertTrue(Range(-5.0..8.0) in (global.resolveVar("b")!!.vectorQuantity.values[0] as AADD).getRange())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }


    /** ConstNet shall compute bottom-up with ranges and units. */
    @Test
    fun evalUpWithDefinedResult() = testSession("SI")  {
        loadKerML(
            """
            feature b: SI::Length {:>> range = "10.0 .. 100.0";}
            feature c: SI::Mass  {:>> range = "2.0..5.0";}
            feature d: SI::Quantity(10.0) {:>> unit = "s^2";} ;
            feature a: SI::Force = b*c/d {:>> unit = "kN";}""")
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")
        val pta = global.resolveVar("a")!!
        pta.ast!!.evalUpRec()
        assertEquals(0, status.issues.size, "Error message: ${status.issues}")

        // Bug: The unit Kg is saved as g, but the value is kept in the original value.
        // Hence, 2..5 kg becomes 2..5 g.
        // In the SI unit system, kg would even be the correct base unit (?).
        assertEquals("0.002..0.05 kN", (global.resolveVar("a")!!.vectorQuantity.toString()))
        assertEquals(0.002, (global.resolveVar("a")!!.aadd().getRange().min), 0.000001)
        assertEquals(0.05, (global.resolveVar("a")!!.aadd().getRange().max), 0.000001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with ranges and units. */
    @Test
    fun evalUpWithNoDefinedResult() = testSession("SI")  {
        loadKerML(catchExceptions = false, input = """
            feature b: SI::Length {:>> range = "10.0 .. 100.0";}
            feature c: SI::Mass {:>> range = "2.0..5.0";}
            feature d: SI::Quantity(10.0) {:>> unit = "s^2";};
            feature a: SI::Force = b*c/d;""")
        propagate()
        // Bug: The unit Kg is saved as g, but the value is kept in the original value.
        // Hence, 2..5 kg becomes 2..5 g.
        // In the SI unit system, kg would even be the correct base unit (?).
        assertEquals(2.0, global.resolveVar("a")!!.min(), 0.0001)
        assertEquals(50.0, global.resolveVar("a")!!.max(), 0.0001)
        assertEquals("kg m / s^2", global.resolveVar("a")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /*  ################### EVAL DOWN SECTION ################### */

    /**
     * EvalDown from left side constraint to right side
     *  7..7 = -10..20 becomes to 7..7 = 7..7
     *  However, the constraint -10.0..20 shall be maintained as it is a separate field
     *  of the property.
     */
    @Test
    fun evalDownLeftToRightSideTest() = testSession("ScalarValues")  {
            loadKerML("""
                feature b: ScalarValues::Real {:>> range = "-10 .. 20";}
                feature a: ScalarValues::Real = b {:>> range = "7.0 .. 8.0";}
            """)
            val a = global.resolveVar("a") !!
            a.ast!!.evalDownRec()
            assertEquals(Range(-10.0..20.0), global.resolveVar("b")!!.rangeSpecs[0])
            assertEquals(7.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
            assertEquals(8.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
            assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /**
     * ConstNet shall compute top-down with scalars.
     * a:7 = b:-100..200 + c:2
     * shall evaluate to a:7 = b:5+c:2
     */
    @Test
    fun evalDownOneStepTest() = testSession("ScalarValues")  {
        loadKerML("""
            feature b: ScalarValues::Real {:>> range = "-100.0..200.0";}
            feature c: ScalarValues::Real {:>> range = "2..2";}
            feature a: ScalarValues::Real = b+c {:>> range = "7.0 .. 7.0";}"""
        )
        global.resolveVar("a")!!.ast!!.evalDownRec()
        assertEquals(5.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
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
        """)
        val a = global.resolveVar("a")!!
        assertTrue(a.aadd().getRange().isReals())
    }

    @Test
    fun evalUpWithScalars() = testSession("ScalarValues")  {
        loadKerML("""
            feature b: ScalarValues::Real(1.0); 
            feature c: ScalarValues::Real(2.0); 
            feature d: ScalarValues::Real(3.0);
            feature a: ScalarValues::Real = b+c*d.
        """)
        val a = global.resolveVar("a")
        assertNotNull(a)
        assertEquals(7.0, (global.resolveVar("a")!!.aadd() as AADD.Leaf).central)
        assertEquals(7.0, global.resolveVar("a")!!.aadd().getRange().max, 0.0001)
        assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().max, 0.0001)
        assertEquals(2.0, global.resolveVar("c")!!.aadd().getRange().max, 0.0001)
        assertEquals(3.0, global.resolveVar("d")!!.aadd().getRange().max, 0.0001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun evalDownWithScalarsTest() = testSession("ScalarValues")  {
        loadKerML("""
            feature b: ScalarValues::Real(-10..20);
            feature c: ScalarValues::Real(2.0);
            feature d: ScalarValues::Real(3.0);
            feature a: ScalarValues::Real = b+c*d {:>> range = "7.0 .. 7.0";}""")
        val a = global.resolveVar("a") !!
        a.ast!!.evalDownRec()
        assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun solveWithUnitsSubtraction() = testSession("SI")  {
        loadKerML("""
            feature b: SI::Length {:>> unit = "cm"; :>> range = "-100..200";}
            feature c: SI::Length {:>> range = "-10..30";}
            feature a: SI::Length = b-c {:>> range = "-30.0 .. 60.0";}"""
        )
        propagate()
        assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
        assertEquals(-30.0, global.resolveVar("a")!!.aadd().getRange().min, 0.000001)
        assertEquals(12.0, global.resolveVar("a")!!.aadd().getRange().max, 0.000001)
        assertEquals(-100.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(200.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals(-10.0, global.resolveVar("c")!!.aadd().getRange().min, 0.00001)
        assertEquals(30.0, global.resolveVar("c")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute bottom-up with ranges. */
    @Test
    fun evalUpWithRangesTest() {
        testSession("Ranges")  {
            loadKerML("feature b: ScalarValues::Real, Ranges::InRange {:>> range = \"1.0..2.0\";}")
            loadKerML("feature c: ScalarValues::Real, Ranges::InRange {:>> range = \"2.0..3.0\";}")
            loadKerML("feature d: ScalarValues::Real, Ranges::InRange {:>> range = \"3.0..4.0\";}")
            loadKerML("feature a: ScalarValues::Real, Ranges::InRange = b+c*d;")
            val a = global.resolveVar("a")
            assertNotNull(a)
            assertEquals(7.0, global.resolveVar("a")!!.min(), 0.00001)
            assertEquals(14.0, global.resolveVar("a")!!.max(), 0.00001)
            // central value depends on approximation schemes; might cause incorrect fault iff changed.
            // only outside tests display((displayTree("a", p.getVar("a").value)))
            // println(resolveName<Expression>("a")!!.quantity.value.toIteString())
            assertEquals(10.25, (global.resolveVar("a")!!.aadd() as AADD.Leaf).value.central)
            assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        }
    }

    /** ConstNet shall compute bottom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsDivision() {
        testSession("SI")  {
            loadKerML("feature b: SI::Length {:>> unit = \"km\"; :>> range = \"1.0..2.0\";}")
            loadKerML("feature c: SI::Length {:>> range = \"2.0..3.0\";}")
            loadKerML("feature d: SI::Time {:>> range = \"5.0..10.0\";}")
            loadKerML("feature a: SI::Frequency = b/c/d;")
            propagate()
            assertEquals(33.33333333333334, global.resolveVar("a")!!.vectorQuantity.getMinAsDouble(), 0.00000001)
            assertEquals(200.0, global.resolveVar("a")!!.vectorQuantity.getMaxAsDouble(), 0.00000001)
            assertEquals("1 / s", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        }
    }

    /** ConstNet shall compute buttom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsMultiplication() = testSession("SI")  {
        loadKerML("""
            feature d: SI::Length {:>> unit = "mm"; :>> range = "10.0 .. 20.0";}
            feature c: SI::Length {:>> range = "2.0 .. 3.0";}
            feature b: SI::Length {:>> unit = "km"; :>> range = "1.0 .. 2.0";}
            feature a: SI::Volume = b*c*d;"""
        )
        propagate()
        assertEquals(20.0, global.resolveVar("a")!!.min(), 0.0001)
        assertEquals(120.0, global.resolveVar("a")!!.max(), 0.0001)
        assertEquals("m^3", global.resolveVar("a")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    /** ConstNet shall compute buttom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsAddition() {
        testSession("SI")  {
            loadKerML("feature b: SI::Length {:>> unit = \"mm\"; :>> range = \"1.0..2.0\";}")
            loadKerML("feature c: SI::Length {:>> range = \"2.0..3.0\";}")
            loadKerML("feature d: SI::Length {:>> unit = \"km\"; :>> range = \"1.0..4.0\";}")
            loadKerML("feature a: SI::Length = b+c+d;")
            propagate()
            assertEquals(4003.002, global.resolveVar("a")!!.aadd().getRange().max, 0.0001)
            assertEquals(1002.001, global.resolveVar("a")!!.aadd().getRange().min, 0.0001)
            assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        }
    }

    /** ConstNet shall compute buttom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsSubtraction() {
        testSession("SI")  {
            loadKerML("feature b: SI::Length = [1.0 .. 2.0] [m];")
            loadKerML("feature c: SI::Length = [2.0 .. 3.0] [m];")
            loadKerML("feature d: SI::Length = [1.0 .. 4.0] [m];")
            loadKerML("feature a: SI::Length = b-c-d;")
            assertEquals(0, status.issues.size, status.issues.toString())
            propagate()
            assertEquals(-6.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
            assertEquals(-1.0, global.resolveVar("a")!!.aadd().getRange().max, 0.00001)
            assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        }
    }

    @Test
    fun notEquals() {
        testSession("SI")  {
            loadKerML("feature b: SI::Length = [1.0 .. 2.0] [m];")
            loadKerML("feature c: SI::Length = [3.0 .. 4.0] [m];")
            loadKerML("feature d: ScalarValues::Boolean = c!=b;")
            assertEquals(0, status.issues.size, status.issues.toString())
            propagate()
            assertEquals("True", global.resolveVar("d")!!.bdd().toString())
            assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
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
            assertEquals(0, status.issues.size, status.issues.toString())
            propagate()
            assertEquals("False", global.resolveVar("d")!!.bdd().toString())
            assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        }
    }

    @Test
    fun notEquals3() {
        testSession("SI")  {
            loadKerML("""
                feature b: SI::Length = 1.0 m ;
                feature c: SI::Length = 1.0 m;
                feature d: ScalarValues::Boolean = b!=c;
            """)
            assertEquals(0, status.issues.size, status.issues.toString())
            propagate()
            assertEquals("False", global.resolveVar("d")!!.bdd().toString())
            assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        }
    }

    @Test
    fun equals3() {
        testSession("SI")  {
            loadKerML("feature b: SI::Length = 1.0 m ;")
            loadKerML("feature c: SI::Length = 1.0 m;")
            loadKerML("feature d: ScalarValues::Boolean = b==c;")
            assertEquals(0, status.issues.size, status.issues.toString())
            propagate()
            assertEquals("True", global.resolveVar("d")!!.bdd().toString())
            assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        }
    }
}
