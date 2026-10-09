package com.github.tukcps.sysmd

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.DDBuilder.IntMath.div
import io.github.tukcps.aadd.DDBuilder.RealMath.div
import io.github.tukcps.aadd.DDException
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.ScalarValue
import io.github.tukcps.aadd.values.bounds.*
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.aa.AffineForm
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.rounding.Rounding
import io.github.tukcps.aadd.values.real.rounding.Rounding.*
import kotlin.math.*

// bound should be sealed class; makes Comparable instance correct (currently partial!)
operator fun Long.compareTo(other : Bound) = other.compareTo(this).unaryMinus()
operator fun Double.compareTo(other : Bound) = other.compareTo(this).unaryMinus()

fun DDBuilder.condition(ix : Int) = BDD.Internal(this, ix, Bool.True, Bool.False)

/** Monadic bind for DDs (>>= from Haskell!)
 * @param R _Must_ be a concrete type, i.e. not an interface.
 * @param f Applied to every leaf in the input which is substituted with the given leaf in the target type.
 */
@Suppress("UNCHECKED_CAST")
fun<T : ScalarValue, R : DD<*>> DD<T>.bind(f : (T) -> R) : R = when(this) {
    // ite() should have signature DD<T> -> DD<T> -> DD<T>
    is DD.Internal<T> -> builder.condition(index).ite( T.bind(f), F.bind(f) ) as R
    is DD.Leaf<T> -> f(value)
}

// Fixme: empty range is formatted as "*..-*" instead of Ø? ---> of which Class? RealRange is ok.

/** needed because abs(a - b) will be partial when dealing with equal infinities, whereas this will be total */
fun diff(a : LongBound, b : LongBound) : LongBound = when {
    // also catches equal infinities
    a == b -> LongBound.Finite(0)
    a is LongBound.Finite && b is LongBound.Finite -> try {
        val y = when {
            a.value > b.value -> Math.subtractExact(a.value, b.value)
            else -> Math.subtractExact(b.value, a.value)
        }

        if(y == Long.MAX_VALUE)
            LongBound.PositiveInfinity
        else
            LongBound.Finite(y)
    } catch(_ : ArithmeticException) {
        // only way is to overflow (which we turn into wrapping overflow)
        LongBound.PositiveInfinity
    }
    else -> LongBound.PositiveInfinity
}

fun diff(a : DoubleBound, b : DoubleBound) : DoubleBound = when {
    a == b -> DoubleBound.Finite(0.0)
    a is DoubleBound.Finite && b is DoubleBound.Finite -> DoubleBound.Finite(abs(a.value - b.value))
    else -> DoubleBound.PositiveInfinity
}

fun<T : Bound?> max(a : T, b : T) : T = when {
    a === null -> b
    b === null -> a
    a > b -> a
    else -> b
}

// fixme: why isn't .. overloaded to make RealRange from DoubleBounds
fun convexHull(n : ClosedRange<LongBound>) : RealRange =
    RealRange(n.start.toDoubleBound(DOWN), n.endInclusive.toDoubleBound(UP))

fun DoubleBound.truncate() : LongBound = when(this) {
    is DoubleBound.Finite -> when {
        value < Long.MIN_VALUE -> LongBound.NegativeInfinity
        value > Long.MAX_VALUE -> LongBound.PositiveInfinity
        else -> LongBound.Finite(value.toLong())
    }
    DoubleBound.NegativeInfinity -> LongBound.NegativeInfinity
    DoubleBound.PositiveInfinity -> LongBound.PositiveInfinity
}

inline fun DoubleBound.round(f : (Double) -> Double) : LongBound = when(this) {
    is DoubleBound.Finite -> DoubleBound.Finite(f(value)).truncate()
    DoubleBound.NegativeInfinity -> LongBound.NegativeInfinity
    DoubleBound.PositiveInfinity -> LongBound.PositiveInfinity
}

fun roundOutwards(r : ClosedRange<DoubleBound>) : IntegerRange = when {
    r.isEmpty() -> IntegerRange.Empty
    else -> IntegerRange(
        r.start.round(::floor),
        r.endInclusive.round(::ceil)
    )
}

fun roundInwards(r : RealRange) : IntegerRange = when {
    r.isEmpty() -> IntegerRange.Empty
    else -> IntegerRange(
        r.start.round(::ceil),
        r.endInclusive.round(::floor)
    )
}

// FIXME: IntegerRange is not iterable
val ClosedRange<LongBound>.values get() = sequence {
    var i = (start as? LongBound.Finite ?: throw IllegalArgumentException("start of range must be finite")).value

    while(i <= endInclusive)
    {
        yield(i)
        i += 1
    }
}

fun DoubleBound.equals(value : DoubleBound, absoluteTolerance : Double) = when {
    this == value -> true
    this !is DoubleBound.Finite || value !is DoubleBound.Finite -> false
    else -> abs(value.value - this.value) <= absoluteTolerance
}

fun DoubleBound.equals(value : Double, absoluteTolerance : Double) = when(value) {
    Double.POSITIVE_INFINITY -> this === DoubleBound.PositiveInfinity
    Double.NEGATIVE_INFINITY -> this === DoubleBound.NegativeInfinity
    else if value.isNaN() -> throw IllegalArgumentException("Cannot compare to NaN")
    else -> this.equals(DoubleBound.Finite(value), absoluteTolerance)
}


/**
 * Computes the intersection of two AADD; for Affine Forms, it considers constraints and LP problem.
 * (TODO!)
 */
@Deprecated("Use fixed version in v0.9.6", ReplaceWith("intersect"))
infix fun AADD.intersectFix(other: NumberRange<DoubleBound>): AADD = when {
    other is AADD       -> apply(other, AffineForm::intersect)
    else                -> apply(builder.real(other), op = AffineForm::intersect)
}

@Deprecated("Use fixed version in v0.9.6")
fun DD<*>.divFix(other: DD<*>): DD<*> = when(this) {
    is AADD if other is AADD -> (this.div(other))
    is IDD  if other is IDD  -> (this.div(other))
    else -> throw DDException("Division of incompatible types.")
}

/** Widens this range by a whole number of ULPs */
fun RealRange.widenByULP(n : Int) : RealRange
{
    fun widen(b : DoubleBound, m : Int) = when(b) {
        is DoubleBound.Finite -> DoubleBound.Finite(b.value + m * b.value.ulp)
        DoubleBound.NegativeInfinity, DoubleBound.PositiveInfinity -> b
    }

    return RealRange(widen(min, -n), widen(max, n))
}

/** The center point of a range. May be infinite for scalar ranges of infinite values.
 * `null` if no such point exists, i.e. on empty range, ranged with mixed finite/infinite sides, or `-*..*`
 */
val RealRange.center : DoubleBound? get() {
    val low = min
    val high = max

    return when {
        isEmpty() -> null
        low == high -> low
        low is DoubleBound.Finite && high is DoubleBound.Finite -> DoubleBound.Finite((low.value + high.value)/2)
        // either mixing finite/infinite, or unequal infinities i.e. `-*..*`
        else -> null
    }
}


/**
 * Parses a RealRange from a SysML/KerML range string specification.
 * Handles SysML/KerML infinity syntax where `*..*` or `*` means unbounded (all reals, `RealRange.Reals`),
 * and `*..x` means unbounded below (`-*..x`).
 */
fun parseRealRange(spec: String): RealRange {
    val s = spec.trim('"', '(', ')', '[', ']', ' ')
    val trimmed = s.replace(" ", "")
    return when {
        // FIXME: When adopting -*..*, ensure "-*..*" and "-*" are handled explicitly alongside "*..*"
        trimmed.isBlank() || trimmed == "*" || trimmed == "*..*" || trimmed == "-*..*" -> RealRange.Reals
        trimmed.startsWith("*..") -> RealRange.parse("-$trimmed")
        else -> RealRange.parse(trimmed)
    }
}

/**
 * Parses an IntegerRange from a SysML/KerML range string specification.
 * Handles SysML/KerML infinity syntax where `*..*` or `*` means unbounded (`IntegerRange.All`),
 * and `*..x` means unbounded below (`-*..x`).
 */
fun parseIntegerRange(spec: String): IntegerRange {
    val s = spec.trim('"', '(', ')', '[', ']', ' ')
    val trimmed = s.replace(" ", "")
    return when {
        // FIXME: When adopting -*..*, ensure "-*..*" and "-*" are handled explicitly alongside "*..*"
        trimmed.isBlank() || trimmed == "*" || trimmed == "*..*" || trimmed == "-*..*" -> IntegerRange.All
        trimmed.startsWith("*..") -> IntegerRange.parse("-$trimmed")
        else -> IntegerRange.parse(trimmed)
    }
}

operator fun AADD.unaryMinus() : AADD = DDBuilder.RealMath.negate(this)
