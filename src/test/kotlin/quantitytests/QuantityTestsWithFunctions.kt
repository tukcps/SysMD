package quantitytests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class QuantityTestsWithFunctions {

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit1() = testSession  {
        loadSysMD(
            """
                attribute a: ScalarValues::Real(4.0 .. 9.0) [m^2];
                attribute b: ScalarValues::Real[m] = sqrt(a);"""
        )
        propagate()
        assertEquals("m", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(2.0, global.resolveVar("b")!!.vectorQuantity.getMinAsDouble(), 0.00001)
        assertEquals(3.0, global.resolveVar("b")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit2() = testSession  {
        loadSysMD(
            """
            attribute a: ScalarValues::Real(4.0 .. 9.0) [Ohm^2];
            attribute b: ScalarValues::Real[Ohm] = sqrt(a); """
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals("2..3 Ohm", global.resolveVar("b")!!.vectorQuantity.toString())
    }

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit3() = testSession  {
        loadSysMD("""
            attribute a: ScalarValues::Real(2.0 .. 9.0) [Ohm^2 m^2];
            attribute b: ScalarValues::Real(2.0 .. 9.0) [Ohm m]=sqrt(a); """
        )
        propagate()
        assertEquals("m^3 kg / s^3 A^2", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit4() = testSession  {
        loadSysMD(
            """
            attribute a: ScalarValues::Real(2.0 .. 9.0) [Pa^4 J^2 V^8 / A^6 N^2];
            attribute b: ScalarValues::Real(2.0 .. 9.0) [Pa^2 J V^4 / A^3 N]=sqrt(a);"""
        )
        propagate()
        assertEquals("kg^6 m^7 / s^16 A^7", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** Quantity with no unit */
    @Test
    fun sqr_unit() = testSession  {
        loadSysMD(
            """
            attribute a: ScalarValues::Real(2.0E16 .. 9.0E16);
            attribute b: ScalarValues::Real = sqr(a);"""
        )

        propagate()
        assertEquals("400e30..8.1e33", global.resolveVar("b")!!.vectorQuantity.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** Quantity with sqr */
    @Test
    fun sqr_unit1() = testSession  {
        loadSysMD(
            """
            attribute a: ScalarValues::Real(2.0 .. 9.0) [m];
            attribute b: ScalarValues::Real[m^2] = sqr(a);"""
        )

        propagate()
        assertEquals("m^2", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** Quantity with sqr */
    @Test
    fun sqr_unit2() = testSession  {
        loadSysMD(
            """
            attribute a: ScalarValues::Real(2.0 .. 9.0) [Ohm];
            attribute b: ScalarValues::Real[Ohm^2] = sqr(a);"""
        )
        propagate()
        assertEquals("m^4 kg^2 / s^6 A^4", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals("electrical resistance", global.resolveVar("a")!!.vectorQuantity.getDimension())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /** Quantity with sqr */
    @Test
    fun sqr_unit3() = testSession  {
        loadSysMD(
            """attribute a: ScalarValues::Real(2.0 .. 9.0) [Ohm m];
            attribute b: ScalarValues::Real(2.0 .. 9.0) [Ohm^2 m^2]=sqr(a);"""
        )
        propagate()
        assertEquals("m^6 kg^2 / s^6 A^4", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /**
     * Quantity with sqr
     */
    @Test
    fun sqr_unit4() = testSession  {
        loadSysMD(
            """
            attribute a: ScalarValues::Real(2.0 .. 9.0) [Pa^2 J V^4 / A^3 N].
            attribute b: ScalarValues::Real(4.0 .. 81.0) [Pa^4 J^2 V^8 / A^6 N^2]=sqr(a)."""
        )
        propagate()
        assertEquals("kg^12 m^14 / s^32 A^14", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun sincosTest() = testSession  {
        loadSysMD(
            """
            attribute a: ScalarValues::Real(0.5 .. 0.5) .
            attribute b: ScalarValues::Real=sin(a).
            attribute c: ScalarValues::Real=cos(a)."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0.479425538604203,global.resolveVar("b")!!.vectorQuantity.value.asAadd().min, 0.0001)
        assertEquals(0.8775825618903725,global.resolveVar("c")!!.vectorQuantity.value.asAadd().min, 0.0001)
    }

    @Test
    fun sincosTest2() = testSession  {
            loadSysMD(
                """
            attribute a: ScalarValues::Real(1.0 .. 1.0) .
            attribute b: ScalarValues::Real=sin(a).
            attribute c: ScalarValues::Real=cos(a)."""
            )
            propagate()
            assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
            assertEquals(0.8414709848078965,global.resolveVar("b")!!.vectorQuantity.value.asAadd().min, 0.0001)
            assertEquals(0.5403023058681394,global.resolveVar("c")!!.vectorQuantity.value.asAadd().min, 0.0001)
        }

    @Test
    fun sincosTest3() = testSession  {
        loadSysMD(
            """
            attribute a: ScalarValues::Real(0.5 .. 1.0) .
            attribute b: ScalarValues::Real=sin(a).
            attribute c: ScalarValues::Real=cos(a)."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0.479425538604203,global.resolveVar("b")!!.vectorQuantity.value.asAadd().min, 0.0001)
        assertEquals(0.8414709848078965,global.resolveVar("b")!!.vectorQuantity.value.asAadd().max, 0.0001)
        assertEquals(0.5403023058681394,global.resolveVar("c")!!.vectorQuantity.value.asAadd().min, 0.0001)
        assertEquals(0.877582561890373,global.resolveVar("c")!!.vectorQuantity.value.asAadd().max, 0.0001)
    }

    /**
     * Quantity with ceil
     */
    @Test
    fun ceil_quantity() = testSession  {
        loadSysMD(
            """
            attribute a: ScalarValues::Real [m] = 1.5 m .
            attribute b: ScalarValues::Real [m] = ceil(a).
            """
        )
        propagate()
        assertEquals("m", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(2.0, global.resolveVar("b")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.0, global.resolveVar("b")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun ceil_quantity_int() = testSession  {
        loadSysMD(
            """
            attribute a: ScalarValues::Integer = [1..3] .
            attribute b: ScalarValues::Integer = ceil(a).
            """
        )
        propagate()
        assertEquals(2, global.resolveVar("b")!!.vectorQuantity.value.asIdd().min)
        assertEquals(3, global.resolveVar("b")!!.vectorQuantity.value.asIdd().max)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /**
     * Quantity with ceil
     */
    @Test
    fun floor_quantity() = testSession  {
        loadSysMD(
            """
            attribute a: ScalarValues::Real(1.5..4.5) [m].
            attribute b: ScalarValues::Real [m] = floor(a).
        """
        )
        propagate()
        assertEquals("m", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(1.0, global.resolveVar("b")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(4.0, global.resolveVar("b")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun floor_quantity_int() = testSession  {
        loadSysMD(
            """
            attribute a: ScalarValues::Integer = [1..3] .
            attribute b: ScalarValues::Integer = floor(a).
        """
        )
        propagate()
        assertEquals(1, global.resolveVar("b")!!.vectorQuantity.value.asIdd().min)
        assertEquals(2, global.resolveVar("b")!!.vectorQuantity.value.asIdd().max)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun floor_quantity_int1() = testSession  {
        loadSysMD(
            """
          attribute a: ScalarValues::Integer(-3..0).
          attribute b: ScalarValues::Integer = floor(a).
        """
        )
        propagate()
        assertEquals(-3, global.resolveVar("b")!!.vectorQuantity.value.asIdd().min)
        assertEquals(-1, global.resolveVar("b")!!.vectorQuantity.value.asIdd().max)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /**
     * Quantity with max
     */
    @Test
    fun max_quantity() = testSession  {
        loadSysMD("""
                attribute a: ScalarValues::Real = 1.0 ;
                attribute b: ScalarValues::Real = 2.0 .
                attribute y: ScalarValues::Real = max(a, b).
            """)
        propagate()
        val y = global.resolveVar("y")!!
        assertEquals(2.0, y.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.0, y.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /**
     * Quantity with max
     */
    @Test
    fun max_quantity2() = testSession  {
        loadSysMD("""
                attribute a: ScalarValues::Integer = 1;
                attribute b: ScalarValues::Integer = 2;
                attribute y: ScalarValues::Integer = max(a, b).
            """)
        propagate()
        val y = global.resolveVar("y")!!
        assertEquals(2.0, y.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.0, y.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    /**
     * Quantity with max
     */
    @Test
    fun min_quantity() = testSession  {
        loadSysMD("""
                attribute a: ScalarValues::Real = 1.0;
                attribute b: ScalarValues::Real = 2.0;
                attribute y: ScalarValues::Real = min(a, b);
            """)
        // propagate()
        val y = global.resolveVar("y")!!
        assertEquals(1.0, y.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, y.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    /**
     * Quantity with max
     */
    @Test
    fun min_quantity2() = testSession  {
        loadSysMD("""
                attribute a: ScalarValues::Integer = 1;
                attribute b: ScalarValues::Integer = 2;
                attribute y: ScalarValues::Integer = min(a, b);
            """)
        propagate()
        val y = global.resolveVar("y")!!
        assertEquals(1.0, y.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, y.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /**
     * Testcase for operation a^b with real
     */
    @Test
    fun testHATbReal() = testSession  {
        loadSysMD("""
                attribute a: ScalarValues::Real = 5.0;
                attribute b: ScalarValues::Real = 3.0;
                attribute y: ScalarValues::Real = a^b;
            """)
        assertEquals(125.0, global.resolveVar("y")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /**
     * Testcase for operation a^b with ScalarValues::Integer
     */
    @Test
    fun testHATbINT() = testSession  {
        loadSysMD("""
                attribute a: ScalarValues::Integer = 5;
                attribute b: ScalarValues::Integer = 3;
                attribute y: ScalarValues::Integer = a^b;
            """)
        assertEquals(125, global.resolveVar("y")!!.vectorQuantity.idd().getRange().min)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /**
     * Testcase for operation a^b evalDown
     */
    @Test
    fun testHATbEVALDown() = testSession  {
        loadSysMD("""
                attribute a: ScalarValues::Real;
                attribute b: ScalarValues::Real = 3.0;
                attribute y: ScalarValues::Real(125..125) = a ^ b;
            """)
        propagate()
        assertEquals(5.0, global.resolveVar("a")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(5.0, global.resolveVar("a")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    /**
     * Testcase for operation a^b evalDown
     */
    @Test
    @Disabled  // See issue #34 in jAADD. The problem is using doubles for calculations, which results in rounding errors.
    fun testHATbEVALINTDown() = testSession  {
        loadSysMD(
            """
                attribute a: ScalarValues::Integer;
                attribute b: ScalarValues::Integer = 3;
                attribute y: ScalarValues::Integer(125..125) = a ^ b;
            """
        )
        propagate()
        assertEquals(3, global.resolveVar("b")!!.vectorQuantity.idd().getRange().min)
        assertEquals(5, global.resolveVar("a")!!.vectorQuantity.idd().getRange().min)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }
}
