package quantitytests

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.quantities.*
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass
import com.github.tukcps.sysmd.quantities.baseUnits.Time
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import util.testSession
import kotlin.math.ln
import kotlin.math.pow


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnitTests {
    private lateinit var ddDummy0: AADD
    private lateinit var ddDummy1: AADD
    private lateinit var ddDummy5: AADD
    private lateinit var ddDummy8: AADD
    private lateinit var ddDummy10: AADD
    private lateinit var ddDummy20: AADD
    private lateinit var ddDummy30: AADD
    private lateinit var ddDummy36: AADD
    private lateinit var ddDummy50: AADD
    private lateinit var ddDummy99: AADD
    private lateinit var ddDummy100: AADD
    private lateinit var ddDummy200: AADD
    private lateinit var ddDummy1000: AADD
    private lateinit var ddDummy1024: AADD
    private lateinit var ddDummyMinus50: AADD
    private lateinit var aaddDummy100: AADD
    private lateinit var aaddDummy1: AADD
    private lateinit var aaddDummy5: AADD
    private lateinit var iddDummy1: IDD
    private lateinit var iddDummy5: IDD
    private lateinit var iddDummy100: IDD
    private lateinit var IDDEmtpy: IDD

    private val precisionExpMinus6 = 0.000001


    @BeforeAll
    fun setUp() {
        DDBuilder {
            ddDummy0 = real(0.0)
            ddDummy1 = real(1.0)
            ddDummy5 = real(5.0)
            ddDummy8 = real(8.0)
            ddDummy10 = real(10.0)
            ddDummy20 = real(20.0)
            ddDummy30 = real(30.0)
            ddDummy36 = real(36.0)
            ddDummy50 = real(50.0)
            ddDummy99 = real(99.0)
            ddDummy100 = real(100.0)
            ddDummy200 = real(200.0)
            ddDummy1000 = real(1000.0)
            ddDummy1024 = real(1024.0)
            ddDummyMinus50 = real(-50.0)
            aaddDummy100 = real(-100.0..100.0)
            aaddDummy1 = real(-1.0..1.0)
            aaddDummy5 = real(-5.0..5.0)
            iddDummy1 = integer(-1L .. 1)
            iddDummy5 = integer(-5L .. 5)
            iddDummy100 = integer(-100L .. 100)
            IDDEmtpy = EmptyIntegerRange
        }
    }


    //----------------------------------------------------------
    //------------------PARSING-----------------------------
    //----------------------------------------------------------

    @Test
    fun parseTestAllSI_Units() {
        val u = Unit("m g s A mol cd kg")
        assertEquals("meter", u.unitSet.elementAt(0).name)
        assertEquals("gram", u.unitSet.elementAt(1).name)
        assertEquals("second", u.unitSet.elementAt(2).name)
        assertEquals("ampere", u.unitSet.elementAt(3).name)
        assertEquals("mole", u.unitSet.elementAt(4).name)
        assertEquals("candela", u.unitSet.elementAt(5).name)
        assertEquals("kilogram", u.unitSet.elementAt(6).name)
        assertEquals("A cd g kg m mol s", u.toString())
    }

    @Test
    fun parseTestSomeDerivedUnits() {
        val u = Unit("Hz N Sv")
        assertEquals("hertz", u.unitSet.elementAt(0).name)
        assertEquals("newton", u.unitSet.elementAt(1).name)
        assertEquals("sievert", u.unitSet.elementAt(2).name)
    }

    @Test
    fun parseTestTemperatures() {
        val u = Unit("°F")
        val q = Quantity(ddDummy1, u)
        assertEquals("Temperature", q.unit.getUnitDimension(1.0))

        val u2 = Unit("°C")
        val q2 = Quantity(ddDummy1, u2)
        assertEquals("Temperature", q2.unit.getUnitDimension(1.0))

        val u3 = Unit("K")
        val q3 = Quantity(ddDummy1, u3)
        assertEquals("Temperature", q3.unit.getUnitDimension(1.0))
    }

    @Test
    fun testExponents() {
        val u = Unit("m^2 A / s^3 N^4")
        assertEquals(2, u.unitSet.elementAt(0).exponent)
        assertEquals(1, u.unitSet.elementAt(1).exponent)
        assertEquals(-3, u.unitSet.elementAt(2).exponent)
        assertEquals(-4, u.unitSet.elementAt(3).exponent)
    }

    @Test
    fun testPrefixes() {
        val u = Unit("km dam YA μK")
        assertEquals(Kilo, u.unitSet.elementAt(0).prefix)
        assertEquals("meter", u.unitSet.elementAt(0).name)

        assertEquals(Deka, u.unitSet.elementAt(1).prefix)
        assertEquals("meter", u.unitSet.elementAt(1).name)

        assertEquals(Yotta, u.unitSet.elementAt(2).prefix)
        assertEquals("ampere", u.unitSet.elementAt(2).name)

        assertEquals(Micro, u.unitSet.elementAt(3).prefix)
        assertEquals("kelvin", u.unitSet.elementAt(3).name)
    }

    /**
     * Quantity constructor calls removePrefixes()
     */
    @Test
    fun removePrefixes() {
        val u = Unit("km")
        val quantity1 = Quantity(ddDummy1, u)
        assertEquals(1000.0, (quantity1.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", quantity1.unit.unitSet.elementAt(0).name)

        val u1 = Unit("1 / km")
        val quantity2 = Quantity(ddDummy1, u1)
        assertEquals(0.001, (quantity2.value as AADD).getRange().min, 0.000001)
        assertEquals(NoPrefix, quantity2.unit.unitSet.elementAt(0).prefix)
        assertEquals("meter", quantity2.unit.unitSet.elementAt(0).name)

        val u2 = Unit("kN")
        val quantity3 = Quantity(ddDummy1, u2)
        assertEquals(1000.0, (quantity3.value as AADD).getRange().min, 0.000001)
        assertEquals(NoPrefix, quantity3.unit.unitSet.elementAt(0).prefix)

    }

    /**
     * Quantity constructor calls toSI()
     */
    @Test
    fun toSi_CheckUnits() {
        val u = Unit("N")
        val quantity1 = Quantity(ddDummy1, u)
        assertEquals("kg m / s^2", quantity1.unit.toString())

        val u2 = Unit("1 / N")
        val quantity2 = Quantity(ddDummy1, u2)
        assertEquals("s^2 / kg m", quantity2.unit.toString())

        val u3 = Unit("km / h")
        val quantity3 = Quantity(ddDummy36, u3)
        assertEquals("m / s", quantity3.unit.toString())
        assertEquals(10.0, quantity3.value.asAadd().getRange().min, 0.000001)
        assert(quantity3.getRange().contains(10.0))

        val u4 = Unit("N")
        val quantity4 = Quantity(ddDummy1, u4)
        assertEquals("kg m / s^2", quantity4.unit.toString())
        assertEquals(1.0, quantity4.value.asAadd().getRange().min, 0.000001)
        assert(quantity4.getRange().contains(1.0))

        val u5 = Unit("J")
        val quantity5 = Quantity(ddDummy1, u5)
        assertEquals("kg m^2 / s^2", quantity5.unit.toString())
        assertEquals(1.0, quantity5.value.asAadd().getRange().min, 0.000001)
        assert(quantity5.getRange().contains(1.0))
    }

    /**
     * Quantity constructor calls toSI()
     */
    @Test
    fun toSi_CheckValues() {
        //1000 liters is a cubic meter
        val u = Unit("l")
        val quantity5 = Quantity(ddDummy1, u)
        assertEquals("m^3", quantity5.unit.toString())
        assertEquals(0.001, (quantity5.value as AADD).getRange().min, 0.0000001)
        assert(quantity5.getRange().contains(0.001))

    }

    @Test
    fun reduceRedundantUnitsTest() {
        val u = Unit("m m s m")
        u.reduceRedundantUnits()
        assertEquals("m^3 s", u.toString())

        val u1 = Unit("g m s m s / g A mol mol")
        u1.reduceRedundantUnits()
        assertEquals("m^2 s^2 / A mol^2", u1.toString())
    }

    @Test
    fun reduceRedundantUnitsTest2() {
        val u = Unit("s m / m s")
        u.reduceRedundantUnits()
        assertEquals("1", u.toString())

        val u1 = Unit("s^10 m / m^10 s")
        u1.reduceRedundantUnits()
        assertEquals("s^9 / m^9", u1.toString())

    }

    @Test
    fun compareToTest() {
        val u1 = Unit("m^2 / s")
        val u2 = Unit("m^2 / s")
        assertEquals(true, u1 == u2)

        val u3 = Unit("m / s")
        val u4 = Unit("m^2 / s")
        assertEquals(false, u3 == u4)
    }

    @Test
    fun transformToTest1() {
        val u1 = Unit("m")

        val quantity1 = Quantity(ddDummy1, u1)
        val result = quantity1.valueIn("km")
        assertEquals(0.001, (result as AADD).getRange().min, 0.000001)
        assert(result.getRange().contains(0.001))

        val u3 = Unit("kg m / s^2")

        val quantity2 = Quantity(ddDummy1, u3)
        val result2 = quantity2.valueIn("N")
        assertEquals(1.0, (result2 as AADD).getRange().min, 0.000001)
        assert(result2.getRange().contains(1.0))

        val u5 = Unit("g")

        val quantity3 = Quantity(ddDummy1, u5)
        val result3 = quantity3.valueIn("kg")
        assertEquals(0.001, (result3 as AADD).getRange().min, 0.000001)
        assert(result3.getRange().contains(0.001))

        val u7 = Unit("mg")

        val quantity4 = Quantity(ddDummy1, u7)
        val result4 = quantity4.valueIn("kg")
        assertEquals(0.000001, (result4 as AADD).getRange().min, 0.0000000001)
        assert(result4.getRange().contains(0.000001))
    }


    @Test // @kotlin.test.Ignore
    fun transformToTest1b() {
        val u1 = Unit("m/s")

        val quantity = Quantity(ddDummy1, u1)
        val result = quantity.valueIn("km/h")
        assertEquals(3.6, (result as AADD).getRange().min, 0.00001)
        assert(result.getRange().contains(3.6))
    }

    @Test
    fun transformToTest2() {
        val u1 = Unit("°C")
        val quantity1 = Quantity(ddDummy1, u1)
        val result1 = quantity1.valueIn("mK")
        assertEquals(274150.0, (result1 as AADD).getRange().min, 0.001)

        val u3 = Unit("°C")
        val quantity2 = Quantity(ddDummy1, u3)
        val result2 = quantity2.valueIn("K")
        assertEquals(274.15, (result2 as AADD).getRange().min, 0.001)

        val u5 = Unit("°C")
        val quantity3 = Quantity(ddDummy1, u5)
        val result4 = quantity3.valueIn("°C")
        assertEquals(1.0, (result4 as AADD).getRange().min, 0.001)
    }
    //----------------------------------------------------------
    //------------------TEMPERATURE-----------------------------
    //----------------------------------------------------------

    @Test
    fun transformToTest2b() {
        val u1 = Unit("°C")
        val quantity1 = Quantity(ddDummy0, u1)
        val result1 = quantity1.valueIn("K")
        val result1b = quantity1.valueIn("°F")
        val result1c = quantity1.valueIn("°C")
        assertEquals(273.15, (result1 as AADD).getRange().min, 0.0001)
        assertEquals(32.0, (result1b as AADD).getRange().min, 0.0001)
        assertEquals(0.0, (result1c as AADD).getRange().min, 0.0001)

        val u3 = Unit("°F")
        val quantity2 = Quantity(ddDummy0, u3)
        val result2 = quantity2.valueIn("K")
        val result2b = quantity2.valueIn("°C")
        val result2c = quantity2.valueIn("°F")
        assertEquals(255.3722222222222, (result2 as AADD).getRange().min, 0.001)
        assertEquals(-17.77777777777778, (result2b as AADD).getRange().min, 0.001)
        assertEquals(0.0, (result2c as AADD).getRange().min, 0.001)

        val u5 = Unit("K")
        val quantity3 = Quantity(ddDummy0, u5)
        val result4 = quantity3.valueIn("°C")
        val result4b = quantity3.valueIn("°F")
        val result4c = quantity3.valueIn("K")
        assertEquals(-273.15, (result4 as AADD).getRange().min, 0.001)
        assertEquals(-459.67, (result4b as AADD).getRange().min, 0.001)
        assertEquals(0.0, (result4c as AADD).getRange().min, 0.001)
    }

    @Test
    fun unitTransformTest3() {
        val result1 = Quantity(ddDummy1, Unit("m^2")).valueIn("km^2")
        assertEquals(0.000001, (result1 as AADD).getRange().min, 0.0000000001)
        val result11 = Quantity(ddDummy1, Unit("m^2")).valueIn("dm^2")
        assertEquals(100.0, (result11 as AADD).getRange().min, 0.00000001)
        val result12 = Quantity(ddDummy1, Unit("m^2")).valueIn("mm^2")
        assertEquals(1000000.0, (result12 as AADD).getRange().min, 0.001)
        val result13 = Quantity(ddDummy1, Unit("m^2")).valueIn("cm^2")
        assertEquals(10000.0, (result13 as AADD).getRange().min, 0.000001)

        val result2 = Quantity(ddDummy1, Unit("m^3")).valueIn("km^3")
        assertEquals(0.000000001, (result2 as AADD).getRange().min, 0.0000000001)
        val result21 = Quantity(ddDummy1, Unit("m^3")).valueIn("dm^3")
        assertEquals(1000.0, (result21 as AADD).getRange().min, 0.0000000001)
        val result22 = Quantity(ddDummy1, Unit("m^3")).valueIn("mm^3")
        assertEquals(1000000000.0, (result22 as AADD).getRange().min, 0.01)
        val result23 = Quantity(ddDummy1, Unit("m^3")).valueIn("cm^3")
        assertEquals(1000000.0, (result23 as AADD).getRange().min, 0.0000001)

        val result3 = Quantity(ddDummy1, Unit("m")).valueIn("km")
        assertEquals(0.001, (result3 as AADD).getRange().min, 0.000001)
        val result31 = Quantity(ddDummy1, Unit("m")).valueIn("dm")
        assertEquals(10.0, (result31 as AADD).getRange().min, 0.000001)
        val result32 = Quantity(ddDummy1, Unit("m")).valueIn("mm")
        assertEquals(1000.0, (result32 as AADD).getRange().min, 0.000001)
        val result33 = Quantity(ddDummy1, Unit("m")).valueIn("cm")
        assertEquals(100.0, (result33 as AADD).getRange().min, 0.000001)
    }

    @Test
    fun lengthConvertTest() {
        val q1 = Quantity(ddDummy1, Unit("inch"))
        val qResult1 = q1.valueIn("cm")
        assertEquals(2.54, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("ft"))
        val qResult2 = q2.valueIn("mm")
        assertEquals(304.8, qResult2.asAadd().getRange().min, 0.000001)

        val q3 = Quantity(ddDummy1, Unit("yd"))
        val qResult3 = q3.valueIn("dm")
        assertEquals(9.144, qResult3.asAadd().getRange().min, 0.000001)

        val q4 = Quantity(ddDummy1, Unit("mi"))
        val qResult4 = q4.valueIn("km")
        assertEquals(1.609344, qResult4.asAadd().getRange().min, 0.000001)

        val q5 = Quantity(ddDummy1, Unit("nmi"))
        val qResult5 = q5.valueIn("km")
        assertEquals(1.852, qResult5.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun lengthConvertTest2() {
        val q1 = Quantity(ddDummy1, Unit("cm"))
        val qResult1 = q1.valueIn("inch")
        assertEquals(0.39370078740157, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("m"))
        val qResult2 = q2.valueIn("ft")
        assertEquals(3.2808398950131, qResult2.asAadd().getRange().min, 0.000001)

        val q3 = Quantity(ddDummy1, Unit("dm"))
        val qResult3 = q3.valueIn("yd")
        assertEquals(0.10936132983377, qResult3.asAadd().getRange().min, 0.000001)

        val q4 = Quantity(ddDummy1, Unit("km"))
        val qResult4 = q4.valueIn("mi")
        assertEquals(0.62137119223733, qResult4.asAadd().getRange().min, 0.000001)

        val q5 = Quantity(ddDummy1, Unit("km"))
        val qResult5 = q5.valueIn("nmi")
        assertEquals(0.53995680345572, qResult5.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun massConvertTest1() {
        val q1 = Quantity(ddDummy1, Unit("lb"))
        val qResult1 = q1.valueIn("kg")
        assertEquals(0.45359237, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("oz"))
        val qResult2 = q2.valueIn("g")
        assertEquals(28.349523125, qResult2.asAadd().getRange().min, 0.000001)

        val q3 = Quantity(ddDummy1, Unit("ct"))
        val qResult3 = q3.valueIn("g")
        assertEquals(0.2, qResult3.asAadd().getRange().min, 0.000001)

        val q4 = Quantity(ddDummy1, Unit("gr"))
        val qResult4 = q4.valueIn("g")
        assertEquals(0.06479891, qResult4.asAadd().getRange().min, 0.000001)

        val q5 = Quantity(ddDummy1, Unit("t"))
        val qResult5 = q5.valueIn("g")
        assertEquals(1000000.0, qResult5.asAadd().getRange().min, 0.000001)

        val q6 = Quantity(ddDummy1, Unit("tn"))
        val qResult6 = q6.valueIn("g")
        assertEquals(907184.74, qResult6.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun currencyTest() {
        val q1 = Quantity(ddDummy1, Unit("€"))
        assertEquals(1.0, q1.getMinAsDouble(), 0.00001)
    }

    @Test
    fun electricalChargeTest() {
        val q1 = Quantity(ddDummy1, Unit("C"))
        val qResult1 = q1.valueIn("As")
        assertEquals(1.0, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("C"))
        val qResult2 = q2.valueIn("Ah")
        assertEquals(0.000277777777777, qResult2.asAadd().getRange().min, 0.00000001)

        val q3 = Quantity(ddDummy1, Unit("As"))
        val qResult3 = q3.valueIn("Ah")
        assertEquals(0.000277777777777, qResult3.asAadd().getRange().min, 0.00000001)

        val q4 = Quantity(ddDummy1, Unit("Ah"))
        val qResult4 = q4.valueIn("C")
        assertEquals(3600.0, qResult4.asAadd().getRange().min, 0.000001)

        val q5 = Quantity(ddDummy1, Unit("Ah"))
        val qResult5 = q5.valueIn("As")
        assertEquals(3600.0, qResult5.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun massConvertTest2() {
        val q1 = Quantity(ddDummy1, Unit("kg"))
        val qResult1 = q1.valueIn("lb")
        assertEquals(2.2046226218488, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("g"))
        val qResult2 = q2.valueIn("oz")
        assertEquals(0.03527396194958, qResult2.asAadd().getRange().min, 0.000001)

        val q3 = Quantity(ddDummy1, Unit("g"))
        val qResult3 = q3.valueIn("ct")
        assertEquals(5.0, qResult3.asAadd().getRange().min, 0.000001)

        val q4 = Quantity(ddDummy1, Unit("g"))
        val qResult4 = q4.valueIn("gr")
        assertEquals(15.432358352941, qResult4.asAadd().getRange().min, 0.000001)

        val q5 = Quantity(ddDummy1, Unit("g"))
        val qResult5 = q5.valueIn("t")
        assertEquals(0.000001, qResult5.asAadd().getRange().min, 0.000001)

        val q6 = Quantity(ddDummy1, Unit("g"))
        val qResult6 = q6.valueIn("tn")
        assertEquals(0.00000102311311, qResult6.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun volumeTest() {
        val q1 = Quantity(ddDummy1, Unit("l"))
        val qResult1 = q1.valueIn("m^3")
        assertEquals(0.001, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("pt"))
        val qResult2 = q2.valueIn("dm^3")
        assertEquals(0.473176473, qResult2.asAadd().getRange().min, 0.000001)

        val q3 = Quantity(ddDummy1, Unit("qt"))
        val qResult3 = q3.valueIn("dm^3")
        assertEquals(0.946352946, qResult3.asAadd().getRange().min, 0.000001)

        val q4 = Quantity(ddDummy1, Unit("gal"))
        val qResult4 = q4.valueIn("dm^3")
        assertEquals(3.785411784, qResult4.asAadd().getRange().min, 0.000001)

        val q5 = Quantity(ddDummy1, Unit("bbl"))
        val qResult5 = q5.valueIn("m^3")
        assertEquals(0.119240471196, qResult5.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun volumeTest2() {
        val q1 = Quantity(ddDummy1, Unit("m^3"))
        val qResult1 = q1.valueIn("l")
        assertEquals(1000.0, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("dm^3"))
        val qResult2 = q2.valueIn("pt")
        assertEquals(2.1133764188652, qResult2.asAadd().getRange().min, 0.000001)

        val q3 = Quantity(ddDummy1, Unit("dm^3"))
        val qResult3 = q3.valueIn("qt")
        assertEquals(1.0566882094326, qResult3.asAadd().getRange().min, 0.000001)

        val q4 = Quantity(ddDummy1, Unit("dm^3"))
        val qResult4 = q4.valueIn("gal")
        assertEquals(0.26417205235815, qResult4.asAadd().getRange().min, 0.000001)

        val q5 = Quantity(ddDummy1, Unit("m^3"))
        val qResult5 = q5.valueIn("bbl")
        assertEquals(8.3864143605761, qResult5.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun areaTest() {
        val q1 = Quantity(ddDummy1, Unit("ac"))
        val qResult1 = q1.valueIn("m^2")
        assertEquals(4046.86, qResult1.asAadd().getRange().min, 0.000001)

        val q3 = Quantity(ddDummy1, Unit("ha"))
        val qResult3 = q3.valueIn("m^2")
        assertEquals(10000.0, qResult3.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun areaTest2() {
        val q2 = Quantity(ddDummy1, Unit("m^2"))
        val qResult2 = q2.valueIn("ac")
        assertEquals(0.000247105, qResult2.asAadd().getRange().min, 0.000001)

        val q3 = Quantity(ddDummy1, Unit("m^2"))
        val qResult3 = q3.valueIn("ha")
        assertEquals(0.0001, qResult3.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun speedTest() {
        val q1 = Quantity(ddDummy1, Unit("mph"))
        val qResult1 = q1.valueIn("m/s")
        assertEquals(0.44704, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("kt"))
        val qResult2 = q2.valueIn("m/s")
        assertEquals(0.514444444444444, qResult2.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun speedTest2() {
        val q1 = Quantity(ddDummy1, Unit("m/s"))
        val qResult1 = q1.valueIn("mph")
        assertEquals(2.23693629, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("m/s"))
        val qResult2 = q2.valueIn("kt")
        assertEquals(1.94384449, qResult2.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun powerTest() {
        val q1 = Quantity(ddDummy1, Unit("W"))
        val qResult1 = q1.valueIn("HP")
        assertEquals(0.001359621155, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("HP"))
        val qResult2 = q2.valueIn("W")
        assertEquals(735.499, qResult2.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun pressureTest() {
        val q1 = Quantity(ddDummy1, Unit("bar"))
        val qResult1 = q1.valueIn("Pa")
        val qResult3 = q1.valueIn("psi")
        assertEquals(100000.0, qResult1.asAadd().getRange().min, 0.000001)
        assertEquals(14.503773773, qResult3.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("Pa"))
        val qResult2 = q2.valueIn("bar")
        val qResult4 = q2.valueIn("psi")
        assertEquals(0.00001, qResult2.asAadd().getRange().min, 0.00000001)
        assertEquals(0.00014503773773, qResult4.asAadd().getRange().min, 0.0000000001)

        val q3 = Quantity(ddDummy1, Unit("psi"))
        val qResult5 = q3.valueIn("bar")
        val qResult6 = q3.valueIn("Pa")
        assertEquals(0.06894757293, qResult5.asAadd().getRange().min, 0.00000001)
        assertEquals(6894.757293, qResult6.asAadd().getRange().min, 0.00001)
    }


    @Test
    fun energyTest() {
        val q1 = Quantity(ddDummy1, Unit("J"))
        val qResult1 = q1.valueIn("Ws")
        assertEquals(1.0, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("J"))
        val qResult2 = q2.valueIn("Wh")
        assertEquals(0.0002777777, qResult2.asAadd().getRange().min, 0.000001)

        val q3 = Quantity(ddDummy1, Unit("J"))
        val qResult3 = q3.valueIn("cal")
        assertEquals(0.239006, qResult3.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun energyTest2() {
        val q1 = Quantity(ddDummy1, Unit("W s"))
        val qResult1 = q1.valueIn("J")
        assertEquals(1.0, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("Wh"))
        val qResult2 = q2.valueIn("J")
        assertEquals(3600.0, qResult2.asAadd().getRange().min, 0.000001)

        val q3 = Quantity(ddDummy1, Unit("cal"))
        val qResult3 = q3.valueIn("J")
        assertEquals(4.184, qResult3.asAadd().getRange().min, 0.000001)
    }

    //----------------------------------------------------------
    //------------------ARITHMETICS-----------------------------
    //----------------------------------------------------------

    @Test
    fun multiplicationTest1() {
        val u1 = Unit("m / s")
        val u2 = Unit("s")

        val result = Quantity(ddDummy10, u1).times(Quantity(ddDummy20, u2))
        assertEquals(200.0, (result.value as AADD).getRange().min, 0.000001)

        val u3 = Unit("N")
        val u4 = Unit("m")

        val result2 = Quantity(ddDummy10, u3).times(Quantity(ddDummy20, u4))
        assertEquals(200.0, (result2.valueIn("N m") as AADD).getRange().min, 0.000001)

        val u5 = Unit("m")
        val u6 = Unit("1/m")
        val result3 = Quantity(ddDummy1, u5).times(Quantity(ddDummy1, u6))
        assert(result3.unit.unitSet.isEmpty())
    }

    @Test
    fun multiplicationTest2() {
        val u1 = Unit("km")
        val u2 = Unit("m")

        val result = Quantity(ddDummy1, u1).times(Quantity(ddDummy10, u2))
        assertEquals("m^2", result.unit.toString())
        assertEquals(10000.0, (result.value as AADD).getRange().min, 0.00001)
        val resultInKm2 = result.valueIn("km^2")
        assertEquals(0.01, (resultInKm2 as AADD).getRange().min, 0.000001)
    }

    @Test
    fun multiplicationPercentageTest() {
        val u1 = Unit("m")
        val u2 = Unit("%")

        val q1 = Quantity(ddDummy50, u2)
        var result = Quantity(ddDummy5, u1).times(q1)
        assertEquals(2.5, (result.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", result.unit.unitSet.elementAt(0).name)

        val q2 = Quantity(ddDummy50, u2)
        result = q2.times(Quantity(ddDummy5, u1))
        assertEquals(2.5, (result.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", result.unit.unitSet.elementAt(0).name)

        val q3 = Quantity(ddDummy5, u2)
        result = q3.times(Quantity(ddDummy50, u1))
        assertEquals(2.5, (result.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", result.unit.unitSet.elementAt(0).name)

        val q4 = Quantity(ddDummyMinus50, u2)
        result = Quantity(ddDummy5, u1).times(q4)
        assertEquals(-2.5, (result.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", result.unit.unitSet.elementAt(0).name)
    }

    /**
     * Quantity constructor calls makeCanonical()
     */
    @Test
    fun multiplicationOnlyPercentageTest() {
        val u1 = Unit("%")
        val u2 = Unit("%")

        val q1 = Quantity(ddDummyMinus50, u1)
        val q2 = Quantity(ddDummyMinus50, u2)
        var result = q1.times(q2)
        val valueInPercentage = result.valueIn("%")
        assertEquals(0.25, (result.value as AADD).getRange().min, 0.000001)
        assertEquals(25.0, (valueInPercentage.asAadd().getRange().min), 0.000001)

        val q3 = Quantity(ddDummy200, u1)
        val q4 = Quantity(ddDummy100, u2)
        result = q3.times(q4)
        assertEquals(2.0, (result.value as AADD).getRange().min, 0.000001)
    }


    @Test
    fun divisionTest1() {
        val u1 = Unit("m")
        val u2 = Unit("s")

        val result = Quantity(ddDummy10, u1).div(Quantity(ddDummy20, u2))
        assertEquals("m / s", result.unit.toString())
        assertEquals(0.5, (result.value as AADD).getRange().min, 0.000001)
    }

    @Test
    fun divisionTest2() {
        val u1 = Unit("km")
        val u2 = Unit("s")

        val result = Quantity(ddDummy10, u1).div(Quantity(ddDummy20, u2))
        assertEquals("m / s", result.unit.toString())
        assertEquals(500.0, (result.value as AADD).getRange().min, 0.000001)
    }

    @Test
    fun divisionPercentageTest() {
        val u1 = Unit("m")
        val u2 = Unit("%")

        val q1a = Quantity(ddDummy50, u2)
        var result = Quantity(ddDummy5, u1).div(q1a)
        assertEquals(10.0, (result.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", result.unit.unitSet.elementAt(0).name)

        val q1 = Quantity(ddDummy50, u2)
        val q2 = Quantity(ddDummy5, u1)
        result = q1.div(q2)
        assertEquals(0.1, (result.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", result.unit.unitSet.elementAt(0).name)

    }

    @Test
    fun divisionOnlyPercentageTest() {
        val u1 = Unit("%")
        val u2 = Unit("%")

        val result = Quantity(ddDummy50, u1).div(Quantity(ddDummy50, u2))
        assertEquals(1.0, (result.value as AADD).getRange().min, 0.000001)
    }


    @Test
    fun additionTest() {
        val u1 = Unit("kN")
        val u2 = Unit("kN")

        var result = Quantity(ddDummy1, u1).plus(Quantity(ddDummy10, u2))
        assertEquals("kg m / s^2", result.unit.toString())
        assertEquals(11000.0, (result.value as AADD).getRange().min, 0.00001)

        val q3 = Quantity(ddDummy1, Unit("%"))
        val q4 = Quantity(ddDummy10, Unit("%"))

        result = q3.plus(q4)
        assertEquals("1", result.unit.toString())
        assertEquals(0.11, (result.value as AADD).getRange().min, 0.00001)
    }

    @Test
    fun subtractionTest() {
        val u1 = Unit("kN")
        val u2 = Unit("N")

        val q1 = Quantity(ddDummy1, u1)
        val q2 = Quantity(ddDummy100, u2)
        var result = q1.minus(q2)
        assertEquals("kg m / s^2", result.unit.toString())
        assertEquals(900.0, (result.value as AADD).getRange().min, 0.001)

        val q3 = Quantity(ddDummy50, Unit("%"))
        val q4 = Quantity(ddDummy20, Unit("%"))

        result = q3.minus(q4)
        assertEquals("1", result.unit.toString())
        assertEquals(0.3, (result.value as AADD).getRange().min, 0.0001)
    }

    @Test
    fun sqrtTest() {
        val u = Unit("m^2")
        var result = Quantity(ddDummy100, u).sqrt()
        assertEquals(10.0, (result.value as AADD).getRange().min, precisionExpMinus6)
        assertEquals("m", result.unit.toString())

        val u1 = Unit("s^4 m^2")
        result = Quantity(ddDummy20, u1).sqrt()
        assertEquals(4.472135955, (result.value as AADD).getRange().min, precisionExpMinus6)
        assertEquals("m s^2", result.unit.toString())
    }


    //----------------------------------------------------------
    //------------------QUANTITIES-----------------------------
    //----------------------------------------------------------

    @Test
    fun bytesTest() { // with prefix Mi, Gi ..
        val quant1 = Quantity(ddDummy100, "B")
        assertEquals(800.0, quant1.getMinAsDouble(), 0.0000001)
        assertEquals("bit", quant1.unit.toString())
        assertEquals("InformationCapacity", quant1.getDimension())

        val quant2 = Quantity(ddDummy1, "kiB")
        assertEquals(8192.0, quant2.getMinAsDouble(), 0.0000001)
        assertEquals("bit", quant2.unit.toString())

        val quant3 = Quantity(ddDummy1, "KiB/s")
        assertEquals(8.0, quant3.valueIn("Kibit/s").asAadd().getRange().min, 0.0000001)

        val quant4 = Quantity(ddDummy8, "MiBit/s")
        assertEquals(1024.0, quant4.valueIn("KiB/s").asAadd().getRange().min, 0.0000001)

        val quant5 = Quantity(ddDummy1, "MiByte/s")
        val quant6 = Quantity(ddDummy8, "MiBit/s")
        assertEquals(16.0, quant5.plus(quant6).valueIn("MiBit/s").asAadd().getRange().min, 0.0000001)
    }

    @Test
    fun bytesTest2() { // with prefix M, G ...
        val quant1 = Quantity(ddDummy100, "B")
        assertEquals(800.0, quant1.getMinAsDouble(), 0.0000001)
        assertEquals("bit", quant1.unit.toString())

        val quant2 = Quantity(ddDummy1, "kB")
        assertEquals(8000.0, quant2.getMinAsDouble(), 0.0000001)
        assertEquals("bit", quant2.unit.toString())

        val quant3 = Quantity(ddDummy1, "KB/s")
        assertEquals(8.0, quant3.valueIn("Kbit/s").asAadd().getRange().min, 0.0000001)

        val quant4 = Quantity(ddDummy8, "MBit/s")
        assertEquals(1000.0, quant4.valueIn("KB/s").asAadd().getRange().min, 0.0000001)

        val quant5 = Quantity(ddDummy1, "MByte/s")
        val quant6 = Quantity(ddDummy8, "MBit/s")
        assertEquals(16.0, quant5.plus(quant6).valueIn("MBit/s").asAadd().getRange().min, 0.0000001)
    }

    @Test
    fun decibelTest1() {
        val quant1 = Quantity(ddDummy10, "dB")
        assertEquals(10.0, quant1.getMinAsDouble(), 0.0000001)
        assertEquals("1", quant1.unit.toString())
        assertEquals("QuantityOfDimensionOne", quant1.getDimension())

        val quant2 = Quantity(ddDummy20, "dB")
        assertEquals(100.0, quant2.getMinAsDouble(), 0.0000001)
        assertEquals("1", quant2.unit.toString())

        val quant3 = Quantity(ddDummy30, "dB")
        assertEquals(1000.0, quant3.getMinAsDouble(), 0.0000001)
        assertEquals("1", quant3.unit.toString())

        val quant4 = Quantity(ddDummy100, "dB")
        assertEquals(10000000000.0, quant4.getMinAsDouble(), 0.001)
        assertEquals("1", quant4.unit.toString())
    }

    @Test
    fun decibelTest2() {
        val quant1 = Quantity(ddDummy10, "1")
        assertEquals(10.0, quant1.valueIn("dB").asAadd().getRange().min, 0.0000001)
        // assertEquals("dB",quant1.transformTo(Unit("dB")).unit.toString())

        val quant2 = Quantity(ddDummy100, "1")
        assertEquals(20.0, quant2.valueIn("dB").asAadd().getRange().min, 0.0000001)

        val quant3 = Quantity(ddDummy1000, "1")
        assertEquals(30.0, quant3.valueIn("dB").asAadd().getRange().min, 0.0000001)
    }

    @Test
    fun decibelTestAddition() {
        val quant1 = Quantity(ddDummy10, "dB")
        val quant2 = Quantity(ddDummy10, "dB")
        assertEquals(
            10.0 * ln(10.0.pow(1.0) + 10.0.pow(1.0)) / ln(10.0),
            (quant1 + quant2).valueIn("dB").asAadd().getRange().min,
            0.0000001
        )

        val quant3 = Quantity(ddDummy99, "dB")
        val quant4 = Quantity(ddDummy100, "dB")
        assertEquals(
            10.0 * ln(10.0.pow(10.0) + 10.0.pow(9.9)) / ln(10.0),
            (quant3 + quant4).valueIn("dB").asAadd().getRange().min,
            0.0000001
        )
    }

    @Test
    fun decibelTestSubtraction() {
        val quant1 = Quantity(ddDummy10, "dB")
        val quant2 = Quantity(ddDummy10, "dB")
        assertEquals(
            10.0 * ln(10.0.pow(1.0) - 10.0.pow(1.0)) / ln(10.0),
            (quant1 - quant2).valueIn("dB").asAadd().getRange().min,
            0.0000001
        )

        val quant3 = Quantity(ddDummy99, "dB")
        val quant4 = Quantity(ddDummy100, "dB")
        assertEquals(
            10.0 * ln(10.0.pow(10.0) - 10.0.pow(9.9)) / ln(10.0),
            (quant4 - quant3).valueIn("dB").asAadd().getRange().min,
            0.0000001
        )
    }

    @Test
    fun quantityTest1() {
        var quant1 = Quantity(ddDummy100, "m")
        var quant2 = Quantity(ddDummy50, "%")
        var quantResult = quant1 * quant2
        assertEquals(50.0, quantResult.getMinAsDouble(), 0.000001)
        assertEquals("m", quantResult.unit.toString())

        quant1 = Quantity(ddDummy20, "%")
        quant2 = Quantity(ddDummy10, "%")
        quantResult = quant1 + quant2
        assert(quantResult.getRange().contains(0.3))
        assertEquals("1", quantResult.unit.toString())
    }

    @Test
    fun radTest() {
        val q1 = Quantity(ddDummy1, Unit("Pi"))
        val qResult1 = q1.valueIn("1") //1 same as rad (normal without Pi)
        assertEquals(3.1415926535, qResult1.asAadd().getRange().min, 0.00000001)

        val q2 = Quantity(ddDummy1, Unit("1"))  //1 same as rad (normal without Pi)
        val qResult2 = q2.valueIn("Pi")
        assertEquals(0.3183098862, qResult2.asAadd().getRange().min, 0.00000001)
    }

    @Test
    fun degTest() {
        val q1 = Quantity(ddDummy1, Unit("°"))
        val qResult1 = q1.valueIn("1")
        assertEquals(2 * kotlin.math.PI / 360, qResult1.asAadd().getRange().min, 0.00000001)

        val q2 = Quantity(ddDummy1, Unit("1"))
        val qResult2 = q2.valueIn("°")
        assertEquals(360 / (2 * kotlin.math.PI), qResult2.asAadd().getRange().min, 0.00000001)
    }

    @Test
    fun radDegTest() {
        val q1 = Quantity(ddDummy1, Unit("°"))
        val qResult1 = q1.valueIn("Pi")
        assertEquals(1 / 180.0, qResult1.asAadd().getRange().min, 0.00000001)

        val q2 = Quantity(ddDummy1, Unit("Pi"))
        val qResult2 = q2.valueIn("°")
        assertEquals(180.0, qResult2.asAadd().getRange().min, 0.00000001)
    }

    /**
     * Quantity constructor calls makeCanonical()
     */
    @Test
    fun quantityMakeCanonicalTest() {
        var quant1 = Quantity(ddDummy1, "kN")
        assertEquals(1000.0, quant1.getMinAsDouble(), 0.000001)
        assertEquals("kg m / s^2", quant1.unit.toString())

        quant1 = Quantity(ddDummy1, "m^2/m")
        assertEquals(1.0, quant1.getMinAsDouble(), 0.000001)
        assert(quant1.getRange().contains(1.0))
        assertEquals("m", quant1.unit.toString())

        quant1 = Quantity(ddDummy1, "m/m")
        assertEquals(1.0, quant1.getMinAsDouble(), 0.000001)
        assert(quant1.getRange().contains(1.0))
        assertEquals("1", quant1.unit.toString())
    }

    // Quantities should not be modified during Quantity executions
    @Test
    fun modifyTest() {
        val quant = Quantity(ddDummy1, "m^2/s^2")
        val quantCopy = quant.clone()
        val quant2 = Quantity(ddDummy10, "km^2/h^2")
        val quant2Copy = quant2.clone()
        assertEquals(quantCopy, quant)
        quant.sqr()
        assertEquals(quantCopy, quant)
        quant.sqrt()
        assertEquals(quantCopy, quant)
        quant.times(quant2)
        assertEquals(quant2Copy, quant2)
        assertEquals(quantCopy, quant)
        quant.plus(quant2)
        assertEquals(quant2Copy, quant2)
        assertEquals(quantCopy, quant)
        quant.div(quant2)
        assertEquals(quant2Copy, quant2)
        assertEquals(quantCopy, quant)
        quant.minus(quant2)
        assertEquals(quant2Copy, quant2)
        assertEquals(quantCopy, quant)
        quant.valueIn(quant2.unit.toString())
        assertEquals(quant2Copy, quant2)
        assertEquals(quantCopy, quant)
    }

    @Test
    fun intersectTest() {
        val p = VariableImplementation(FeatureImplementation(unitConstraint = "m"))
        p.vectorQuantity = Quantity(aaddDummy100, "cm")
        val upQuantity = Quantity(aaddDummy5, "m")
        assertEquals(-1.0, p.vectorQuantity.intersect(upQuantity).value.asAadd().getRange().min, 0.000001)
        assertEquals(1.0, p.vectorQuantity.intersect(upQuantity).value.asAadd().getRange().max, 0.000001)
    }

    @Test
    fun intersectTestInt() {
        val p = VariableImplementation(FeatureImplementation(unitConstraint = "1"))
        p.vectorQuantity = Quantity(iddDummy1)
        val upQuantity = Quantity(iddDummy5)
        assertEquals(-1, p.vectorQuantity.intersect(upQuantity).value.asIdd().getRange().min)
        assertEquals(1, p.vectorQuantity.intersect(upQuantity).value.asIdd().getRange().max)
    }

    @Test
    fun constraintTest() {
        val p = VariableImplementation(FeatureImplementation(unitConstraint = "m"))
        p.valueSpecs = mutableListOf(Range("-0.5..2"))
        p.vectorQuantity = Quantity(aaddDummy100, "cm")
        assertEquals(
            -0.5,
            p.vectorQuantity.constrain(p.vectorQuantity, p.rangeSpecs, p.unitSpec).value.asAadd().getRange().min,
            0.000001
        )
        assertEquals(
            1.0,
            p.vectorQuantity.constrain(p.vectorQuantity, p.rangeSpecs, p.unitSpec).value.asAadd().getRange().max,
            0.000001
        )
    }

    @Test
    fun constraintTestInt() {
        val p = VariableImplementation(FeatureImplementation(unitConstraint = "1"))
        p.vectorQuantity = Quantity(iddDummy1)
        p.valueSpecs = mutableListOf(IntegerRange("0..2"))
        assertEquals(0, p.vectorQuantity.constrain(p.intSpecs).value.asIdd().getRange().min)
        assertEquals(1, p.vectorQuantity.constrain(p.intSpecs).value.asIdd().getRange().max)
    }

    @Test
    fun emptyIDDDTest() {
        val quantity2 = Quantity(IDDEmtpy)
        assertEquals("∅", quantity2.toString())
    }

    // The units °C, °F and % do work
    @Test
    fun missingUnitsTest() = testSession("SI") {
        loadKerML(
            input = """
            // The units °C, °F and % do not work
                feature test1: SI::Temperature[°C];
                feature test2: SI::Temperature[°F];
                feature test3: SI::Mass ;
                feature percentage: SI::Quantity[%].
            """
        )
        initialize()
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun differenceTest() {
        val quant1 = Quantity(ddDummy10, "N")
        val quant2 = Quantity(ddDummy1, "N")
        assert(!quant1.unit.isDifference)
        assert(!quant2.unit.isDifference)
        val quantResult = quant1.minus(quant2)
        assert(quantResult.unit.isDifference)
        assertEquals("Force Difference", quantResult.getDimension())
    }

    @Test
    fun differenceTest1() {
        val quant1 = Quantity(ddDummy10, "°C")
        val quant2 = Quantity(ddDummy1, "°C")
        assert(!quant1.unit.isDifference)
        assert(!quant2.unit.isDifference)
        val quantResult = quant1.minus(quant2)
        assert(quantResult.unit.isDifference)
        assertEquals("Temperature Difference", quantResult.getDimension())
        assertEquals("K", quantResult.unit.toString())
        assertEquals(9.0, quantResult.value.asAadd().getRange().max, 0.00001)
    }

    @Test
    fun differenceTimeTest() {
        val quant1 = Quantity(ddDummy10, "s")
        val quant2 = Quantity(ddDummy1, "s")
        assertFalse(quant1.unit.isDifference)
        assertFalse(quant2.unit.isDifference)
        val quantResult = quant1.minus(quant2)
        assert(quantResult.unit.isDifference)
        assertEquals("Time", quant1.getDimension())
        assertEquals("Time", quant2.getDimension())
        assertEquals("Time Difference", quantResult.getDimension())
        assertEquals("s", quantResult.unit.toString())
    }

    @Test
    fun testYear() = testSession("SI") {
        loadKerML("""feature date1: SI::Time [Year] = Year("2021").""")
        propagate()
        assertEquals(1609459200.0,
            global.resolveVar("date1")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun testYear2() = testSession("SI") {
        loadKerML(
            """
            feature year: SI::Time [Year] = Year("2022").
            feature time: SI::Time [a] = 200.0 a.
            feature yearResult: SI::Time [Year] = year + time."""
        )

        propagate()
        assertEquals("2022", global.resolveVar("year")!!.vectorQuantity.toString())
        assertEquals("2222", global.resolveVar("yearResult")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun testYear3() = testSession("SI") {
        loadKerML(
            """
            feature year: SI::Time [Year] = Year("2021");
            feature year2: SI::Time [Year] = Year("2023");
            feature result: SI::Time [a] = year2 - year;"""
        )

        propagate()
        assertEquals(2.0, global.resolveVar("result")!!.vectorQuantity.valuesIn("a")[0].asAadd().min, 0.001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun testDateTime() = testSession("SI") {
        loadKerML("""feature date1: SI::Time = DateTime("2021-10-30T13:00:01+02:00");""")
        propagate()
        assertEquals(1635591601.0,
            global.resolveVar("date1")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun testDateTime2() = testSession("SI") {
        loadKerML("""feature date: SI::Time [DateTime] = DateTime("2021-10-10T00:00");""")
        propagate()
        assertEquals("2021-10-10T00:00", global.resolveVar("date")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }


    @Test
    fun testDateTimeDiff1() = testSession("SI") {
        loadKerML(
            """
            feature date1: SI::Time = DateTime("2021-10-30T13:00:01+02:00");
            feature date2: SI::Time = DateTime("2021-10-30T13:01:01+02:00");
            feature datediff: SI::Time [s] = date2-date1;"""
        )

        propagate()
        assertEquals(1635591601.0,
            global.resolveVar("date1")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals(1635591661.0,
            global.resolveVar("date2")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals(60.0,
            global.resolveVar("datediff")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun testDateTimeDiff2() = testSession("SI") {
        loadKerML(
            """
            feature date1: SI::Time = DateTime("2021-10-10T03:00:00+02:00");
            feature date2: SI::Time = DateTime("2021-10-11T03:00:00+02:00");
            feature datediff: SI::Time [h] = date2-date1;"""
        )

        propagate()
        assertEquals(1633827600.0,
            global.resolveVar("date1")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals(1633914000.0,
            global.resolveVar("date2")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals("24 h", global.resolveVar("datediff")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun testDateTimeSum() = testSession("SI") {
        loadKerML("""
            feature date: SI::Time = DateTime("2021-10-10T03:00:00");
            feature time: SI::Time [a] = 1.0 a;
            feature dateResult: SI::Time [DateTime] = date + time;"""
        )
        propagate()
        assertEquals("2022-10-10T03:00", global.resolveVar("dateResult")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun dateTest() = testSession("SI") {
        loadKerML("""
            feature date: SI::Time [Date] = Date("2022-10-10");
            feature time: SI::Time [d] = 0.5 d;
            feature dateResult: SI::Time [Date] = date + time;"""
        )

        propagate()
        assertEquals("2022-10-10", global.resolveVar("date")!!.vectorQuantity.toString())
        assertEquals("2022-10-11", global.resolveVar("dateResult")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun monthTest() = testSession("SI") {
        loadKerML("""
            feature month: SI::Time [Month] = Month("2022-10");
            feature time: SI::Time [d] = 20.0 d;
            feature monthResult: SI::Time [Month] = month + time;"""
        )
        propagate()
        assertEquals("2022-10", global.resolveVar("month")!!.vectorQuantity.toString())
        assertEquals("2022-11", global.resolveVar("monthResult")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun monthTest2() = testSession("SI") {
        loadKerML(
            """
            feature month: SI::Time [Month] = Month("2022-10");
            feature time: SI::Time [a] = 30.0 a;
            feature monthResult: SI::Time [Month] = month + time;"""
        )
        propagate()
        assertEquals("2022-10", global.resolveVar("month")!!.vectorQuantity.toString())
        assertEquals("2052-10", global.resolveVar("monthResult")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun monthTest3() = testSession("SI") {
        loadKerML("""
            feature month1: SI::Time [Month] = Month("2021-10");
            feature month2: SI::Time [Month] = Month("2023-10");
            feature time: SI::Time [a] = month2 - month1;"""
        )
        propagate()
        assertEquals("2021-10", global.resolveVar("month1")!!.vectorQuantity.toString())
        assertEquals("2023-10", global.resolveVar("month2")!!.vectorQuantity.toString())
        assertEquals("2 a", global.resolveVar("time")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun addUnitTest1() {
        addUnit("testunit", "tu", "Nm", "testdimension", 5.0, false)
        val unit = Unit("tu")
        assertEquals("testunit", unit.unitSet.elementAt(0).name)
        assertEquals("tu", unit.unitSet.elementAt(0).symbol)
        assertEquals("testdimension", unit.unitSet.elementAt(0).dimension)
        assertEquals(5.0, unit.unitSet.elementAt(0).convFac)
        assertFalse(unit.unitSet.elementAt(0).isLogarithmic)
        val baseUnitSet = mutableSetOf(Length.Meter.copy(2), Mass.Kilogram.copy(1), Time.Second.copy(-2))
        assertEquals(baseUnitSet, unit.unitSet.elementAt(0).getBaseUnits())
    }

    @Test
    fun addUnitTest2() {
        addUnit("testunit", "tu", "Nm", "testdimension", 5.0, false)
        val quantity = Quantity(ddDummy1, "tu")
        assertEquals(5.0, quantity.valueIn("Nm").asAadd().getRange().min, 0.00001)
    }

    @Test
    fun addZeroTest() {
        val q1 = Quantity(ddDummy10, "m")
        val q2 = Quantity(ddDummy0, "")
        val qResult = q1.plus(q2)
        assertEquals(10.0, qResult.value.asAadd().getRange().min, 0.000001)
        assertEquals(10.0, qResult.value.asAadd().getRange().max, 0.000001)
    }


    @Test
    fun unitsMixed() = testSession("SI") {
        loadKerML("""
                feature percentage: SI::Quantity [%] = 10.0 [%];
                feature number: ScalarValues::Real = 1.0;
                feature result: ScalarValues::Real = percentage + number;
                feature ratio: SI::Quantity [dB] = 10.0 [dB];
                feature result2: ScalarValues::Real[1] = ln(ratio)/ln(10.0);
                feature result3: ScalarValues::Real = power2(ratio);
                feature result3: ScalarValues::Real = power2(ratio);""")
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        propagate()
        assertEquals(0.1, global.resolveVar("percentage")!!.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(1.0, global.resolveVar("number")!!.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(1.1, global.resolveVar("result")!!.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(
            10.0,
            global.resolveVar("ratio")!!.vectorQuantity.valuesIn("dB")[0].asAadd().getRange().min,
            0.0000001
        )
        assertEquals(1.0, global.resolveVar("result2")!!.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(1024.0, global.resolveVar("result3")!!.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }
}
