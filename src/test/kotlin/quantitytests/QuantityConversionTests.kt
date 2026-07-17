package quantitytests

import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.*
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class QuantityConversionTests {
    private lateinit var ddDummy0: AADD
    private lateinit var ddDummy1: AADD
    private lateinit var ddDummy5: AADD
    private lateinit var ddDummy10: AADD
    private lateinit var ddDummy20: AADD
    private lateinit var ddDummy30: AADD
    private lateinit var ddDummy36: AADD
    private lateinit var ddDummy50: AADD
    private lateinit var ddDummy99: AADD
    private lateinit var ddDummy100: AADD
    private lateinit var ddDummy200: AADD
    private lateinit var ddDummy1000: AADD

    @BeforeTest
    fun setUp() {
        DDBuilder {
            ddDummy0 = real(0.0)
            ddDummy1 = real(1.0)
            ddDummy5 = real(5.0)
            ddDummy10 = real(10.0)
            ddDummy20 = real(20.0)
            ddDummy30 = real(30.0)
            ddDummy36 = real(36.0)
            ddDummy50 = real(50.0)
            ddDummy99 = real(99.0)
            ddDummy100 = real(100.0)
            ddDummy200 = real(200.0)
            ddDummy1000 = real(1000.0)
        }
    }

    @Test
    fun transform1() {
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

    @Test
    fun transform1b() {
        val u1 = Unit("m/s")
        val quantity = Quantity(ddDummy1, u1)
        val result = quantity.valueIn("km/h")
        assertEquals(3.6, (result as AADD).getRange().min, 0.00001)
        assert(result.getRange().contains(3.6))
    }

    @Test
    fun transform2() {
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

    @Test
    fun transform2b() {
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
    fun unitTransform3() {
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
    fun lengthConvert() {
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
    fun lengthConvert2() {
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
    fun massConvert1() {
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
    fun currency() {
        val q1 = Quantity(ddDummy1, Unit("EUR"))
        assertEquals(1.0, q1.getMinAsDouble(), 0.00001)
    }

    @Test
    fun electricalCharge() {
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
    fun massConvert2() {
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
    fun volume() {
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
    fun volume2() {
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
    fun area() {
        val q1 = Quantity(ddDummy1, Unit("ac"))
        val qResult1 = q1.valueIn("m^2")
        assertEquals(4046.86, qResult1.asAadd().getRange().min, 0.000001)

        val q3 = Quantity(ddDummy1, Unit("ha"))
        val qResult3 = q3.valueIn("m^2")
        assertEquals(10000.0, qResult3.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun area2() {
        val q2 = Quantity(ddDummy1, Unit("m^2"))
        val qResult2 = q2.valueIn("ac")
        assertEquals(0.000247105, qResult2.asAadd().getRange().min, 0.000001)

        val q3 = Quantity(ddDummy1, Unit("m^2"))
        val qResult3 = q3.valueIn("ha")
        assertEquals(0.0001, qResult3.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun speed() {
        val q1 = Quantity(ddDummy1, Unit("mph"))
        val qResult1 = q1.valueIn("m/s")
        assertEquals(0.44704, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("kt"))
        val qResult2 = q2.valueIn("m/s")
        assertEquals(0.514444444444444, qResult2.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun speed2() {
        val q1 = Quantity(ddDummy1, Unit("m/s"))
        val qResult1 = q1.valueIn("mph")
        assertEquals(2.23693629, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("m/s"))
        val qResult2 = q2.valueIn("kt")
        assertEquals(1.94384449, qResult2.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun power() {
        val q1 = Quantity(ddDummy1, Unit("W"))
        val qResult1 = q1.valueIn("HP")
        assertEquals(0.001359621155, qResult1.asAadd().getRange().min, 0.000001)

        val q2 = Quantity(ddDummy1, Unit("HP"))
        val qResult2 = q2.valueIn("W")
        assertEquals(735.499, qResult2.asAadd().getRange().min, 0.000001)
    }

    @Test
    fun pressure() {
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
    fun energy() {
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
    fun energy2() {
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

    @Test
    fun rad() {
        val q1 = Quantity(ddDummy1, Unit("Pi"))
        val qResult1 = q1.valueIn("1")
        assertEquals(3.1415926535, qResult1.asAadd().getRange().min, 0.00000001)

        val q2 = Quantity(ddDummy1, Unit("1"))
        val qResult2 = q2.valueIn("Pi")
        assertEquals(0.3183098862, qResult2.asAadd().getRange().min, 0.00000001)
    }

    @Test
    fun deg() {
        val q1 = Quantity(ddDummy1, Unit("°"))
        val qResult1 = q1.valueIn("1")
        assertEquals(2 * kotlin.math.PI / 360, qResult1.asAadd().getRange().min, 0.00000001)

        val q2 = Quantity(ddDummy1, Unit("1"))
        val qResult2 = q2.valueIn("°")
        assertEquals(360 / (2 * kotlin.math.PI), qResult2.asAadd().getRange().min, 0.00000001)
    }

    @Test
    fun radDeg() {
        val q1 = Quantity(ddDummy1, Unit("°"))
        val qResult1 = q1.valueIn("Pi")
        assertEquals(1 / 180.0, qResult1.asAadd().getRange().min, 0.00000001)

        val q2 = Quantity(ddDummy1, Unit("Pi"))
        val qResult2 = q2.valueIn("°")
        assertEquals(180.0, qResult2.asAadd().getRange().min, 0.00000001)
    }

    @Test
    fun difference() {
        val quant1 = Quantity(ddDummy10, "N")
        val quant2 = Quantity(ddDummy1, "N")
        assert(!quant1.unit.isDifference)
        assert(!quant2.unit.isDifference)
        val quantResult = quant1.minus(quant2)
        assert(quantResult.unit.isDifference)
        assertEquals("Force Difference", quantResult.getDomain())
    }

    @Test
    fun difference1() {
        val quant1 = Quantity(ddDummy10, "°C")
        val quant2 = Quantity(ddDummy1, "°C")
        assert(!quant1.unit.isDifference)
        assert(!quant2.unit.isDifference)
        val quantResult = quant1.minus(quant2)
        assert(quantResult.unit.isDifference)
        assertEquals("ThermodynamicTemperature Difference", quantResult.getDomain())
        assertEquals("K", quantResult.unit.toString())
        assertEquals(9.0, quantResult.value.asAadd().getRange().max, 0.00001)
    }

    @Test
    fun differenceTime() {
        val quant1 = Quantity(ddDummy10, "s")
        val quant2 = Quantity(ddDummy1, "s")
        assertFalse(quant1.unit.isDifference)
        assertFalse(quant2.unit.isDifference)
        val quantResult = quant1.minus(quant2)
        assert(quantResult.unit.isDifference)
        assertEquals("Duration", quant1.getDomain())
        assertEquals("Duration", quant2.getDomain())
        assertEquals("Duration Difference", quantResult.getDomain())
        assertEquals("s", quantResult.unit.toString())
    }

    @Test
    fun year() = testSession("ISQ") {
        loadKerML("""feature date1: ISQ::TimeValue [Year] = Year("2021").""")
        solver.propagate()
        assertEquals(1609459200.0,
            global.resolveVar("date1")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun year2() = testSession("ISQ") {
        loadKerML(
            """
            feature year: ISQ::TimeValue [Year] = Year("2022").
            feature time: ISQ::DurationValue [a] = 200.0 a.
            feature yearResult: ISQ::TimeValue [Year] = year + time."""
        )

        solver.propagate()
        assertEquals("2022", global.resolveVar("year")!!.vectorQuantity.toString())
        assertEquals("2222", global.resolveVar("yearResult")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun year3() = testSession("ISQ") {
        loadKerML(
            """
            feature year: ISQ::TimeValue [Year] = Year("2021");
            feature year2: ISQ::TimeValue [Year] = Year("2023");
            feature result: ISQ::DurationValue [a] = year2 - year;"""
        )

        solver.propagate()
        assertEquals(2.0, global.resolveVar("result")!!.vectorQuantity.valuesIn("a")[0].asAadd().min, 0.001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun dateTime() = testSession("ISQ") {
        loadKerML("""feature date1: ISQ::TimeValue = DateTime("2021-10-30T13:00:01+02:00");""")
        solver.propagate()
        assertEquals(1635591601.0,
            global.resolveVar("date1")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun dateTime2() = testSession("ISQ") {
        loadKerML("""feature date: ISQ::TimeValue [DateTime] = DateTime("2021-10-10T00:00");""")
        solver.propagate()
        assertEquals("2021-10-10T00:00", global.resolveVar("date")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun dateTimeDiff1() = testSession("ISQ") {
        loadKerML(
            """
            feature date1: ISQ::TimeValue = DateTime("2021-10-30T13:00:01+02:00");
            feature date2: ISQ::TimeValue = DateTime("2021-10-30T13:01:01+02:00");
            feature datediff: ISQ::DurationValue [s] = date2-date1;"""
        )

        solver.propagate()
        assertEquals(1635591601.0,
            global.resolveVar("date1")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals(1635591661.0,
            global.resolveVar("date2")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals(60.0,
            global.resolveVar("datediff")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun dateTimeDiff2() = testSession("ISQ") {
        loadKerML(
            """
            feature date1: ISQ::TimeValue = DateTime("2021-10-10T03:00:00+02:00");
            feature date2: ISQ::TimeValue = DateTime("2021-10-11T03:00:00+02:00");
            feature datediff: ISQ::DurationValue [h] = date2-date1;"""
        )

        solver.propagate()
        assertEquals(1633827600.0,
            global.resolveVar("date1")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals(1633914000.0,
            global.resolveVar("date2")!!.vectorQuantity.value.asAadd().getRange().max, 0.00001)
        assertEquals("24 h", global.resolveVar("datediff")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun dateTimeSum() = testSession("ISQ") {
        loadKerML("""
            feature date: ISQ::TimeValue = DateTime("2021-10-10T03:00:00");
            feature time: ISQ::DurationValue [a] = 1.0 a;
            feature dateResult: ISQ::TimeValue [DateTime] = date + time;"""
        )
        solver.propagate()
        assertEquals("2022-10-10T03:00", global.resolveVar("dateResult")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun date() = testSession("ISQ") {
        loadKerML("""
            feature date: ISQ::TimeValue [Date] = Date("2022-10-10");
            feature time: ISQ::DurationValue [d] = 0.5 d;
            feature dateResult: ISQ::TimeValue [Date] = date + time;"""
        )

        solver.propagate()
        assertEquals("2022-10-10", global.resolveVar("date")!!.vectorQuantity.toString())
        assertEquals("2022-10-11", global.resolveVar("dateResult")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun month() = testSession("ISQ") {
        loadKerML("""
            feature month: ISQ::TimeValue [Month] = Month("2022-10");
            feature time: ISQ::DurationValue [d] = 20.0 d;
            feature monthResult: ISQ::TimeValue [Month] = month + time;"""
        )
        solver.propagate()
        assertEquals("2022-10", global.resolveVar("month")!!.vectorQuantity.toString())
        assertEquals("2022-11", global.resolveVar("monthResult")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun month2() = testSession("ISQ") {
        loadKerML(
            """
            feature month: ISQ::TimeValue [Month] = Month("2022-10");
            feature time: ISQ::DurationValue [a] = 30.0 a;
            feature monthResult: ISQ::TimeValue [Month] = month + time;"""
        )
        solver.propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        assertEquals("2022-10", global.resolveVar("month")!!.vectorQuantity.toString())
        assertEquals("2052-10", global.resolveVar("monthResult")!!.vectorQuantity.toString())
    }

    @Test
    fun month3() = testSession("ISQ") {
        loadKerML("""
            feature month1: ISQ::TimeValue [Month] = Month("2021-10");
            feature month2: ISQ::TimeValue [Month] = Month("2023-10");
            feature time: ISQ::DurationValue [a] = month2 - month1;"""
        )
        solver.propagate()
        assertEquals("2021-10", global.resolveVar("month1")!!.vectorQuantity.toString())
        assertEquals("2023-10", global.resolveVar("month2")!!.vectorQuantity.toString())
        assertEquals("2 a", global.resolveVar("time")!!.vectorQuantity.toString())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun missingUnits() = testSession("ISQ") {
        loadKerML(input = """
                feature test1: ISQ::ThermodynamicTemperatureValue {:>> unit ="°C";}
                feature test2: ISQ::ThermodynamicTemperatureValue {:>> unit ="°F";}
                feature test3: ISQ::MassValue ;
                feature percentage: Quantities::ScalarQuantityValue[%].
            """)
        solver.propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun transformIntervalTemperature() {
        val u = Unit("°C")
        var ddRange: AADD? = null
        DDBuilder {
            ddRange = real(0.0 .. 10.0)
        }
        val quantity = Quantity(ddRange!!, u)
        val result = quantity.valueIn("K")
        assertEquals(273.15, (result as AADD).getRange().min, 0.0001)
        assertEquals(283.15, result.getRange().max, 0.0001)
    }

    @Test
    fun usdGbpCurrencies() {
        val q1 = Quantity(ddDummy1, Unit("USD"))
        assertEquals(1.0, q1.getMinAsDouble(), 0.00001)
        val q2 = Quantity(ddDummy1, Unit("GBP"))
        assertEquals(1.0, q2.getMinAsDouble(), 0.00001)
    }

    @Test
    fun volumeUnitsImperial() {
        // 1 pt (US liquid pint) = 473.176473 ml
        val q1 = Quantity(ddDummy1, "pt")
        assertEquals(0.000473176, q1.valueIn("m^3").asAadd().getRange().min, 0.000001)

        // 1 qt (US liquid quart) = 946.352946 ml
        val q2 = Quantity(ddDummy1, "qt")
        assertEquals(0.000946352, q2.valueIn("m^3").asAadd().getRange().min, 0.000001)

        // 1 gal (US liquid gallon) = 3.785411784 l
        val q3 = Quantity(ddDummy1, "gal")
        assertEquals(0.00378541, q3.valueIn("m^3").asAadd().getRange().min, 0.00001)

        // 1 bbl (US barrel) = 119.240471196 l
        val q4 = Quantity(ddDummy1, "bbl")
        assertEquals(0.11924, q4.valueIn("m^3").asAadd().getRange().min, 0.0001)
    }

    @Test
    fun specialDerivedUnits() {
        // steradian (sr) = m^2 / m^2 = 1
        assertEquals("1", Quantity(ddDummy1, "sr").unit.toString())

        // Siemens (S) = A / V = A^2 s^3 / kg m^2
        assertEquals("A^2 s^3 / kg m^2", Quantity(ddDummy1, "S").unit.toString())

        // Weber (Wb) = V s = kg m^2 / A s^2
        assertEquals("kg m^2 / A s^2", Quantity(ddDummy1, "Wb").unit.toString())

        // Tesla (T) = Wb / m^2 = kg / A s^2
        assertEquals("kg / A s^2", Quantity(ddDummy1, "T").unit.toString())

        // Lux (lx) = lm / m^2 = cd / m^2
        assertEquals("cd / m^2", Quantity(ddDummy1, "lx").unit.toString())

        // Becquerel (Bq) = 1 / s
        assertEquals("1 / s", Quantity(ddDummy1, "Bq").unit.toString())

        // Gray (Gy) = J / kg = m^2 / s^2
        assertEquals("m^2 / s^2", Quantity(ddDummy1, "Gy").unit.toString())

        // Stokes (St) kinematic viscosity = 10^-4 m^2 / s
        val qSt = Quantity(ddDummy1, "St")
        assertEquals("m^2 / s", qSt.unit.toString())
        assertEquals(0.0001, qSt.valueIn("m^2/s").asAadd().getRange().min, 0.0000001)

        // Stilb (sb) luminance = cd / cm^2 = 10^4 cd / m^2
        val qSb = Quantity(ddDummy1, "sb")
        assertEquals("cd / m^2", qSb.unit.toString())
        assertEquals(10000.0, qSb.valueIn("cd/m^2").asAadd().getRange().min, 0.0001)
    }

    @Test
    fun massFlowConversion() {
        val q1 = Quantity(ddDummy1, "kg/s")
        assertEquals(1.0, q1.getMinAsDouble(), 0.00001)

        val q2 = Quantity(ddDummy1, "g/s")
        assertEquals(0.001, q2.valueIn("kg/s").asAadd().getRange().min, 0.000001)
    }
}
