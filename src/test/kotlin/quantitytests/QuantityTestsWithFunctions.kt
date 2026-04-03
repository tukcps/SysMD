package quantitytests

import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.math.cos
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class QuantityTestsWithFunctions {

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit1() = testSession("ISQ", "Ranges")  {
        loadKerML("""
           feature a: ISQ::AreaValue {:>> range = "4.0..9.0";}
           feature b: ISQ::LengthValue  = sqrt(a); 
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals("m", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(2.0, global.resolveVar("b")!!.vectorQuantity.getMinAsDouble(), 0.00001)
        assertEquals(3.0, global.resolveVar("b")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
    }

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit2() = testSession("ISQ", "Ranges")  {
        loadKerML(
            """
            feature a: Quantities::ScalarQuantityValue {:>> unit = "Ohm^2"; :>> range = "4.0 .. 9.0";}
            feature b: ISQ::ResistanceValue = sqrt(a); """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals("2..3 Ω", global.resolveVar("b")!!.vectorQuantity.toString())
    }

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit3() = testSession("ISQ", "Ranges")  {
        loadKerML(
            """
            feature a: Quantities::ScalarQuantityValue {:>> unit = "Ohm^2 m^2"; :>> range = "2.0 .. 9.0";}
            feature b: Quantities::ScalarQuantityValue =sqrt(a){:>> unit = "Ohm m"; :>> range = "2.0 .. 9.0";}"""
        )
        solver.propagate()
        assertEquals("kg m^3 / A^2 s^3", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** Quantity with sqrt */
    @Test
    fun sqrt_unit4() = testSession("ISQ", "Ranges")  {
        loadKerML(
            """
            feature a: Quantities::ScalarQuantityValue {:>> unit = "Pa^4 J^2 V^8 / A^6 N^2"; :>> range = "2.0 .. 9.0";}
            feature b: Quantities::ScalarQuantityValue = sqrt(a){:>> unit = "Pa^2 J V^4 / A^3 N"; :>> range = "2.0 .. 9.0";}"""
        )
        solver.propagate()
        assertEquals("kg^6 m^7 / A^7 s^16", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** Quantity with no unit */
    @Test
    fun sqr_unit() = testSession("Ranges")  {
        loadKerML(
            """
            feature a: Ranges::RealInRange { :>> range = "2.0E16 .. 9.0E16";}
            feature b: ScalarValues::Real = sqr(a);"""
        )
        solver.propagate()
        assertEquals("400e30..8.1e33", global.resolveVar("b")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    /** Quantity with sqr */
    @Test
    fun sqr_unit1() = testSession("ISQ", "Ranges")  {
        loadKerML(
            """
            feature a: ISQ::LengthValue{:>> range = "2.0 .. 9.0";}
            feature b: ISQ::AreaValue = sqr(a);"""
        )

        solver.propagate()
        assertEquals("m^2", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /** Quantity with sqr */
    @Test
    fun sqr_unit2() = testSession("ISQ")  {
        loadKerML("""
            feature a: ISQ::ResistanceValue{ :>> range = "2.0 .. 9.0";}
            feature b: Quantities::ScalarQuantityValue = sqr(a){:>> unit = "Ohm^2";}
        """)
        solver.propagate()
        assertEquals("kg^2 m^4 / A^4 s^6", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals("Resistance", global.resolveVar("a")!!.vectorQuantity.getDomain())
        assertNoIssues()
    }

    /** Quantity with sqr */
    @Test
    fun sqr_unit3() = testSession("ISQ", "Ranges")  {
        loadKerML(
            """
            feature a: Quantities::ScalarQuantityValue {:>> unit = "Ohm m"; :>> range = "2.0 .. 9.0";}
            feature b: Quantities::ScalarQuantityValue =sqr(a){:>> unit = "Ohm^2 m^2"; :>> range = "2.0 .. 9.0";}""")
        solver.propagate()
        assertEquals("kg^2 m^6 / A^4 s^6", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    /**
     * Quantity with sqr
     */
    @Test
    fun sqr_unit4() = testSession("ISQ")  {
        loadKerML(
            """
            feature a: Quantities::ScalarQuantityValue { :>> unit = "Pa^2 J V^4 / A^3 N"; :>> range = "2.0 .. 9.0";}
            feature b: Quantities::ScalarQuantityValue = sqr(a) { :>> unit = "Pa^4 J^2 V^8 / A^6 N^2"; :>> range = "4.0 .. 81.0";} 
        """)
        solver.propagate()
        assertEquals("kg^12 m^14 / A^14 s^32", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertNoIssues()
    }

    @Test
    fun sinCosTest() = testSession("Ranges")  {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = "0.5 .. 0.5";}
            feature b: ScalarValues::Real=sin(a);
            feature c: ScalarValues::Real=cos(a);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.479425538604203,global.resolveVar("b")!!.vectorQuantity.value.asAadd().min, 0.0001)
        assertEquals(0.8775825618903725,global.resolveVar("c")!!.vectorQuantity.value.asAadd().min, 0.0001)
    }

    @Test
    fun sinCosTest2() = testSession("Ranges")  {
            loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "1.0 .. 1.0";}
            feature b: ScalarValues::Real=sin(a);
            feature c: ScalarValues::Real=cos(a);""")
            solver.propagate()
            assertNoIssues()
            assertEquals(0.8414709848078965,global.resolveVar("b")!!.vectorQuantity.value.asAadd().min, 0.0001)
            assertEquals(0.5403023058681394,global.resolveVar("c")!!.vectorQuantity.value.asAadd().min, 0.0001)
        }

    @Test
    fun sinCosTest3() = testSession("Ranges")  {
        loadKerML("""
            feature a: Ranges::RealInRange { :>> range = "0.5 .. 1.0";}
            feature b: ScalarValues::Real=sin(a);
            feature c: ScalarValues::Real=cos(a); """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(0.479425538604203,global.resolveVar("b")!!.vectorQuantity.value.asAadd().min, 0.0001)
        assertEquals(0.8414709848078965,global.resolveVar("b")!!.vectorQuantity.value.asAadd().max, 0.0001)
        assertEquals(0.5403023058681394,global.resolveVar("c")!!.vectorQuantity.value.asAadd().min, 0.0001)
        assertEquals(0.877582561890373,global.resolveVar("c")!!.vectorQuantity.value.asAadd().max, 0.0001)
    }

    @Test
    fun sinCosTestNegative() = testSession("Ranges")  {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "-1.0 .. -0.5";}
            feature b: ScalarValues::Real=sin(a);
            feature c: ScalarValues::Real=cos(a); """
        )
        solver.propagate()
        assertNoIssues()
        //assertEquals(sin(-0.5),global.resolveVar("b")!!.vectorQuantity.value.asAadd().min, 0.0001)
        //assertEquals(sin(-1.0),global.resolveVar("b")!!.vectorQuantity.value.asAadd().max, 0.0001)
        assertEquals(cos(-1.0),global.resolveVar("c")!!.vectorQuantity.value.asAadd().min, 0.0001)
        assertEquals(cos(-0.5),global.resolveVar("c")!!.vectorQuantity.value.asAadd().max, 0.0001)
    }

    /**
     * Quantity with ceil
     */
    @Test
    fun ceil_quantity() = testSession("ISQ")  {
        loadKerML("""
            feature a: ISQ::LengthValue = 1.5 [m];
            feature b: ISQ::LengthValue = ceil(a); 
        """)
        solver.propagate()
        assertEquals("m", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(2.0, global.resolveVar("b")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.0, global.resolveVar("b")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertNoIssues()
    }

    @Test
    fun ceil_quantity_int() = testSession("ScalarValues")  {
        loadKerML(
            """
            feature a: ScalarValues::Integer = [1..3]; 
            feature b: ScalarValues::Integer = ceil(a); 
        """)
        solver.propagate()
        assertEquals(2, global.resolveVar("b")!!.vectorQuantity.value.asIdd().min)
        assertEquals(3, global.resolveVar("b")!!.vectorQuantity.value.asIdd().max)
        assertNoIssues()
    }

    /**
     * Quantity with ceil
     */
    @Test
    fun floor_quantity() = testSession("ISQ", "Ranges")  {
        loadKerML("""
            feature a: ISQ::LengthValue(1.5..4.5);  
            feature b: ISQ::LengthValue = floor(a);
        """)
        solver.propagate()
        assertEquals("m", global.resolveVar("b")!!.vectorQuantity.unit.toString())
        assertEquals(1.0, global.resolveVar("b")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(4.0, global.resolveVar("b")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertNoIssues()
    }

    @Test
    fun floor_quantity_int() = testSession("ScalarValues")  {
        loadKerML("""
            feature a: ScalarValues::Integer = oneOf(1..3);
            feature b: ScalarValues::Integer = floor(a); 
        """)
        solver.propagate()
        assertEquals(1, global.resolveVar("b")!!.vectorQuantity.value.asIdd().min)
        assertEquals(2, global.resolveVar("b")!!.vectorQuantity.value.asIdd().max)
        assertNoIssues()
    }

    @Test
    fun floor_quantity_int1() = testSession("Ranges")  {
        loadKerML("""
          feature a: Ranges::IntegerInRange {:>> range = "-3..0";}
          feature b: ScalarValues::Integer = floor(a);
        """)
        solver.propagate()
        assertEquals(-3, global.resolveVar("b")!!.vectorQuantity.value.asIdd().min)
        assertEquals(-1, global.resolveVar("b")!!.vectorQuantity.value.asIdd().max)
        assertNoIssues()
    }


    /**
     * Quantity with max
     */
    @Test
    fun max_quantity() = testSession("ScalarValues")  {
        loadKerML("""
                feature a: ScalarValues::Real = 1.0;
                feature b: ScalarValues::Real = 2.0; 
                feature y: ScalarValues::Real = max(a, b); 
            """)
        solver.propagate()
        val y = global.resolveVar("y")!!
        assertEquals(2.0, y.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.0, y.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertNoIssues()
    }

    /**
     * Quantity with max
     */
    @Test
    fun max_quantity2() = testSession("ScalarValues")  {
        loadKerML("""
                feature a: ScalarValues::Integer = 1;
                feature b: ScalarValues::Integer = 2;
                feature y: ScalarValues::Integer = max(a, b).
            """)
        solver.propagate()
        val y = global.resolveVar("y")!!
        assertEquals(2.0, y.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.0, y.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertNoIssues()
    }


    /**
     * Quantity with min
     */
    @Test
    fun min_quantity() = testSession("ScalarValues")  {
        loadKerML("""
                feature a: ScalarValues::Real = 1.0;
                feature b: ScalarValues::Real = 2.0;
                feature y: ScalarValues::Real = min(a, b);
            """)
        val y = global.resolveVar("y")!!
        assertEquals(1.0, y.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, y.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertNoIssues()
    }


    /**
     * Quantity with max
     */
    @Test
    fun min_quantity2() = testSession("ScalarValues")  {
        loadKerML("""
                feature a: ScalarValues::Integer = 1;
                feature b: ScalarValues::Integer = 2;
                feature y: ScalarValues::Integer = min(a, b);
            """)
        solver.propagate()
        val y = global.resolveVar("y")!!
        assertEquals(1.0, y.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, y.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertNoIssues()
    }

    /**
     * Testcase for operation a^b with real
     */
    @Test
    fun testHATbReal() = testSession("ScalarValues")  {
        loadKerML("""
                feature a: ScalarValues::Real = 5.0;
                feature b: ScalarValues::Real = 3.0;
                feature y: ScalarValues::Real = a^b;
            """)
        assertEquals(125.0, global.resolveVar("y")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertNoIssues()
    }

    /**
     * Testcase for operation a^b with ScalarValues::Integer
     */
    @Test
    fun testHATbINT() = testSession("ScalarValues")  {
        loadKerML("""
                feature a: ScalarValues::Integer = 5;
                feature b: ScalarValues::Integer = 3;
                feature y: ScalarValues::Integer = a^b;
            """)
        assertEquals(125, global.resolveVar("y")!!.vectorQuantity.idd().getRange().min)
        assertNoIssues()
    }

    /**
     * Testcase for operation a^b evalDown
     */
    @Test
    fun testHATbEVALDown() = testSession("Ranges")  {
        loadKerML("""
                feature a: ScalarValues::Real;
                feature b: ScalarValues::Real = 3.0;
                feature y: Ranges::RealInRange = a ^ b{ :>> range = "125..125";}
        """)
        solver.propagate()
        assertEquals(5.0, global.resolveVar("a")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(5.0, global.resolveVar("a")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertNoIssues()
    }

    /**
     * Testcase for operation a^b evalDown
     */
    @Test @Ignore
    fun testHATbEVALINTDown() = testSession("Ranges")  {
        loadKerML("""
                feature a: ScalarValues::Integer;
                feature b: ScalarValues::Integer = 3;
                feature y: Ranges::IntegerInRange = a ^ b{ :>> range = "125..125";}
            """)
        solver.propagate()
        assertEquals(5, global.resolveVar("a")!!.vectorQuantity.idd().getRange().min)
        assertEquals(5, global.resolveVar("a")!!.vectorQuantity.idd().getRange().max)
        assertNoIssues()
    }

    @Test
    fun conversionTest1() = testSession("ISQ") {
        loadKerML(
            """
             feature t1: Quantities::ScalarQuantityValue = 1.0 [h^2]{:>> unit = "h^2";}
            feature t2: Quantities::ScalarQuantityValue = t1 {:>> unit = "min^2";}"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(3600.0, global.resolveVar("t2")!!.aadd().getRange().max, 0.0001)
    }

    @Test
    fun conversionTest2() = testSession("ISQ") {
        loadKerML("""
                feature t1: ISQ::AccelerationValue = 1.0 [km/min^2];
                feature t2: ISQ::AccelerationValue  = t1;
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.2777777, global.resolveVar("t2")!!.aadd().getRange().min, 0.0001)
    }

    @Test
    fun conversionTest3() = testSession("ISQ") {
        loadKerML("""
                feature t1: Quantities::ScalarQuantityValue  = 1.0 [N/m^2] { :>> unit = "N/m^2";}
                feature t2: Quantities::ScalarQuantityValue = t1 { :>> unit = "mN/dm^2";}
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(10.0, global.resolveVar("t2")!!.aadd().getRange().min, 0.0001)
    }

    @Test
    fun stringToStringTest() = testSession("ScalarValues") {
        loadKerML("""
            feature name: ScalarValues::String = "Hallo";
            """)
        solver.propagate()
        assertEquals("Hallo", global.resolveVar("name")!!.vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun unitConversationTest() = testSession("ISQ", "Ranges") {
        loadKerML("""
            feature t: ISQ::AccelerationValue{ :>> range = "-9.81";}
            feature s: ISQ::AccelerationValue = t;
        """
        )
        solver.propagate()
        assertEquals("-9.81 m/s^2", global.resolveVar("s")!!.vectorQuantity.toString())
        assertNoIssues()
    }
}
