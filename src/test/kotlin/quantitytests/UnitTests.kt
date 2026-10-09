package quantitytests

import util.assertBounds
import com.github.tukcps.sysmd.quantities.AdditionError
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.UnknownUnitError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.DDBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UnitTests {
    @Test
    fun equivalentUnitsWorkAsHashKeys() {
        for ((left, right) in listOf("N" to "kg m / s^2", "Hz" to "1 / s")) {
            val first = Unit(left)
            val second = Unit(right)
            assertEquals(first, second)
            assertEquals(first.hashCode(), second.hashCode())
            assertTrue(hashSetOf(first).contains(second))
            assertEquals("found", hashMapOf(first to "found")[second])
        }
    }

    @Test
    fun prefixesAndScaleMakeUnitsDifferent() {
        for ((left, right) in listOf("m" to "km", "N" to "kN", "%" to "1", "mm" to "m")) {
            val first = Unit(left)
            val second = Unit(right)
            assertNotEquals(first, second)
            assertTrue(first.hasSameDimension(second))
            assertTrue(first.isCompatibleWith(second))
        }
        assertFalse(Unit("m").hasSameDimension(Unit("s")))
    }

    @Test
    fun unknownIsOnlyEqualToUnknown() {
        val unknown = Unit("?")
        assertEquals(unknown, Unit("?").toSI())
        for (known in listOf(Unit("m"), Unit("s"), Unit("1"))) {
            assertFalse(known == unknown)
            assertFalse(unknown == known)
            assertTrue(known.isCompatibleWith(unknown))
            assertTrue(unknown.isCompatibleWith(known))
        }
        assertFalse(Unit("m").isCompatibleWith(Unit("s")))
        assertTrue(Unit("m").isCompatibleWith(Unit("km")))
        assertEquals(3, hashSetOf(unknown, Unit("m"), Unit("km")).size)
    }

    @Test
    fun dimensionalEqualityIsTransitive() {
        val units = listOf(Unit("N"), Unit("kg m / s^2"), Unit("kg m s^-2"))
        assertEquals(units[0], units[1])
        assertEquals(units[1], units[2])
        assertEquals(units[0], units[2])
        assertNotEquals(Unit("N"), Unit("kN"))
    }

    @Test
    fun rejectMalformedExponents() {
        for (input in listOf("m^2^3", "m^2^", "m^^2", "m^", "m^abc", "m / s^2^3")) {
            assertFailsWith<UnknownUnitError>(input) { Unit(input) }
        }
        assertEquals("m^2 / s^3", Unit("m^2 / s^3").toString())
    }

    @Test
    fun unknownRemainsCompatibleInArithmetic() {
        DDBuilder {
            val unknown = VectorQuantity(real(2.0), "?")
            val meters = VectorQuantity(real(3.0), "m")
            for (sum in listOf(unknown + meters, meters + unknown)) {
                assertEquals(Unit("m"), sum.unit)
                assertBounds(5.0, sum)
            }
            assertEquals(Unit("m"), (unknown - meters).unit)
            assertEquals(Unit("m"), (meters - unknown).unit)
            assertFailsWith<AdditionError> { meters + VectorQuantity(real(4.0), "s") }
        }
    }

    @Test
    fun negativeExponentsAreParsed() {
        assertEquals(Unit("1/m^2"), Unit("m^-2"))
        assertEquals("1 / m^2", Unit("m^-2").toString())
        assertEquals(Unit("m/s^2"), Unit("m s^-2"))
        assertEquals(Unit("m^2"), Unit("1/m^-2"))
        for (input in listOf("m^-", "m^--2", "m-2", "-m")) {
            assertFailsWith<UnknownUnitError>(input) { Unit(input) }
        }
    }

    @Test
    fun microSignIsAcceptedAsPrefix() {
        // "\u00B5" is the micro sign of many keyboards, "\u03BC" the greek letter mu
        assertEquals(Unit("\u03BCm").toString(), Unit("\u00B5m").toString())
        DDBuilder {
            assertBounds(1.0e-6, VectorQuantity(real(1.0), "\u00B5m"))
            assertBounds(1.0e-6, VectorQuantity(real(1.0), "\u03BCm"))
        }
    }

    @Test
    fun powResetsDifferenceForEveryExponent() {
        val difference = Unit("m").copy(isDifference = true, unitDomain = "Length")
        for (exponent in listOf(1, 2, 3)) {
            val result = difference.pow(exponent)
            assertFalse(result.isDifference)
            assertEquals("", result.unitDomain)
        }
        assertEquals(Unit("m"), difference.pow(1))
        assertEquals(Unit("m^2"), difference.pow(2))
    }
}
