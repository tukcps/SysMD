package quantitytests

import com.github.tukcps.sysmd.quantities.*
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.IDD
import kotlin.test.*

class QuantityParsingTests {
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
    private lateinit var iddEmtpy: IDD

    @BeforeTest
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
            iddEmtpy = EmptyIntegerRange
        }
    }

    @Test
    fun parseAllSiUnits() {
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
    fun parseSomeDerivedUnits() {
        val u = Unit("Hz N Sv")
        assertEquals("hertz", u.unitSet.elementAt(0).name)
        assertEquals("newton", u.unitSet.elementAt(1).name)
        assertEquals("sievert", u.unitSet.elementAt(2).name)
    }

    @Test
    fun parseTemperatures() {
        val u = Unit("°F")
        val q = Quantity(ddDummy1, u)
        assertEquals("ThermodynamicTemperature", q.unit.getUnitDomain(1.0))

        val u2 = Unit("°C")
        val q2 = Quantity(ddDummy1, u2)
        assertEquals("ThermodynamicTemperature", q2.unit.getUnitDomain(1.0))

        val u3 = Unit("K")
        val q3 = Quantity(ddDummy1, u3)
        assertEquals("ThermodynamicTemperature", q3.unit.getUnitDomain(1.0))
    }

    @Test
    fun exponents() {
        val u = Unit("m^2 A / s^3 N^4")
        assertEquals(2, u.unitSet.elementAt(0).exponent)
        assertEquals(1, u.unitSet.elementAt(1).exponent)
        assertEquals(-3, u.unitSet.elementAt(2).exponent)
        assertEquals(-4, u.unitSet.elementAt(3).exponent)
    }

    @Test
    fun prefixes() {
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

    @Test
    fun toSiCheckUnits() {
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

    @Test
    fun toSiCheckValues() {
        val u = Unit("l")
        val quantity5 = Quantity(ddDummy1, u)
        assertEquals("m^3", quantity5.unit.toString())
        assertEquals(0.001, (quantity5.value as AADD).getRange().min, 0.0000001)
        assert(quantity5.getRange().contains(0.001))
    }

    @Test
    fun reduceRedundantUnits() {
        val u = Unit("m m s m")
        u.reduceRedundantUnits()
        assertEquals("m^3 s", u.toString())

        val u1 = Unit("g m s m s / g A mol mol")
        u1.reduceRedundantUnits()
        assertEquals("m^2 s^2 / A mol^2", u1.toString())
    }

    @Test
    fun reduceRedundantUnits2() {
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
    fun quantityMakeCanonical() {
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

    @Test
    fun emptyIdd() {
        val quantity2 = Quantity(iddEmtpy)
        assertEquals("∅", quantity2.toString())
    }

    @Test
    fun bytes() {
        val quant1 = Quantity(ddDummy100, "B")
        assertEquals(800.0, quant1.getMinAsDouble(), 0.0000001)
        assertEquals("bit", quant1.unit.toString())
        assertEquals("StorageCapacity", quant1.getDomain())

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
    fun bytes2() {
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
    fun decibel1() {
        val quant1 = Quantity(ddDummy10, "dB")
        assertEquals(10.0, quant1.getMinAsDouble(), 0.0000001)
        assertEquals("1", quant1.unit.toString())
        assertEquals("DimensionOne", quant1.getDomain())

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
    fun decibel2() {
        val quant1 = Quantity(ddDummy10, "1")
        assertEquals(10.0, quant1.valueIn("dB").asAadd().getRange().min, 0.0000001)

        val quant2 = Quantity(ddDummy100, "1")
        assertEquals(20.0, quant2.valueIn("dB").asAadd().getRange().min, 0.0000001)

        val quant3 = Quantity(ddDummy1000, "1")
        assertEquals(30.0, quant3.valueIn("dB").asAadd().getRange().min, 0.0000001)
    }

    @Test
    fun addUnit1() {
        addUnit("testunit", "tu", "Nm", "testdomain", 5.0, false)
        val unit = Unit("tu")
        assertEquals("testunit", unit.unitSet.elementAt(0).name)
        assertEquals("tu", unit.unitSet.elementAt(0).symbol)
        assertEquals("testdomain", unit.unitSet.elementAt(0).domain)
        assertEquals(5.0, unit.unitSet.elementAt(0).convFac)
        assertFalse(unit.unitSet.elementAt(0).isLogarithmic)
        val baseUnitSet = mutableSetOf(Length.Meter.copy(2), Mass.Kilogram.copy(1), Duration.Second.copy(-2))
        assertEquals(baseUnitSet, unit.unitSet.elementAt(0).getBaseUnits())
    }

    @Test
    fun addUnit2() {
        addUnit("testunit", "tu", "Nm", "testdomain", 5.0, false)
        val quantity = Quantity(ddDummy1, "tu")
        assertEquals(5.0, quantity.valueIn("Nm").asAadd().getRange().min, 0.00001)
    }
}
