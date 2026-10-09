package com.github.tukcps.sysmd.quantities

import com.github.tukcps.sysmd.roundOutwards
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.util.Tolerance
import io.github.tukcps.aadd.util.isNotEmpty
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.bounds.DoubleBound
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.bounds.LongBound
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import kotlin.math.*

/**
 * Class with functionality to create nice representations of values,
 * supporting engineering notation and interval representations.
 * Precision is defined in significant figures.
 */
class Representer(
    private val precision: Int = 5,
    val infinityString: String = "*",
    val negativeinfinityString: String = infinityString,
    val illegalValue: String = "∅"
) {
    val negativeInfinityString: String get() = negativeinfinityString

    private val tolerance: Tolerance = Tolerance(relative = 10.0.pow(-precision))
    private fun nearZero(x: Double): Boolean = abs(x) < 10.0.pow(-precision)

    /**
     * Formats a DoubleBound to a string.
     */
    fun represent(bound: DoubleBound): String = when (bound) {
        is DoubleBound.Finite -> formatNumber(bound.value)
        // FIXME: When adopting -*..*, default negativeinfinityString should be "-$infinityString" (e.g. "-*")
        DoubleBound.NegativeInfinity -> negativeinfinityString
        DoubleBound.PositiveInfinity -> infinityString
    }

    /**
     * Formats an AADD value (scalar or interval) to a string.
     * @param value AADD representation of a value
     */
    fun represent(value: AADD): String {
        val r = value.getRange()
        if (r.isEmpty()) return illegalValue
        val minBound = r.min
        val maxBound = r.max
        if (minBound == maxBound) {
            return represent(minBound)
        }
        // Both bounds infinite and not equal (i.e. -Inf .. +Inf)
        // FIXME: When adopting -*..*, this should return "$negativeinfinityString..$infinityString" (i.e. "-*..*")
        if (minBound !is DoubleBound.Finite && maxBound !is DoubleBound.Finite) {
            return "$negativeinfinityString..$infinityString"
        }
        val min = minBound.toDouble()
        val max = maxBound.toDouble()
        // Point value / scalar check within tolerance or both near zero
        if (r.isScalar(tolerance) || (nearZero(min) && nearZero(max))) {
            return represent(minBound)
        }
        // Symmetric relative zero check:
        // If lower bound is negligible compared to upper bound, snap lower bound to 0
        // If upper bound is negligible compared to lower bound, snap upper bound to 0
        if (minBound is DoubleBound.Finite && maxBound is DoubleBound.Finite) {
            val ratio = 10.0.pow(precision + 1)
            if (min != 0.0 && abs(max / min) > ratio) {
                return "0..${represent(maxBound)}"
            }
            if (max != 0.0 && abs(min / max) > ratio) {
                return "${represent(minBound)}..0"
            }
        }

        return "${represent(minBound)}..${represent(maxBound)}"
    }

    private fun VectorQuantity.optimalUnit() : Pair<List<AADD>, String> = when {
        values.all { it.isInfeasible() } -> values to unit.toString()
        // 1. If the user explicitly specified this unit, format strictly in that unit without adding prefixes/derived units
        unitSpec.isNotEmpty() && userWantedUnitSpec -> this.valuesIn(unitSpec) to unitSpec
        unit.toString() == "?" -> values to unit.toString()
        // 2. If the unit was inherited from a type (userWantedUnitSpec == false) or unspecified,
        // discover derived units and optimal SI prefixes (e.g. 1 GC instead of 1e9 A s)
        unit.calculatedUnitSymbol.isNotEmpty()  -> {
            val unitSymbol = unit.calculatedUnitSymbol

            values.map { it as AADD }.let { rs ->
                if(rs.any { it.isZero() })
                    return rs to unitSymbol
            }

            // The SI unit of mass already contains a prefix: prefixes are applied to gram (Mg, g, mg), not to kg
            val prefixableSymbol = if (unitSymbol.startsWith("kg")) unitSymbol.drop(1) else unitSymbol

            val bestSolution = ConversionTables.prefixes
                .filter { prefix -> prefix.key.isEmpty() || prefix.key.last() != 'i' }
                // prefix + symbol must not be a registered unit itself (e.g. P + S = "PS" is metric horsepower)
                .filter { prefix -> prefix.key.isEmpty() || (prefix.key + prefixableSymbol) !in ConversionTables.unitsMap }
                .maxByOrNull { prefix ->
                    val valuesInPrefix = valuesIn(prefix.key + prefixableSymbol).map {
                        (it as AADD).getRange()
                    }.filter {
                        it.isNotEmpty()
                    }
                    val min = valuesInPrefix.minOf { it.min }.toDouble().absoluteValue
                    val max = valuesInPrefix.maxOf { it.max }.toDouble().absoluteValue

                    when {
                        (min * 1.0001) < 1.0
                            -> Double.MIN_VALUE
                        min > 0.0 && (max / min) > 10e24
                            -> Double.MIN_VALUE
                        else -> prefix.value.factor
                    }
                }?.value ?: NoPrefix

            val finalUnitSymbol = when {
                unitSymbol == "m^2" && bestSolution == Hecto -> "ha"
                unitSymbol == "m^3" && bestSolution == Deci -> "l"
                unitSymbol == "m" && (bestSolution == Centi || bestSolution == Deci) -> "cm"
                bestSolution.symbol !in listOf("d", "c", "da", "h") || unitSymbol in listOf("m^2", "m^3") -> bestSolution.symbol + prefixableSymbol
                else -> unitSymbol
            }

            valuesIn(finalUnitSymbol) to finalUnitSymbol
        }
        // 3. Fallback to unitSpec (e.g. inherited type unit) if no derived unit symbol exists (e.g., m/s)
        unitSpec.isNotEmpty() -> valuesIn(unitSpec) to unitSpec
        else -> values to unit.toString()
    }.let { (x,y) -> Pair(x.map { it as AADD }, y) }

    private inline fun representDateBound(b : LongBound, fmt : (LocalDateTime) -> String) = when(b) {
        is LongBound.Finite -> fmt(LocalDateTime.ofEpochSecond(b.value, 0, UTC))
        LongBound.PositiveInfinity -> infinityString
        LongBound.NegativeInfinity -> negativeInfinityString
    }

    private inline fun representAsDateTime(x : NumberRange<DoubleBound>, fmt : (LocalDateTime) -> String) : String
    {
        val secs = roundOutwards(x)
        val min = representDateBound(secs.min, fmt)
        val max = representDateBound(secs.max, fmt)

        return when {
            min == max -> min
            else -> "$min..$max"
        }
    }

    fun representAsDateTime(x : NumberRange<DoubleBound>) = representAsDateTime(x) { it.toString() }

    fun representAsDate(x : NumberRange<DoubleBound>) = representAsDateTime(x) {
        // fixme: rounding direction?
        val rounded = if(it.hour >= 12) it.plusDays(1) else it
        rounded.format(ISO_LOCAL_DATE)
    }

    fun representAsMonth(x : NumberRange<DoubleBound>) = representAsDateTime(x) {
        // fixme: rounding direction? Month length?
        val rounded = if(it.dayOfMonth >= 15) it.plusMonths(1) else it
        //remove the day, because only the month should be considered
        rounded.format(ISO_LOCAL_DATE).substring(0..6)
    }

    fun representAsYear(x : NumberRange<DoubleBound>) = representAsDateTime(x) {
        val rounded = if(it.monthValue >= 7) it.plusYears(1) else it
        rounded.format(ISO_LOCAL_DATE).substring(0..3)
    }

    fun represent(vq : VectorQuantity) : String = buildString {
        val parens = vq.values.size != 1

        if(parens)
            append('(')

        val (xs, u) = when(vq.values[0]) {
            is AADD -> vq.optimalUnit()
            else -> Pair(vq.values, vq.unit.toString())
        }

        var formattedAsDate = false

        xs.joinTo(this, ", ") { v ->
            when(v) {
                is AADD -> when(u) {
                    else if v.isInfeasible() -> represent(v)
                    "DateTime" -> representAsDateTime(v).also { formattedAsDate = true }
                    "Date" -> representAsDate(v).also { formattedAsDate = true }
                    "Month" -> representAsMonth(v).also { formattedAsDate = true }
                    "Year" -> representAsYear(v).also { formattedAsDate = true }
                    else -> represent(v)
                }
                is IDD -> v.getRange().toString().let {
                    if(it == "-*..*") "*..*" else it // FIXME: WRONG!
                }
                is BDD -> v.value.toString()
                is StrDD -> v.toString()
            }
        }

        if(parens)
            append(')')

        if(!formattedAsDate && u !in listOf("1", "", "?"))
            append(' ').append(u) // no […] ?
    }

    /**
     * Formats a finite number using standard decimal notation for magnitudes
     * in [1e-3, 1e6) and engineering notation (exponent multiple of 3) otherwise.
     * Always uses [precision] significant figures.
     */
    fun formatNumber(num: Double): String {
        when {
            num.isNaN() -> return "NaN"
            num == Double.POSITIVE_INFINITY -> return infinityString
            num == Double.NEGATIVE_INFINITY -> return negativeinfinityString
            num == 0.0 || abs(num) < 1e-200 -> return "0"
        }

        val prefix = if (num < 0.0) "-" else ""
        val absNum = abs(num)
        val exp10 = floor(log10(absNum)).toInt()

        // Standard decimal notation for magnitudes between 0.001 and 1000000
        if (exp10 in -3..5) {
            val scale = max(0, precision - 1 - exp10)
            val bd = BigDecimal.valueOf(absNum).setScale(scale, RoundingMode.HALF_EVEN).stripTrailingZeros()

            return when {
                bd.compareTo(BigDecimal.ZERO) == 0 -> "0"
                bd >= BigDecimal.valueOf(1000000) -> "${prefix}1e6"
                else -> prefix + bd.toPlainString()
            }
        }

        // Engineering notation: exponent must be an integer multiple of 3
        var engExp = Math.floorDiv(exp10, 3) * 3
        val mantissa = absNum / 10.0.pow(engExp)
        // Significant digits for mantissa rounding:
        val mantissaExp = floor(log10(mantissa)).toInt()
        val scale = max(0, precision - 1 - mantissaExp)
        var bdMantissa = BigDecimal.valueOf(mantissa).setScale(scale, RoundingMode.HALF_EVEN).stripTrailingZeros()

        if (bdMantissa.compareTo(BigDecimal.ZERO) == 0)
            return "0"

        // In case rounding carried over (e.g. 999.9999 -> 1000)
        if (bdMantissa >= BigDecimal.valueOf(1000)) {
            bdMantissa = bdMantissa.divide(BigDecimal.valueOf(1000)).stripTrailingZeros()
            engExp += 3
        }

        return "${prefix}${bdMantissa.toPlainString()}e${engExp}"
    }

    companion object {

        val default = Representer()
        fun represent(value: AADD): String = default.represent(value)
        fun represent(bound: DoubleBound): String = default.represent(bound)
        fun formatNumber(num: Double): String = default.formatNumber(num)
    }
}
