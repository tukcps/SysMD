package constraintnettests

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConstraintPropagationTests {


    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun evalDownWithUnitsAddition() {
        testSession {
            // -30 .. 50 m == -1000..2000 cm + -10 .. 30 m
            // => -20 .. 50 m == ...
            +"attribute b: ScalarValues::Real(-1000 .. 2000) [cm];"
            +"attribute c: ScalarValues::Real(-10.0..30.0) [m];"
            +"attribute d: ScalarValues::Real(10.0..10) [m];"
            +"attribute a: ScalarValues::Real(-30.0 .. 50.0) [m] = b+c+d;"
            propagate()
            assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            // println(resolveName<Expression>("b")!!.quantity)
            // println(resolveName<Expression>("b")!!.quantity.valueIn("mm"))
            assertEquals(-1000.0, global.resolveVar("b")!!.vectorQuantity.valuesIn("cm")[0].asAadd().getRange().min, 0.00001)
            assertEquals(2000.0, global.resolveVar("b")!!.vectorQuantity.valuesIn("cm")[0].asAadd().getRange().max, 0.00001)
            assertEquals(-10.0, global.resolveVar("c")!!.aadd().getRange().min, 0.00001)
            assertEquals(30.0, global.resolveVar("c")!!.aadd().getRange().max, 0.00001)
            assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        }
    }

    /** Units shall be converted when computing bottom-up, considering the prefix. */
    @Test
    fun evalUpNoOperation() {
        testSession(catchExceptions = false) {
            loadSysMD("attribute a: ScalarValues::Real [km] = 1.0 m.")
            assertEquals(0, status.exceptions.size, status.exceptions.toString())
            val a = global.resolveVar("a") !!
            a.ast!!.evalUpRec()
            assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            assertEquals(0.001, global.resolveVar("a")!!.vectorQuantity.valuesIn("km")[0].asAadd().getRange().min, 0.00001)
        }
    }


    @Test
    fun evalDownWithRange()  = testSession {
        loadSysMD("attribute a: ScalarValues::Real(1..20) [mV].")
        val a = global.resolveVar("a")
        assertNotNull(a)
        initialize()
        propagate()
        assertEquals("1..20 mV", global.resolveVar("a")!!.vectorQuantity.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /**
     * ConstNet shall support ranges in dependencies.
     **/
    @Test
    fun evalDownWithUnitsMultiplication()  = testSession {
        loadSysMD("""
            attribute b: ScalarValues::Real(1.0 .. 30.0). // 1..5
            attribute c: ScalarValues::Real(2.0 .. 20.0). // 2..10
            attribute a: ScalarValues::Real(9.0 .. 10.0) = b*c.""")
        propagate()
        assertEquals(1.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(5.0, global.resolveVar("b")!!.max(), 0.00001)
        assertEquals(2.0, global.resolveVar("c")!!.aadd().getRange().min, 0.00001)
        assertEquals(10.0, global.resolveVar("c")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /**
     * ConstNet shall compute top-down with scalars.
     * Division should here avoid including division by zero, which is a separate test case.
     */
    @Test
    fun evalDownWithUnitsDivision() = testSession {
        +"""attribute b: ScalarValues::Real(1..20) [m].           // 0.5..4, reduced to 1..4 m
            attribute c: ScalarValues::Real(100 .. 200) [cm].     // 1..2 m
            attribute a: ScalarValues::Real(1.0 .. 2.0) = b/c."""
        propagate()
        assertEquals("1", global.resolveVar("a")!!.vectorQuantity.unit.toString())
        assertEquals(1.0, global.resolveVar("b")!!.min(), 0.00001)
        assertEquals(4.0, global.resolveVar("b")!!.max(), 0.00001)
        assertEquals(100.0, global.resolveVar("c")!!.min(), 0.00001)
        assertEquals(200.0, global.resolveVar("c")!!.max(), 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    /**
     * ConstNet shall propagate top-down with scalars and the special case model.builder.Reals.
     **/
    @Test
    fun evalDownWithScalarsRealsTest() {
        testSession {
            +"""attribute b: ScalarValues::Real;
                attribute c: ScalarValues::Real = 2.0;
                attribute d: ScalarValues::Real = 3.0;
                attribute a: ScalarValues::Real(7.0 .. 7.0) = b+c*d;"""
            propagate()
            assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
            assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
            assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        }
    }

    @Test
    fun evalDownMultiplicationTest() = testSession {
        loadSysMD("""
                class Baseplate {
                    attribute width: ScalarValues::Real(100 .. 300) [mm]; 
                    attribute depth: ScalarValues::Real(100 .. 300) [mm]; 
                    attribute area:  ScalarValues::Real(800 .. 1000) [cm^2] = width * depth; 
                }
            """.trimIndent())
        propagate()
        assertEquals(0.26666, global.resolveVar("Baseplate::depth")!!.vectorQuantity.getMinAsDouble(), 0.001)
        assertEquals(0.26666, global.resolveVar("Baseplate::width")!!.vectorQuantity.getMinAsDouble(), 0.001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    /**
     * We check that an evaluation downwards restricts the 'builder.range' of properties.
     */
    @Test
    fun evalDownWithRangesTest() = testSession {
        loadSysMD(input = """
            attribute b: ScalarValues::Real(-10.0 .. 20.0).  // Larger than needed to get results
            attribute c: ScalarValues::Real(2.0 .. 3.0).
            attribute d: ScalarValues::Real(3.0 .. 4.0).
            attribute a: ScalarValues::Real(7 .. 14) = b+c*d. """) // b hence can only be from -5 to 8.
        val a = global.resolveVar("a") !!
        a.ast!!.evalDown()
        assertTrue(Range(-5.0..8.0) in (global.resolveVar("b")!!.vectorQuantity.values[0] as AADD).getRange())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    /** ConstNet shall compute bottom-up with ranges and units. */
    @Test
    fun evalUpWithDefinedResult() = testSession {
        loadSysMD("""
            attribute b: ScalarValues::Real(10.0 .. 100.0) [m];
            attribute c: ScalarValues::Real(2.0..5.0) [kg];
            attribute d: ScalarValues::Real(10.0) [s^2];
            attribute a: ScalarValues::Real [kN] = b*c/d.""")
        assertEquals(0, status.exceptions.size, "Error message: ${status.exceptions}")
        val pta = global.resolveVar("a")!!
        pta.ast!!.evalUpRec()
        assertEquals(0, status.exceptions.size, "Error message: ${status.exceptions}")

        // Bug: The unit Kg is saved as g, but the value is kept in the original value.
        // Hence, 2..5 kg becomes 2..5 g.
        // In the SI unit system, kg would even be the correct base unit (?).
        assertEquals("0.002..0.05 kN", (global.resolveVar("a")!!.vectorQuantity.toString()))
        assertEquals(0.002, (global.resolveVar("a")!!.aadd().getRange().min), 0.000001)
        assertEquals(0.05, (global.resolveVar("a")!!.aadd().getRange().max), 0.000001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute buttom-up with ranges and units. */
    @Test
    fun evalUpWithNoDefinedResult() = testSession {
        loadSysMD(catchExceptions = false, input = """
            attribute b: ScalarValues::Real(10.0 .. 100.0) [m].
            attribute c: ScalarValues::Real(2.0..5.0) [kg].
            attribute d: ScalarValues::Real(10.0) [s^2].
            attribute a: ScalarValues::Real [N] = b*c/d.""")
        propagate()
        // Bug: The unit Kg is saved as g, but the value is kept in the original value.
        // Hence, 2..5 kg becomes 2..5 g.
        // In the SI unit system, kg would even be the correct base unit (?).
        assertEquals(2.0, global.resolveVar("a")!!.min(), 0.0001)
        assertEquals(50.0, global.resolveVar("a")!!.max(), 0.0001)
        assertEquals("m kg / s^2", global.resolveVar("a")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /*  ################### EVAL DOWN SECTION ################### */

    /**
     * EvalDown from left side constraint to right side
     *  7..7 = -10..20 becomes to 7..7 = 7..7
     *  However, the constraint -10.0..20 shall be maintained as it is a separate field
     *  of the property.
     */
    @Test
    fun evalDownLeftToRightSideTest() = testSession {
            +"""attribute b: ScalarValues::Real(-10 .. 20); 
                attribute a: ScalarValues::Real(7.0 .. 8.0) = b;"""
            val a = global.resolveVar("a") !!
            a.ast!!.evalDownRec()
            assertEquals(Range(-10.0..20.0), global.resolveVar("b")!!.rangeSpecs[0])
            assertEquals(7.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
            assertEquals(8.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
            assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /**
     * ConstNet shall compute top-down with scalars.
     * a:7 = b:-100..200 + c:2
     * shall evaluate to a:7 = b:5+c:2
     */
    @Test
    fun evalDownOneStepTest() = testSession {
        loadSysMD(
            """
            attribute b: ScalarValues::Real(-100.0..200.0).
            attribute c: ScalarValues::Real(2..2).
            attribute a: ScalarValues::Real(7.0 .. 7.0) = b+c."""
        )
        global.resolveVar("a")!!.ast!!.evalDownRec()
        assertEquals(5.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(5.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /* ################### EVAL UP SECTION ################### */

    /** ConstNet shall compute bottom-up with scalars, and overflow should result in inf results. */
    @Test
    fun evalUpWithScalarsNoOverflowTest() = testSession {
        loadSysMD("""
            attribute b: ScalarValues::Real; 
            attribute c: ScalarValues::Real; 
            attribute d: ScalarValues::Real;
            attribute a: ScalarValues::Real = b+c*d.
        """)
        val a = global.resolveVar("a")!!
        assertTrue(a.aadd().getRange().isReals())
    }

    @Test
    fun evalUpWithScalars() = testSession {
        loadSysMD("""
            attribute b: ScalarValues::Real(1.0); 
            attribute c: ScalarValues::Real(2.0); 
            attribute d: ScalarValues::Real(3.0);
            attribute a: ScalarValues::Real = b+c*d.
        """)
        val a = global.resolveVar("a")
        assertNotNull(a)
        assertEquals(7.0, (global.resolveVar("a")!!.aadd() as AADD.Leaf).central)
        assertEquals(7.0, global.resolveVar("a")!!.aadd().getRange().max, 0.0001)
        assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().max, 0.0001)
        assertEquals(2.0, global.resolveVar("c")!!.aadd().getRange().max, 0.0001)
        assertEquals(3.0, global.resolveVar("d")!!.aadd().getRange().max, 0.0001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun evalDownWithScalarsTest() = testSession {
        loadSysMD("""
            attribute b: ScalarValues::Real(-10..20).
            attribute c: ScalarValues::Real(2.0).
            attribute d: ScalarValues::Real(3.0).
            attribute a: ScalarValues::Real(7.0 .. 7.0) = b+c*d.""")
        val a = global.resolveVar("a") !!
        a.ast!!.evalDownRec()
        assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(1.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun solveWithUnitsSubtraction() = testSession {
        loadSysMD("""
            attribute b: ScalarValues::Real(-100..200) [cm].
            attribute c: ScalarValues::Real(-10..30) [m].
            attribute a: ScalarValues::Real(-30.0 .. 60.0) = b-c."""
        )
        propagate()
        assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
        assertEquals(-30.0, global.resolveVar("a")!!.aadd().getRange().min, 0.000001)
        assertEquals(12.0, global.resolveVar("a")!!.aadd().getRange().max, 0.000001)
        assertEquals(-100.0, global.resolveVar("b")!!.aadd().getRange().min, 0.00001)
        assertEquals(200.0, global.resolveVar("b")!!.aadd().getRange().max, 0.00001)
        assertEquals(-10.0, global.resolveVar("c")!!.aadd().getRange().min, 0.00001)
        assertEquals(30.0, global.resolveVar("c")!!.aadd().getRange().max, 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute buttom-up with ranges. */
    @Test
    fun evalUpWithRangesTest() {
        testSession {
            +"attribute b: ScalarValues::Real(1.0..2.0)."
            +"attribute c: ScalarValues::Real(2.0..3.0)."
            +"attribute d: ScalarValues::Real(3.0..4.0)."
            +"attribute a: ScalarValues::Real = b+c*d."
            val a = global.resolveVar("a")
            assertNotNull(a)
            assertEquals(7.0, global.resolveVar("a")!!.min(), 0.00001)
            assertEquals(14.0, global.resolveVar("a")!!.max(), 0.00001)
            // central value depends on approximation schemes; might cause incorrect fault iff changed.
            // only outside tests display((displayTree("a", p.getVar("a").value)))
            // println(resolveName<Expression>("a")!!.quantity.value.toIteString())
            assertEquals(10.25, (global.resolveVar("a")!!.aadd() as AADD.Leaf).value.central)
            assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        }
    }

    /** ConstNet shall compute buttom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsDivision() {
        testSession {
            +"attribute b: ScalarValues::Real(1.0..2.0) [km];"
            +"attribute c: ScalarValues::Real(2.0..3.0) [m];"
            +"attribute d: ScalarValues::Real(5.0..10.0) [km];"
            +"attribute a: ScalarValues::Real = b/c/d;"
            propagate()
            assertEquals(0.03333333333333334, global.resolveVar("a")!!.vectorQuantity.getMinAsDouble(), 0.00000001)
            assertEquals(0.2, global.resolveVar("a")!!.vectorQuantity.getMaxAsDouble(), 0.00000001)
            assertEquals("1 / m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        }
    }

    /** ConstNet shall compute buttom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsMultiplication() = testSession {
        loadSysMD(
            """
            attribute d: ScalarValues::Real(10.0 .. 20.0)[mm].
            attribute c: ScalarValues::Real(2.0 .. 3.0)[m].
            attribute b: ScalarValues::Real(1.0 .. 2.0)[km].
            attribute a: ScalarValues::Real = b*c*d."""
        )
        propagate()
        assertEquals(20.0, global.resolveVar("a")!!.min(), 0.0001)
        assertEquals(120.0, global.resolveVar("a")!!.max(), 0.0001)
        assertEquals("m^3", global.resolveVar("a")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** ConstNet shall compute buttom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsAddition() {
        testSession {
            +"attribute b: ScalarValues::Real(1.0..2.0) [mm];"
            +"attribute c: ScalarValues::Real(2.0..3.0) [m];"
            +"attribute d: ScalarValues::Real(1.0..4.0) [km];"
            +"attribute a: ScalarValues::Real = b+c+d;"
            propagate()
            assertEquals(4003.002, global.resolveVar("a")!!.aadd().getRange().max, 0.0001)
            assertEquals(1002.001, global.resolveVar("a")!!.aadd().getRange().min, 0.0001)
            assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        }
    }

    /** ConstNet shall compute buttom-up with ranges and units. */
    @Test
    fun evalUpWithUnitsSubtraction() {
        testSession {
            +"attribute b: ScalarValues::Real = [1.0 .. 2.0] [m];"
            +"attribute c: ScalarValues::Real = [2.0 .. 3.0] [m];"
            +"attribute d: ScalarValues::Real = [1.0 .. 4.0] [m];"
            +"attribute a: ScalarValues::Real = b-c-d;"
            assertEquals(0, status.exceptions.size, status.exceptions.toString())
            propagate()
            assertEquals(-6.0, global.resolveVar("a")!!.aadd().getRange().min, 0.00001)
            assertEquals(-1.0, global.resolveVar("a")!!.aadd().getRange().max, 0.00001)
            assertEquals("m", global.resolveVar("a")!!.vectorQuantity.unit.toString())
            assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        }
    }
}
