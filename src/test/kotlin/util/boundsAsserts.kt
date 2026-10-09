package util

import com.github.tukcps.sysmd.cspsolver.Solver
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.equals
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.Integer
import io.github.tukcps.aadd.Real
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.bounds.*
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.ia.RealRange
import org.opentest4j.AssertionFailedError
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Uniform absolute tolerance for all range asserts */
const val TOLERANCE = 1e-9

/** The variable with the given path, or a test failure if it does not exist. */
fun Solver.variable(path : String, index : Int = 0) : Variable =
    getVariable(path, index) ?: throw AssertionFailedError("Variable $path not found")

/** @return A human-readable name for a specific vector component */
private fun Variable.name(ix : Int) = when {
    vectorQuantity.values.size == 1 -> path // intentionally not capitalized
    else -> "component #${ix+1} of $path"
}

/** Appends a user-supplied error message, if needed */
private infix fun String.suf(s : String?) = if(s !== null) "$this: $s" else this

private fun assertEmpty(range : NumberRange<*>, msg : String? = null) = assertTrue(range.isEmpty(), msg)

/** Asserts that every component of the variable has an empty range */
fun assertEmpty(v : Variable, msg : String? = null) =
    v.vectorQuantity.valuesInSI().forEach {
        when(it) {
            is Real -> assertEmpty(it.getRange(), msg)
            is Integer -> assertEmpty(it.getRange(), msg)
            else -> throw AssertionFailedError("${v.path} has wrong type" suf msg)
        }
    }

private fun DoubleBound.asDouble() = when(this) {
    is DoubleBound.Finite -> value
    DoubleBound.PositiveInfinity -> Double.POSITIVE_INFINITY
    DoubleBound.NegativeInfinity -> Double.NEGATIVE_INFINITY
}
private fun DoubleBound.isAbove(v : Double, tol : Double) = asDouble() > v + tol
private fun DoubleBound.isBelow(v : Double, tol : Double) = asDouble() < v - tol

/** Asserts that a range is equal to a kotlin range
 * @param unit unit, for formatting only
 * */
private fun checkRealRange(want : ClosedRange<Double>, got : RealRange, absoluteTolerance : Double = TOLERANCE, msg : String? = null, unit : String = "")
{
    val unitSuf = if(unit.isNotEmpty()) " [$unit]" else ""
    when {
        want.isEmpty() -> assertEmpty(got, msg)
        got.isEmpty() -> throw AssertionFailedError("Expected $want$unitSuf but got ∅" suf msg)
        !got.min.equals(want.start, absoluteTolerance) || !got.max.equals(want.endInclusive, absoluteTolerance)
            -> throw AssertionFailedError("Expected $want$unitSuf but got $got$unitSuf" suf msg)
        (got.min.isAbove(want.start, absoluteTolerance) || got.max.isBelow(want.endInclusive, absoluteTolerance))
            -> throw AssertionFailedError("Expected $want$unitSuf but got $got$unitSuf (not an over-approximation)" suf msg)
    }
}

/** Asserts that a range is equal to a kotlin range */
private fun checkIntRange(want : ClosedRange<*>, got : IntegerRange, msg : String? = null) = when {
    want.isEmpty() -> assertEmpty(got, msg)
    got.isEmpty() || got.start != want.start.toLongBound() || got.endInclusive != want.endInclusive.toLongBound()
        -> throw AssertionFailedError("Expected $want but got $got" suf msg)
    else -> {}
}

/** A long, or +-Infinity (as Double) for an unbounded integer range */
private fun Any?.toLongBound() : LongBound = when {
    this is Long -> LongBound.Finite(this)
    this == Double.POSITIVE_INFINITY -> LongBound.PositiveInfinity
    this == Double.NEGATIVE_INFINITY -> LongBound.NegativeInfinity
    this is Double -> LongBound.Finite(toLong())
    else -> throw IllegalArgumentException("Not a bound of an integer range: $this")
}

/** The values in [unit]; without a unit (the default) the plain SI values. */
private fun VectorQuantity.valuesFor(unit : String) = if(unit.isEmpty()) valuesInSI() else valuesIn(unit)

/** Human-readable name of component [ix] of a value, for error messages */
private fun Any.describe(ix : Int, size : Int) = when(this) {
    is Variable -> name(ix)
    else -> if(size == 1) "value" else "component #${ix+1}"
}

private fun Any.toVQ() : VectorQuantity = when(this) {
    is Variable -> vectorQuantity
    is VectorQuantity -> this
    else -> throw IllegalArgumentException("Cannot assert a range on ${this::class.simpleName}")
}

@Suppress("UNCHECKED_CAST")
private fun ClosedRange<*>.asDoubles() : ClosedRange<Double> =
    (this as? ClosedRange<Double>)?.takeIf { start is Double }
        ?: (this as ClosedRange<Long>).let { it.start.toDouble()..it.endInclusive.toDouble() }

/** Normalizes an expected value (number, range, or a list of those) into one range per component */
private fun Any.toRanges() : List<ClosedRange<*>> = when(this) {
    is List<*> -> flatMap { it!!.toRanges() }
    is ClosedRange<*> -> listOf(this)
    is Double -> listOf(this..this)
    is Float -> listOf(toDouble()..toDouble())
    is Long -> listOf(this..this)
    is Int -> listOf(toLong()..toLong())
    else -> throw IllegalArgumentException("Unsupported expected value: $this")
}

/**
 * Asserts that the bounds of [got] are the expected ones: tight (within the uniform [TOLERANCE]) and with safe inclusion:
 * every bound must equal the expected bound, and the range must include it (min <= want <= max).
 *
 * @param want expected value: a number (a point, i.e. x..x), a range `a..b` (Double or Long), or a list of those, one per component
 * @param got the checked value: a [Variable] (or a [VectorQuantity]/[Real]/[Integer] of a calculation without a solver)
 * @param unit unit in which the value is read; by default SI
 * @param msg additional message on failure
 */
fun assertBounds(want : Any, got : Any, unit : String = "", msg : String? = null)
{
    val wants = want.toRanges()
    when(got)
    {
        is Real -> return checkRealRange(wants.single().asDoubles(), got.getRange(), TOLERANCE, msg)
        is Integer -> return checkIntRange(wants.single(), got.getRange(), msg)
        // ranges that are no value of a variable: range specifications, multiplicities
        is RealRange -> return checkRealRange(wants.single().asDoubles(), got, TOLERANCE, msg)
        is IntegerRange -> return checkIntRange(wants.single(), got, msg)
    }

    val values = got.toVQ().valuesFor(unit)
    assertEquals(wants.size, values.size, "Dimensionality mismatch" suf msg)
    for((ix, wg) in (wants zip values).withIndex())
    {
        val (w, g) = wg
        val where = (if(values.size == 1) "" else "in component #${ix+1}") suf msg
        when(g)
        {
            is Real -> checkRealRange(w.asDoubles(), g.getRange(), TOLERANCE, where, unit)
            is Integer -> checkIntRange(w, g.getRange(), where)
            else -> throw AssertionFailedError("${got.describe(ix, values.size)} has wrong type" suf msg)
        }
    }
}
