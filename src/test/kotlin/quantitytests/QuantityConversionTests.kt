package quantitytests

import util.variable
import com.github.tukcps.sysmd.quantities.ConversionTables
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.dd.AADD
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadKerML
import util.testSession
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

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
        val quantity1 = VectorQuantity(ddDummy1, u1)
        val result = quantity1.valueIn("km")
        assertBounds(0.001, (result as AADD))

        val u3 = Unit("kg m / s^2")
        val quantity2 = VectorQuantity(ddDummy1, u3)
        val result2 = quantity2.valueIn("N")
        assertBounds(1.0, (result2 as AADD))

        val u5 = Unit("g")
        val quantity3 = VectorQuantity(ddDummy1, u5)
        val result3 = quantity3.valueIn("kg")
        assertBounds(0.001, (result3 as AADD))

        val u7 = Unit("mg")
        val quantity4 = VectorQuantity(ddDummy1, u7)
        val result4 = quantity4.valueIn("kg")
        assertBounds(0.000001, (result4 as AADD))
    }

    @Test
    fun transform1b() {
        val u1 = Unit("m/s")
        val quantity = VectorQuantity(ddDummy1, u1)
        val result = quantity.valueIn("km/h")
        assertBounds(3.6..3.6, (result as AADD))
    }

    @Test
    fun transform2() {
        val u1 = Unit("°C")
        val quantity1 = VectorQuantity(ddDummy1, u1)
        val result1 = quantity1.valueIn("mK")
        assertBounds(274150.0, (result1 as AADD))

        val u3 = Unit("°C")
        val quantity2 = VectorQuantity(ddDummy1, u3)
        val result2 = quantity2.valueIn("K")
        assertBounds(274.15, (result2 as AADD))

        val u5 = Unit("°C")
        val quantity3 = VectorQuantity(ddDummy1, u5)
        val result4 = quantity3.valueIn("°C")
        assertBounds(1.0, (result4 as AADD))
    }

    @Test
    fun transform2b() {
        val u1 = Unit("°C")
        val quantity1 = VectorQuantity(ddDummy0, u1)
        val result1 = quantity1.valueIn("K")
        val result1b = quantity1.valueIn("°F")
        val result1c = quantity1.valueIn("°C")
        assertBounds(273.15, (result1 as AADD))
        assertBounds(32.0, (result1b as AADD))
        assertBounds(0.0, (result1c as AADD))

        val u3 = Unit("°F")
        val quantity2 = VectorQuantity(ddDummy0, u3)
        val result2 = quantity2.valueIn("K")
        val result2b = quantity2.valueIn("°C")
        val result2c = quantity2.valueIn("°F")
        assertBounds(255.3722222222222, (result2 as AADD))
        assertBounds(-17.77777777777778, (result2b as AADD))
        assertBounds(0.0, (result2c as AADD))

        val u5 = Unit("K")
        val quantity3 = VectorQuantity(ddDummy0, u5)
        val result4 = quantity3.valueIn("°C")
        val result4b = quantity3.valueIn("°F")
        val result4c = quantity3.valueIn("K")
        assertBounds(-273.15, (result4 as AADD))
        assertBounds(-459.67, (result4b as AADD))
        assertBounds(0.0, (result4c as AADD))
    }

    @Test
    fun unitTransform3() {
        val result1 = VectorQuantity(ddDummy1, Unit("m^2")).valueIn("km^2")
        assertBounds(0.000001, (result1 as AADD))
        val result11 = VectorQuantity(ddDummy1, Unit("m^2")).valueIn("dm^2")
        assertBounds(100.0, (result11 as AADD))
        val result12 = VectorQuantity(ddDummy1, Unit("m^2")).valueIn("mm^2")
        assertBounds(1000000.0, (result12 as AADD))
        val result13 = VectorQuantity(ddDummy1, Unit("m^2")).valueIn("cm^2")
        assertBounds(10000.0, (result13 as AADD))

        val result2 = VectorQuantity(ddDummy1, Unit("m^3")).valueIn("km^3")
        assertBounds(0.000000001, (result2 as AADD))
        val result21 = VectorQuantity(ddDummy1, Unit("m^3")).valueIn("dm^3")
        assertBounds(1000.0, (result21 as AADD))
        val result22 = VectorQuantity(ddDummy1, Unit("m^3")).valueIn("mm^3")
        assertBounds(9.999999999999998E8..1.0E9, (result22 as AADD))
        val result23 = VectorQuantity(ddDummy1, Unit("m^3")).valueIn("cm^3")
        assertBounds(1000000.0, (result23 as AADD))

        val result3 = VectorQuantity(ddDummy1, Unit("m")).valueIn("km")
        assertBounds(0.001, (result3 as AADD))
        val result31 = VectorQuantity(ddDummy1, Unit("m")).valueIn("dm")
        assertBounds(10.0, (result31 as AADD))
        val result32 = VectorQuantity(ddDummy1, Unit("m")).valueIn("mm")
        assertBounds(1000.0, (result32 as AADD))
        val result33 = VectorQuantity(ddDummy1, Unit("m")).valueIn("cm")
        assertBounds(100.0, (result33 as AADD))
    }

    @Test
    fun lengthConvert() {
        val q1 = VectorQuantity(ddDummy1, Unit("inch"))
        val qResult1 = q1.valueIn("cm")
        assertBounds(2.54, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("ft"))
        val qResult2 = q2.valueIn("mm")
        assertBounds(304.8, qResult2.asAadd())

        val q3 = VectorQuantity(ddDummy1, Unit("yd"))
        val qResult3 = q3.valueIn("dm")
        assertBounds(9.144, qResult3.asAadd())

        val q4 = VectorQuantity(ddDummy1, Unit("mi"))
        val qResult4 = q4.valueIn("km")
        assertBounds(1.609344, qResult4.asAadd())

        val q5 = VectorQuantity(ddDummy1, Unit("nmi"))
        val qResult5 = q5.valueIn("km")
        assertBounds(1.852, qResult5.asAadd())
    }

    @Test
    fun lengthConvert2() {
        val q1 = VectorQuantity(ddDummy1, Unit("cm"))
        val qResult1 = q1.valueIn("inch")
        assertBounds(0.39370078740157, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("m"))
        val qResult2 = q2.valueIn("ft")
        assertBounds(3.2808398950131, qResult2.asAadd())

        val q3 = VectorQuantity(ddDummy1, Unit("dm"))
        val qResult3 = q3.valueIn("yd")
        assertBounds(0.10936132983377, qResult3.asAadd())

        val q4 = VectorQuantity(ddDummy1, Unit("km"))
        val qResult4 = q4.valueIn("mi")
        assertBounds(0.62137119223733, qResult4.asAadd())

        val q5 = VectorQuantity(ddDummy1, Unit("km"))
        val qResult5 = q5.valueIn("nmi")
        assertBounds(0.53995680345572, qResult5.asAadd())
    }

    @Test
    fun massConvert1() {
        val q1 = VectorQuantity(ddDummy1, Unit("lb"))
        val qResult1 = q1.valueIn("kg")
        assertBounds(0.45359237, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("oz"))
        val qResult2 = q2.valueIn("g")
        assertBounds(28.349523125, qResult2.asAadd())

        val q3 = VectorQuantity(ddDummy1, Unit("ct"))
        val qResult3 = q3.valueIn("g")
        assertBounds(0.2, qResult3.asAadd())

        val q4 = VectorQuantity(ddDummy1, Unit("gr"))
        val qResult4 = q4.valueIn("g")
        assertBounds(0.06479891, qResult4.asAadd())

        val q5 = VectorQuantity(ddDummy1, Unit("t"))
        val qResult5 = q5.valueIn("g")
        assertBounds(1000000.0, qResult5.asAadd())

        val q6 = VectorQuantity(ddDummy1, Unit("tn"))
        val qResult6 = q6.valueIn("g")
        assertBounds(907184.74, qResult6.asAadd())
    }

    @Test
    fun currency() {
        val q1 = VectorQuantity(ddDummy1, Unit("EUR"))
        assertBounds(1.0, q1)
    }

    @Test
    fun electricalCharge() {
        val q1 = VectorQuantity(ddDummy1, Unit("C"))
        val qResult1 = q1.valueIn("As")
        assertBounds(1.0, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("C"))
        val qResult2 = q2.valueIn("Ah")
        assertBounds(0.000277777777777, qResult2.asAadd())

        val q3 = VectorQuantity(ddDummy1, Unit("As"))
        val qResult3 = q3.valueIn("Ah")
        assertBounds(0.000277777777777, qResult3.asAadd())

        val q4 = VectorQuantity(ddDummy1, Unit("Ah"))
        val qResult4 = q4.valueIn("C")
        assertBounds(3600.0, qResult4.asAadd())

        val q5 = VectorQuantity(ddDummy1, Unit("Ah"))
        val qResult5 = q5.valueIn("As")
        assertBounds(3600.0, qResult5.asAadd())
    }

    @Test
    fun massConvert2() {
        val q1 = VectorQuantity(ddDummy1, Unit("kg"))
        val qResult1 = q1.valueIn("lb")
        assertBounds(2.2046226218488, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("g"))
        val qResult2 = q2.valueIn("oz")
        assertBounds(0.03527396194958, qResult2.asAadd())

        val q3 = VectorQuantity(ddDummy1, Unit("g"))
        val qResult3 = q3.valueIn("ct")
        assertBounds(5.0, qResult3.asAadd())

        val q4 = VectorQuantity(ddDummy1, Unit("g"))
        val qResult4 = q4.valueIn("gr")
        assertBounds(15.432358352941, qResult4.asAadd())

        val q5 = VectorQuantity(ddDummy1, Unit("g"))
        val qResult5 = q5.valueIn("t")
        assertBounds(0.000001, qResult5.asAadd())

        val q6 = VectorQuantity(ddDummy1, Unit("g"))
        val qResult6 = q6.valueIn("tn")
        assertBounds(1.10231131092439E-6, qResult6.asAadd())
    }

    @Test
    fun volume() {
        val q1 = VectorQuantity(ddDummy1, Unit("l"))
        val qResult1 = q1.valueIn("m^3")
        assertBounds(0.001, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("pt"))
        val qResult2 = q2.valueIn("dm^3")
        assertBounds(0.473176473, qResult2.asAadd())

        val q3 = VectorQuantity(ddDummy1, Unit("qt"))
        val qResult3 = q3.valueIn("dm^3")
        assertBounds(0.946352946, qResult3.asAadd())

        val q4 = VectorQuantity(ddDummy1, Unit("gal"))
        val qResult4 = q4.valueIn("dm^3")
        assertBounds(3.785411784, qResult4.asAadd())

        val q5 = VectorQuantity(ddDummy1, Unit("bbl"))
        val qResult5 = q5.valueIn("m^3")
        assertBounds(0.158987294928, qResult5.asAadd())
    }

    @Test
    fun volume2() {
        val q1 = VectorQuantity(ddDummy1, Unit("m^3"))
        val qResult1 = q1.valueIn("l")
        assertBounds(1000.0, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("dm^3"))
        val qResult2 = q2.valueIn("pt")
        assertBounds(2.1133764188652, qResult2.asAadd())

        val q3 = VectorQuantity(ddDummy1, Unit("dm^3"))
        val qResult3 = q3.valueIn("qt")
        assertBounds(1.0566882094326, qResult3.asAadd())

        val q4 = VectorQuantity(ddDummy1, Unit("dm^3"))
        val qResult4 = q4.valueIn("gal")
        assertBounds(0.26417205235815, qResult4.asAadd())

        val q5 = VectorQuantity(ddDummy1, Unit("m^3"))
        val qResult5 = q5.valueIn("bbl")
        assertBounds(6.289810770432, qResult5.asAadd())
    }

    @Test
    fun area() {
        val q1 = VectorQuantity(ddDummy1, Unit("ac"))
        val qResult1 = q1.valueIn("m^2")
        assertBounds(4046.8564224, qResult1.asAadd())

        val q3 = VectorQuantity(ddDummy1, Unit("ha"))
        val qResult3 = q3.valueIn("m^2")
        assertBounds(10000.0, qResult3.asAadd())
    }

    @Test
    fun area2() {
        val q2 = VectorQuantity(ddDummy1, Unit("m^2"))
        val qResult2 = q2.valueIn("ac")
        assertBounds(0.000247105, qResult2.asAadd())

        val q3 = VectorQuantity(ddDummy1, Unit("m^2"))
        val qResult3 = q3.valueIn("ha")
        assertBounds(0.0001, qResult3.asAadd())
    }

    @Test
    fun speed() {
        val q1 = VectorQuantity(ddDummy1, Unit("mph"))
        val qResult1 = q1.valueIn("m/s")
        assertBounds(0.44704, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("kt"))
        val qResult2 = q2.valueIn("m/s")
        assertBounds(0.514444444444444, qResult2.asAadd())
    }

    @Test
    fun speed2() {
        val q1 = VectorQuantity(ddDummy1, Unit("m/s"))
        val qResult1 = q1.valueIn("mph")
        assertBounds(2.236936292054402, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("m/s"))
        val qResult2 = q2.valueIn("kt")
        assertBounds(1.943844492440606, qResult2.asAadd())
    }

    @Test
    fun power() {
        val q1 = VectorQuantity(ddDummy1, Unit("W"))
        val qResult1 = q1.valueIn("HP")
        assertBounds(0.001341022090, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("HP"))
        val qResult2 = q2.valueIn("W")
        assertBounds(745.699871582, qResult2.asAadd())
    }

    @Test
    fun pressure() {
        val q1 = VectorQuantity(ddDummy1, Unit("bar"))
        val qResult1 = q1.valueIn("Pa")
        val qResult3 = q1.valueIn("psi")
        assertBounds(100000.0, qResult1.asAadd())
        assertBounds(14.503773773, qResult3.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("Pa"))
        val qResult2 = q2.valueIn("bar")
        val qResult4 = q2.valueIn("psi")
        assertBounds(0.00001, qResult2.asAadd())
        assertBounds(0.00014503773773, qResult4.asAadd())

        val q3 = VectorQuantity(ddDummy1, Unit("psi"))
        val qResult5 = q3.valueIn("bar")
        val qResult6 = q3.valueIn("Pa")
        assertBounds(0.06894757293, qResult5.asAadd())
        assertBounds(6894.757293, qResult6.asAadd())
    }

    @Test
    fun energy() {
        val q1 = VectorQuantity(ddDummy1, Unit("J"))
        val qResult1 = q1.valueIn("Ws")
        assertBounds(1.0, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("J"))
        val qResult2 = q2.valueIn("Wh")
        assertBounds(0.0002777777, qResult2.asAadd())

        val q3 = VectorQuantity(ddDummy1, Unit("J"))
        val qResult3 = q3.valueIn("cal")
        assertBounds(0.23900573613766726, qResult3.asAadd())
    }

    @Test
    fun energy2() {
        val q1 = VectorQuantity(ddDummy1, Unit("W s"))
        val qResult1 = q1.valueIn("J")
        assertBounds(1.0, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("Wh"))
        val qResult2 = q2.valueIn("J")
        assertBounds(3600.0, qResult2.asAadd())

        val q3 = VectorQuantity(ddDummy1, Unit("cal"))
        val qResult3 = q3.valueIn("J")
        assertBounds(4.184, qResult3.asAadd())
    }

    @Test
    fun rad() {
        val q1 = VectorQuantity(ddDummy1, Unit("Pi"))
        val qResult1 = q1.valueIn("1")
        assertBounds(3.1415926535, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("1"))
        val qResult2 = q2.valueIn("Pi")
        assertBounds(0.3183098862, qResult2.asAadd())
    }

    @Test
    fun deg() {
        val q1 = VectorQuantity(ddDummy1, Unit("°"))
        val qResult1 = q1.valueIn("1")
        assertBounds(2 * kotlin.math.PI / 360, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("1"))
        val qResult2 = q2.valueIn("°")
        assertBounds(360 / (2 * kotlin.math.PI), qResult2.asAadd())
    }

    @Test
    fun radDeg() {
        val q1 = VectorQuantity(ddDummy1, Unit("°"))
        val qResult1 = q1.valueIn("Pi")
        assertBounds(1 / 180.0, qResult1.asAadd())

        val q2 = VectorQuantity(ddDummy1, Unit("Pi"))
        val qResult2 = q2.valueIn("°")
        assertBounds(180.0, qResult2.asAadd())
    }

    @Test
    fun difference() {
        val quant1 = VectorQuantity(ddDummy10, "N")
        val quant2 = VectorQuantity(ddDummy1, "N")
        assert(!quant1.unit.isDifference)
        assert(!quant2.unit.isDifference)
        val quantResult = quant1.minus(quant2)
        assert(quantResult.unit.isDifference)
        assertEquals("Force Difference", quantResult.getDomain())
    }

    @Test
    fun difference1() {
        val quant1 = VectorQuantity(ddDummy10, "°C")
        val quant2 = VectorQuantity(ddDummy1, "°C")
        assert(!quant1.unit.isDifference)
        assert(!quant2.unit.isDifference)
        val quantResult = quant1.minus(quant2)
        assert(quantResult.unit.isDifference)
        assertEquals("ThermodynamicTemperature Difference", quantResult.getDomain())
        assertEquals("K", quantResult.unit.toString())
        assertBounds(9.0, quantResult.value.asAadd())
    }

    @Test
    fun differenceTime() {
        val quant1 = VectorQuantity(ddDummy10, "s")
        val quant2 = VectorQuantity(ddDummy1, "s")
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
        loadKerML("""feature date1: ISQ::TimeValue(* [Year]) = Year("2021"); """)
        solver.propagate()
        assertNoIssues()
        assertBounds(1609459200.0, solver.variable("date1").vectorQuantity.value.asAadd())
        assertNoIssues()
    }

    @Test
    fun year2() = testSession("ISQ") {
        loadKerML("""
            feature year: ISQ::TimeValue(* [Year]) = Year("2022"); 
            feature time: ISQ::DurationValue(* [a]) = 200.0 a; 
            feature yearResult: ISQ::TimeValue(* [Year]) = year + time; 
        """, Runlevel.ALL)

        assertEquals("2022", solver.variable("year").vectorQuantity.toString())
        assertEquals("2222", solver.variable("yearResult").vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun year3() = testSession("ISQ") {
        loadKerML("""
            feature year: ISQ::TimeValue(* [Year]) = Year("2021");
            feature year2: ISQ::TimeValue(* [Year]) = Year("2023");
            feature result: ISQ::DurationValue(* [a]) = year2 - year;
        """, Runlevel.ALL)
solver.propagate()
assertNoIssues()

        assertBounds(2.0, solver.variable("result").vectorQuantity.valuesIn("a")[0].asAadd())
        assertNoIssues()
    }

    @Test
    fun dateTime() = testSession("ISQ") {
        loadKerML("""feature date1: ISQ::TimeValue = DateTime("2021-10-30T13:00:01+02:00");""")
        solver.propagate()
        assertNoIssues()
        assertBounds(1635591601.0, solver.variable("date1").vectorQuantity.value.asAadd())
        assertNoIssues()
    }

    @Test
    fun dateTime2() = testSession("ISQ") {
        loadKerML("""feature date: ISQ::TimeValue(* [DateTime]) = DateTime("2021-10-10T00:00");""")
        solver.propagate()
        assertEquals("2021-10-10T00:00", solver.variable("date").vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun dateTimeDiff1() = testSession("ISQ") {
        loadKerML("""
            feature date1: ISQ::TimeValue = DateTime("2021-10-30T13:00:01+02:00");
            feature date2: ISQ::TimeValue = DateTime("2021-10-30T13:01:01+02:00");
            feature datediff: ISQ::DurationValue(* [s]) = date2-date1;
        """, Runlevel.ALL)
solver.propagate()
assertNoIssues()

        assertBounds(1635591601.0, solver.variable("date1").vectorQuantity.value.asAadd())
        assertBounds(1635591661.0, solver.variable("date2").vectorQuantity.value.asAadd())
        assertBounds(60.0, solver.variable("datediff").vectorQuantity.value.asAadd())
        assertNoIssues()
    }

    @Test
    fun dateTimeDiff2() = testSession("ISQ") {
        loadKerML("""
            feature date1: ISQ::TimeValue = DateTime("2021-10-10T03:00:00+02:00");
            feature date2: ISQ::TimeValue = DateTime("2021-10-11T03:00:00+02:00");
            feature datediff: ISQ::DurationValue(* [h]) = date2-date1;""")

        solver.propagate()
        assertNoIssues()
        assertBounds(1633827600.0, solver.variable("date1").vectorQuantity.value.asAadd())
        assertBounds(1633914000.0, solver.variable("date2").vectorQuantity.value.asAadd())
        assertEquals("24 h", solver.variable("datediff").vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun dateTimeSum() = testSession("ISQ") {
        loadKerML("""
            feature date: ISQ::TimeValue = DateTime("2021-10-10T03:00:00");
            feature time: ISQ::DurationValue(* [a]) = 1.0 a;
            feature dateResult: ISQ::TimeValue(* [DateTime]) = date + time;"""
        )
        solver.propagate()
        assertEquals("2022-10-10T03:00", solver.variable("dateResult").vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun date() = testSession("ISQ") {
        loadKerML("""
            feature date: ISQ::TimeValue(* [Date]) = Date("2022-10-10");
            feature time: ISQ::DurationValue(* [d]) = 0.5 d;
            feature dateResult: ISQ::TimeValue(* [Date]) = date + time;
        """, Runlevel.ALL)
        assertEquals("2022-10-10", solver.variable("date").vectorQuantity.toString())
        assertEquals("2022-10-11", solver.variable("dateResult").vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun month() = testSession("ISQ") {
        loadKerML("""
            feature month: ISQ::TimeValue(* [Month]) = Month("2022-10");
            feature time: ISQ::DurationValue(* [d]) = 20.0 d;
            feature monthResult: ISQ::TimeValue(* [Month]) = month + time;
        """, Runlevel.ALL)
        assertEquals("2022-10", solver.variable("month").vectorQuantity.toString())
        assertEquals("2022-11", solver.variable("monthResult").vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun month2() = testSession("ISQ") {
        loadKerML("""
            feature month: ISQ::TimeValue(* [Month]) = Month("2022-10");
            feature time: ISQ::DurationValue(* [a]) = 30.0 a;
            feature monthResult: ISQ::TimeValue(* [Month]) = month + time;"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals("2022-10", solver.variable("month").vectorQuantity.toString())
        assertEquals("2052-10", solver.variable("monthResult").vectorQuantity.toString())
    }

    @Test
    fun month3() = testSession("ISQ") {
        loadKerML("""
            feature month1: ISQ::TimeValue(* [Month]) = Month("2021-10");
            feature month2: ISQ::TimeValue(* [Month]) = Month("2023-10");
            feature time: ISQ::DurationValue(* [a]) = month2 - month1;
        """, Runlevel.ALL)
        assertEquals("2021-10", solver.variable("month1").vectorQuantity.toString())
        assertEquals("2023-10", solver.variable("month2").vectorQuantity.toString())
        assertEquals("2 a", solver.variable("time").vectorQuantity.toString())
        assertNoIssues()
    }

    @Test
    fun missingUnits() = testSession("ISQ") {
        loadKerML(input = """
                feature test1: ISQ::ThermodynamicTemperatureValue {:>> range = * [°C];}
                feature test2: ISQ::ThermodynamicTemperatureValue {:>> range = * [°F];}
                feature test3: ISQ::MassValue ;
                feature percentage: Quantities::ScalarQuantityValue( * [%]).
            """, Runlevel.ALL)
        assertNoIssues()
    }

    @Test
    fun transformIntervalTemperature() {
        val u = Unit("°C")
        var ddRange: AADD? = null
        DDBuilder {
            ddRange = real(0.0 .. 10.0)
        }
        val quantity = VectorQuantity(ddRange!!, u)
        assertBounds(273.15 .. 283.15, quantity, unit = "K", )
    }

    @Test
    fun usdGbpCurrencies() {
        val q1 = VectorQuantity(ddDummy1, Unit("USD"))
        assertBounds(1.0, q1)
        val q2 = VectorQuantity(ddDummy1, Unit("GBP"))
        assertBounds(1.0, q2)
    }

    @Test
    fun volumeUnitsImperial() {
        // 1 pt (US liquid pint) = 473.176473 ml
        val q1 = VectorQuantity(ddDummy1, "pt")
        assertBounds(0.000473176, q1.valueIn("m^3").asAadd())

        // 1 qt (US liquid quart) = 946.352946 ml
        val q2 = VectorQuantity(ddDummy1, "qt")
        assertBounds(0.000946352, q2.valueIn("m^3").asAadd())

        // 1 gal (US liquid gallon) = 3.785411784 l
        val q3 = VectorQuantity(ddDummy1, "gal")
        assertBounds(0.003785411784, q3.valueIn("m^3").asAadd())

        // 1 bbl (US oil barrel) = 158.987294928 l
        val q4 = VectorQuantity(ddDummy1, "bbl")
        assertBounds(0.158987294928, q4.valueIn("m^3").asAadd())
    }

    @Test
    fun specialDerivedUnits() {
        // steradian (sr) = m^2 / m^2 = 1
        assertEquals("1", VectorQuantity(ddDummy1, "sr").unit.toString())

        // Siemens (S) = A / V = A^2 s^3 / kg m^2
        assertEquals("A^2 s^3 / kg m^2", VectorQuantity(ddDummy1, "S").unit.toString())

        // Weber (Wb) = V s = kg m^2 / A s^2
        assertEquals("kg m^2 / A s^2", VectorQuantity(ddDummy1, "Wb").unit.toString())

        // Tesla (T) = Wb / m^2 = kg / A s^2
        assertEquals("kg / A s^2", VectorQuantity(ddDummy1, "T").unit.toString())

        // Lux (lx) = lm / m^2 = cd / m^2
        assertEquals("cd / m^2", VectorQuantity(ddDummy1, "lx").unit.toString())

        // Becquerel (Bq) = 1 / s
        assertEquals("1 / s", VectorQuantity(ddDummy1, "Bq").unit.toString())

        // Gray (Gy) = J / kg = m^2 / s^2
        assertEquals("m^2 / s^2", VectorQuantity(ddDummy1, "Gy").unit.toString())

        // Stokes (St) kinematic viscosity = 10^-4 m^2 / s
        val qSt = VectorQuantity(ddDummy1, "St")
        assertEquals("m^2 / s", qSt.unit.toString())
        assertBounds(0.0001, qSt.valueIn("m^2/s").asAadd())

        // Stilb (sb) luminance = cd / cm^2 = 10^4 cd / m^2
        val qSb = VectorQuantity(ddDummy1, "sb")
        assertEquals("cd / m^2", qSb.unit.toString())
        assertBounds(10000.0, qSb.valueIn("cd/m^2").asAadd())
    }

    @Test
    fun massFlowConversion() {
        val q1 = VectorQuantity(ddDummy1, "kg/s")
        assertBounds(1.0, q1)

        val q2 = VectorQuantity(ddDummy1, "g/s")
        assertBounds(0.001, q2.valueIn("kg/s").asAadd())
    }

    @Test
    fun metricHorsepower() {
        // 1 PS = 75 kgf m/s = 735.49875 W
        assertBounds(735.49875, VectorQuantity(ddDummy1, Unit("PS")).valueIn("W").asAadd())
        assertBounds(0.98632007061953, VectorQuantity(ddDummy1, Unit("PS")).valueIn("HP").asAadd())
    }
}
