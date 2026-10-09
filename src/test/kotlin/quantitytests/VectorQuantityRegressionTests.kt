package quantitytests

import util.assertBounds
import com.github.tukcps.sysmd.quantities.AdditionError
import com.github.tukcps.sysmd.quantities.DDError
import com.github.tukcps.sysmd.quantities.TransformationError
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.ite
import com.github.tukcps.sysmd.quantities.max
import com.github.tukcps.sysmd.quantities.min
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.dd.DD
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class VectorQuantityRegressionTests {
    @Test
    fun dotProductMultipliesUnits() {
        DDBuilder {
            val force = VectorQuantity(listOf(real(2.0), real(3.0)), "N")
            val distance = VectorQuantity(listOf(real(4.0), real(5.0)), "m")
            val energy = force dot distance
            assertEquals(Unit("J"), energy.unit)
            assertBounds(23.0, energy)
        }
    }

    @Test
    fun canonicalFactoryCopiesInputList() {
        DDBuilder {
            val source = mutableListOf<DD<*>>(real(2.0), real(3.0))
            val quantity = VectorQuantity.fromCanonical(source, Unit("m"))
            source.clear()
            assertEquals(2, quantity.values.size)
            assertBounds(2.0, VectorQuantity(quantity.values[0]))
        }
    }

    @Test
    fun canonicalFactoryRejectsMixedTypes() {
        DDBuilder {
            assertFailsWith<DDError> { VectorQuantity.fromCanonical(emptyList()) }
            assertFailsWith<DDError> {
                VectorQuantity.fromCanonical(listOf(integer(2), real(3.0)))
            }
            assertFailsWith<DDError> {
                VectorQuantity.fromCanonical(listOf(boolean(true), integer(3)))
            }
        }
    }

    @Test
    fun equalQuantitiesIgnoreDisplayMetadataInHashing() {
        DDBuilder {
            val meters = VectorQuantity(real(1.0), "m")
            val kilometers = meters.copy(unitSpec = "km", userWantedUnitSpec = true)
            assertEquals(meters, kilometers)
            assertEquals(meters.hashCode(), kilometers.hashCode())
            assertTrue(hashSetOf(meters).contains(kilometers))
        }
    }

    @Test
    fun equalityDoesNotUseApproximateValues() {
        DDBuilder {
            val first = VectorQuantity(real(1.0))
            val second = VectorQuantity(real(1.00000075))
            val third = VectorQuantity(real(1.0000015))
            assertFalse(first == second)
            assertFalse(second == third)
            assertFalse(first == third)
            assertTrue(first.isApproximatelyEqualTo(second))
            assertTrue(second.isApproximatelyEqualTo(third))
            assertFalse(first.isApproximatelyEqualTo(third))
        }
    }

    @Test
    fun exactRangeEqualityWorksAcrossBuildersAndValueTypes() {
        val leftBuilder = DDBuilder()
        val rightBuilder = DDBuilder()
        val pairs = listOf(
            VectorQuantity(leftBuilder.real(1.0..2.0), "m") to VectorQuantity(rightBuilder.real(1.0..2.0), "m"),
            VectorQuantity(leftBuilder.integer(1L..2L)) to VectorQuantity(rightBuilder.integer(1L..2L)),
            VectorQuantity(leftBuilder.boolean(true)) to VectorQuantity(rightBuilder.boolean(true)),
            VectorQuantity(leftBuilder.string("same")) to VectorQuantity(rightBuilder.string("same"))
        )
        for ((left, right) in pairs) {
            assertEquals(left, right)
            assertEquals(left.hashCode(), right.hashCode())
            assertEquals("found", hashMapOf(left to "found")[right])
        }
        assertFalse(VectorQuantity(leftBuilder.real(1.0)) == VectorQuantity(leftBuilder.integer(1)))
        assertFalse(VectorQuantity(leftBuilder.real(1.0), "m") == VectorQuantity(leftBuilder.real(1.0), "s"))
    }

    @Test
    fun dotProductPreservesUnknownAndDimensionlessUnits() {
        DDBuilder {
            val unknown = VectorQuantity(real(2.0), "?")
            val meters = VectorQuantity(real(3.0), "m")
            assertEquals(Unit("?"), (unknown dot meters).unit)
            assertEquals(Unit("m"), (VectorQuantity(real(2.0)) dot meters).unit)
            val integerResult = VectorQuantity(listOf(integer(2), integer(3))) dot
                VectorQuantity(listOf(integer(4), integer(5)))
            assertEquals(VectorQuantity(integer(23)), integerResult)
        }
    }


    @Test
    fun temperatureOffsetOnlyAppliesToAbsoluteTemperatures() {
        DDBuilder {
            // absolute temperatures have an offset
            assertBounds(293.15, VectorQuantity(real(20.0), "°C"))
            assertBounds(373.15, VectorQuantity(real(212.0), "°F"))
            // compound units and powers are only scaled
            assertBounds(1.0, VectorQuantity(real(1.0), "J/°C"))
            assertBounds(2.0, VectorQuantity(real(2.0), "°C/s"))
            assertBounds(1.8, VectorQuantity(real(1.0), "J/°F"))
            assertBounds(1.0, VectorQuantity(real(1.0), "J/K").valueIn("J/°C").asAadd())
            assertBounds(5.0, VectorQuantity(real(9.0), "J/K").valueIn("J/°F").asAadd())
            assertBounds(81.0, VectorQuantity(real(25.0), "K^2").valueIn("°F^2").asAadd())
        }
    }

    @Test
    fun temperatureDifferenceIsConvertedWithoutOffset() {
        DDBuilder {
            val t20 = VectorQuantity(real(20.0), "°C")
            val t10 = VectorQuantity(real(10.0), "°C")
            val difference = t20 - t10
            assertTrue(difference.unit.isDifference)
            assertBounds(10.0, difference.valueIn("°C").asAadd())
            assertBounds(10.0, difference.valueIn("K").asAadd())
            assertBounds(18.0, difference.valueIn("°F").asAadd())
            assertEquals("10 °C", difference.copy(userWantedUnitSpec = true).toString())

            // absolute - difference and difference + absolute are absolute temperatures again
            val cooled = t20 - VectorQuantity(real(5.0), "K")
            assertFalse(cooled.unit.isDifference)
            assertBounds(15.0, cooled.valueIn("°C").asAadd())
            val heated = difference + VectorQuantity(real(5.0), "°C")
            assertFalse(heated.unit.isDifference)
            assertBounds(15.0, heated.valueIn("°C").asAadd())

            // a difference that is given in °F is scaled by 5/9 only
            val fahrenheitDifference = VectorQuantity(real(18.0), Unit("°F").copy(isDifference = true))
            assertBounds(10.0, fahrenheitDifference)
        }
    }

    @Test
    fun plusChecksUnitsBeforeSkippingZero() {
        DDBuilder {
            val meters = VectorQuantity(real(5.0), "m")
            assertFailsWith<AdditionError> { VectorQuantity(real(0.0), "kg") + meters }
            assertFailsWith<AdditionError> { meters + VectorQuantity(real(0.0), "kg") }
            // the plain number 0 and a 0 of the same dimension are still neutral, also for vectors
            assertEquals(meters, VectorQuantity(real(0.0)) + meters)
            assertEquals(meters, meters + VectorQuantity(real(0.0), "km"))
            val vector = VectorQuantity(listOf(real(1.0), real(2.0)), "m")
            assertEquals(vector, VectorQuantity(real(0.0), "m") + vector)
        }
    }

    @Test
    fun plusDoesNotSwallowTinyValues() {
        DDBuilder {
            val femtoFarad = VectorQuantity(real(1.0), "fF")
            val sum = femtoFarad + femtoFarad
            assertBounds(2.0, sum.valueIn("fF").asAadd())
            assertBounds(2.0e-15, (VectorQuantity(real(1.0e-15)) + VectorQuantity(real(1.0e-15))))
        }
    }

    @Test
    fun comparisonsRejectIncompatibleUnits() {
        DDBuilder {
            val meters = VectorQuantity(real(5.0), "m")
            val seconds = VectorQuantity(real(1.0), "s")
            assertFailsWith<TransformationError> { meters gt seconds }
            assertFailsWith<TransformationError> { meters lt seconds }
            assertFailsWith<TransformationError> { meters ge seconds }
            assertFailsWith<TransformationError> { meters le seconds }
            assertFailsWith<TransformationError> { meters eq seconds }
            assertFailsWith<TransformationError> { meters neq seconds }
            assertFailsWith<TransformationError> { max(meters, seconds) }
            assertFailsWith<TransformationError> { min(meters, seconds) }
            assertFailsWith<TransformationError> { boolean(true).ite(meters, seconds) }

            // same dimension, the plain number 0 and unknown units are still comparable
            val kilometers = VectorQuantity(real(1.0), "km")
            assertEquals(VectorQuantity(boolean(true)), kilometers gt meters)
            assertEquals(VectorQuantity(boolean(true)), meters gt VectorQuantity(real(0.0)))
            assertEquals(VectorQuantity(boolean(true)), meters gt VectorQuantity(real(1.0), "?"))
            assertBounds(1000.0, max(meters, kilometers))
            assertBounds(5.0, min(meters, kilometers))
            assertEquals(meters, boolean(true).ite(meters, kilometers))
        }
    }

    @Test
    fun absKeepsSmallPositiveLowerBound() {
        DDBuilder {
            val length = VectorQuantity(listOf(real(100.0), real(0.0)), "μm").abs()
            assertBounds(1.0e-4..1.0e-4, length)

            val distance = VectorQuantity(listOf(real(100.0), real(0.0)), "nm")
                .cityBlockDistance(VectorQuantity(listOf(real(0.0), real(0.0)), "nm"))
            assertBounds(1.0e-7..1.0e-7, distance)

            // ranges that contain 0 still start at 0
            assertBounds(0.0..2.0, VectorQuantity(listOf(real(-1.0..2.0), real(0.0)), "m").abs())
        }
    }

    @Test
    fun valuesInRejectsDecibelForQuantitiesWithDimension() {
        DDBuilder {
            assertFailsWith<TransformationError> { VectorQuantity(real(5.0), "m").valueIn("dB") }
            assertBounds(20.0, VectorQuantity(real(100.0)).valueIn("dB").asAadd())
            assertBounds(20.0, VectorQuantity(real(20.0), "dB").valueIn("dB").asAadd())
        }
    }

    @Test
    fun transcendentalFunctionsReturnPlainNumbers() {
        DDBuilder {
            val half = VectorQuantity(real(50.0), "%").copy(userWantedUnitSpec = true)
            assertEquals("50 %", half.toString())
            val results = listOf(
                half.sin() to kotlin.math.sin(0.5), half.cos() to kotlin.math.cos(0.5), half.tan() to kotlin.math.tan(0.5),
                half.arcsin() to kotlin.math.asin(0.5), half.arccos() to kotlin.math.acos(0.5), half.arctan() to kotlin.math.atan(0.5),
                half.ln() to kotlin.math.ln(0.5), half.log2() to kotlin.math.log2(0.5), half.log(real(10.0)) to kotlin.math.log10(0.5),
                half.log(VectorQuantity(real(10.0))) to kotlin.math.log10(0.5),
                half.exp() to kotlin.math.exp(0.5), half.pow2() to kotlin.math.sqrt(2.0), half.pow(real(2.0)) to 0.25
            )
            for ((result, expected) in results) {
                assertEquals(VectorQuantity.NO_UNIT, result.unit)
                assertEquals("", result.unitSpec)
                assertFalse(result.userWantedUnitSpec)
                assertBounds(expected, result)
            }
            assertEquals("0.47943", half.sin().toString())
        }
    }

    @Test
    fun vectorDividedByVectorIsTheEnclosingScalar() {
        DDBuilder {
            val quotient = VectorQuantity(listOf(real(2.0), real(6.0)), "m") / VectorQuantity(listOf(real(1.0), real(2.0)), "s")
            assertTrue(quotient.isScalar)
            assertEquals(Unit("m/s"), quotient.unit)
            assertBounds(2.0..3.0, quotient)
        }
    }

    @Test
    fun massIsPrefixedOnGramNotOnKilogram() {
        DDBuilder {
            assertEquals("5 kg", VectorQuantity(real(5.0), "kg").toString())
            assertEquals("5 Mg", VectorQuantity(real(5000.0), "kg").toString())
            assertEquals("2 g", VectorQuantity(real(0.002), "kg").toString())
            assertEquals("2 mg", VectorQuantity(real(2.0e-6), "kg").toString())
            assertEquals("5 Mg", VectorQuantity(real(5.0), "t").toString())
        }
    }

    @Test
    fun timestampAndDurationAreToldApartByUnit() {
        DDBuilder {
            val year2001 = VectorQuantity(real(1.0e9), "DateTime")
            val year2023 = VectorQuantity(real(1.7e9), "DateTime")
            assertEquals("Timestamp", year2001.getDomain())
            assertEquals("Timestamp", year2023.getDomain())
            assertEquals("Duration", VectorQuantity(real(1.7e9), "s").getDomain())
            assertEquals("Duration", VectorQuantity(real(60.0), "a").getDomain())
            assertEquals("Duration", (year2023 - year2001).getDomain())
            assertEquals("Timestamp", (year2001 + VectorQuantity(real(60.0), "s")).getDomain())
        }
    }

    @Test
    fun ceilAndFloorRoundInTheDisplayedUnit() {
        DDBuilder {
            for (unit in listOf("cm", "mm", "km", "inch", "°C", "°F")) {
                val exact = VectorQuantity(real(150.0), unit)
                assertBounds(150.0, exact.ceil().valueIn(unit).asAadd(), msg = "ceil(150 $unit)")
                assertBounds(150.0, VectorQuantity(exact.ceil().valueIn(unit)))
                assertBounds(150.0, exact.floor().valueIn(unit).asAadd(), msg = "floor(150 $unit)")
                assertBounds(150.0, VectorQuantity(exact.floor().valueIn(unit)))

                val between = VectorQuantity(real(150.5), unit)
                assertBounds(151.0, between.ceil().valueIn(unit).asAadd(), msg = "ceil(150.5 $unit)")
                assertBounds(150.0, between.floor().valueIn(unit).asAadd(), msg = "floor(150.5 $unit)")
                assertEquals(unit, between.ceil().unitSpec)
            }
            assertBounds(294.15, VectorQuantity(real(20.5), "°C").ceil())
            assertEquals("151 cm", VectorQuantity(real(150.5), "cm").copy(userWantedUnitSpec = true).ceil().toString())

            // intervals are rounded outwards in the displayed unit
            val range = VectorQuantity(real(1.2..2.7), "km")
            assertBounds(2000.0..3000.0, range.ceil())
            assertBounds(1000.0..2000.0, range.floor())

            // a temperature difference is rounded without offset
            val difference = VectorQuantity(real(20.5), "°C") - VectorQuantity(real(10.0), "°C")
            assertBounds(11.0, difference.ceil())

            // without a unit spec the SI value is rounded
            val product = VectorQuantity(real(150.0), "cm") * VectorQuantity(real(1.0))
            assertEquals("", product.unitSpec)
            assertBounds(2.0, product.ceil())
            assertBounds(2.0, VectorQuantity(real(1.5)).ceil())
        }
    }

    @Test
    fun valuesInOneIsTheDimensionlessUnit() {
        DDBuilder {
            val kilometers = VectorQuantity(real(5.0), "km")
            assertFailsWith<TransformationError> { kilometers.valuesIn("1") }
            assertFailsWith<TransformationError> { kilometers.valueIn("1") }
            assertFailsWith<TransformationError> { kilometers.valuesIn("") }
            assertFailsWith<TransformationError> { kilometers.valuesIn("rad") }
            assertFailsWith<TransformationError> { kilometers.valuesIn("%") }

            assertBounds(0.5, VectorQuantity(real(50.0), "%").valueIn("1").asAadd())
            assertBounds(kotlin.math.PI, VectorQuantity(real(180.0), "°").valueIn("1").asAadd())
            assertBounds(2.0, VectorQuantity(real(2.0), "rad").valueIn("1").asAadd())
            assertBounds(10.0, VectorQuantity(real(10.0), "dB").valueIn("1").asAadd())
            assertBounds(3.0, VectorQuantity(real(3.0)).valueIn("1").asAadd())
            assertBounds(50.0, VectorQuantity(real(0.5)).valueIn("%").asAadd())
            assertBounds(3.0, VectorQuantity(real(3.0), "?").valueIn("1").asAadd())

            // the SI values are available for every quantity
            assertBounds(5000.0, VectorQuantity(kilometers.valuesInSI()[0]))
            assertBounds(5.0, VectorQuantity(kilometers.valuesInUnitSpec()[0]))
            assertBounds(5000.0, VectorQuantity((kilometers * VectorQuantity(real(1.0))).valuesInUnitSpec()[0]))
        }
    }
}
