package util

import com.github.tukcps.sysmd.cspsolver.Solver
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.quantities.Representer
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.Integer
import io.github.tukcps.aadd.Real
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.util.Tolerance
import io.github.tukcps.aadd.util.isNotEmpty
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.bounds.DoubleBound
import io.github.tukcps.aadd.values.bounds.LongBound
import io.github.tukcps.aadd.values.bounds.minus
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.ia.RealRange
import org.opentest4j.AssertionFailedError

/** Uniform absolute tolerance for all range asserts */
val TOLERANCE = Tolerance(absolute = 1e-9)

private class MessageBuilder(got : Any, val unit : String, val msg : String, val repr : Representer = Representer.default)
{
    private val isScalar : Boolean
    private val path : String?
    /** index at which values are being compared */
    var index : Int? = null

    init {
        when(got) {
            is Variable -> {
                path = got.path
                isScalar = got.vectorQuantity.values.size == 1
            }
            is VectorQuantity -> {
                path = null
                isScalar = got.values.size == 1
            }
            else -> {
                path = null
                isScalar = true
            }
        }
    }

    /** Determines if two numbers are different but are very similar in printed form.
     * Used to decide when to annotate with the actual value difference.
     */
    private fun confusinglyClose(a : DoubleBound, b : DoubleBound) : Boolean
    {
        if(a == b)
            return false

        val sa = a.toString()
        val sb = b.toString()

        val ea = sa.dropWhile { it != 'e' }
        val eb = sb.dropWhile { it != 'e' }

        return when {
            // if exponents aren't equal, the difference is obvious
            ea != eb -> false
            // either sign or whole part differs, also obvious
            sa.indexOf('.') != sb.indexOf('.') -> false
            // check if strings start with similar digits
            else -> sa.take(5) == sb.take(5)
        }
    }

    private fun formatDelta(want : Any, got : Any) : String
    {
        when {
            want !is RealRange || got !is RealRange -> return ""
            want.isEmpty() || got.isEmpty() -> return ""
            // only print numbers that are very close where the difference isn't immediately obvious
            !confusinglyClose(want.min, got.min) && !confusinglyClose(want.max, got.max) -> return ""
        }

        val dmin = got.min - want.min
        val dmax = got.max - want.max

        // ensure positive numbers are printed with sign
        val lsign = if(dmin !== null && dmin > 0) "+" else ""
        val rsign = if(dmax !== null && dmax > 0) "+" else ""

        return "difference: $lsign$dmin/$rsign$dmax"
    }

    private fun StringBuilder.suffix()
    {
        if(msg.isNotEmpty())
            append(": ").append(msg)
    }

    private fun StringBuilder.value(v : Any, withUnit : Boolean)
    {
        append(when(v) {
            is AADD -> repr.represent(v)
            is DoubleBound -> repr.represent(v)
            else -> v.toString()
        })

        if(withUnit && unit.isNotEmpty())
            append(" [").append(unit).append(']')
    }

    private fun expectButGot(
        prefix : String, want : Any, got : Any,
        withUnit : Boolean = false, extra : String = "", withDelta : Boolean = false
    ) : Nothing = throw AssertionFailedError(buildString {
        append(prefix)

        if(prefix.isNotEmpty())
            append("; ")

        append("Expected ")
        value(want, withUnit)
        append(" but got ")
        value(got, withUnit)

        listOf(
            extra,
            if(withDelta) formatDelta(want, got) else ""
        ).filter {
            it.isNotEmpty()
        }.takeIf {
            it.isNotEmpty()
        }?.joinTo(this, separator = "; ", prefix = " (", postfix = ")")

        suffix()
    })

    private fun componentPrefix() = when {
        !isScalar && index !== null -> "in component #$index" + (path?.let { " of $it" } ?: "")
        path !== null -> "in $path"
        else -> ""
    }

    fun typeMismatch(want : String, got : String) : Nothing = expectButGot(
        prefix = path?.let { "$it has wrong type" } ?: "type mismatch",
        want, got
    )

    fun dimensionMismatch(want : Int, got : Int) : Nothing = expectButGot(
        prefix = path?.let { "$it has wrong dimension" } ?: "dimension mismatch",
        want.toString(), got.toString()
    )

    fun valueMismatch(want : Any, got : Any) : Nothing = expectButGot(
        componentPrefix(), want, got, withUnit = true, withDelta = true
    )

    fun noSafeInclusion(want : RealRange, got : RealRange) : Nothing = expectButGot(
        componentPrefix(), want, got, withUnit = true, extra = "not an over-approximation", withDelta = true
    )
}

/** Retrieves the bounds of a vector quantity's values
 * Converts those values to the target unit, if one is set.
 */
private fun VectorQuantity.valueRanges(mb : MessageBuilder) : List<NumberRange<*>> = when {
    mb.unit.isEmpty() -> valuesInSI()
    else -> valuesIn(mb.unit)
}.map {
    when(it) {
        is Real -> it.getRange()
        is Integer -> it.getRange()
        is BDD -> mb.typeMismatch("any numeric DD", "BDD")
        is StrDD -> mb.typeMismatch("any numeric DD", "StrDD")
    }
}

/** Normalizes an expected value (number, range, or a list of those) into one range per component */
@Suppress("UNCHECKED_CAST")
private fun Any.toRanges() : List<NumberRange<*>> = when(this) {
    is List<*> -> flatMap { it!!.toRanges() }
    is NumberRange<*> -> listOf(when(start) {
        is DoubleBound -> RealRange(this as NumberRange<DoubleBound>)
        is LongBound -> IntegerRange(this as NumberRange<LongBound>)
    })
    is ClosedRange<*> if start is Double -> listOf(RealRange(start as Double, endInclusive as Double))
    is ClosedRange<*> if start is Float -> listOf(RealRange((start as Float).toDouble(), (endInclusive as Float).toDouble()))
    is ClosedRange<*> if start is Long -> listOf(IntegerRange(start as Long, endInclusive as Long))
    is ClosedRange<*> if start is Int -> listOf(IntegerRange((start as Int).toLong(), (endInclusive as Int).toLong()))
    is Double -> listOf(RealRange(this, this))
    is Float -> listOf(RealRange(toDouble(),toDouble()))
    is Long -> listOf(IntegerRange(this, this))
    is Int -> listOf(IntegerRange(toLong(), toLong()))
    else -> throw IllegalArgumentException("Not a numeric range type: $this")
}

/** Asserts that [want] and [got] are (nearly) equal */
private fun checkValues(want : List<NumberRange<*>>, got : List<NumberRange<*>>, mb : MessageBuilder, tol : Tolerance)
{
    if(want.size != got.size)
        mb.dimensionMismatch(want.size, got.size)

    for((ix,wg) in (want zip got).withIndex())
    {
        mb.index = ix
        val (w,g) = wg

        if(w.isEmpty() && g.isEmpty())
            continue
        if(w.isEmpty() || g.isEmpty())
            mb.valueMismatch(w, g)

        when(w)
        {
            is IntegerRange -> {
                val g = wg.second as? IntegerRange ?: mb.typeMismatch("Integer", "Real")

                if(w != g)
                    mb.valueMismatch(w, g)
            }
            is RealRange -> {
                val g = wg.second as? RealRange ?: mb.typeMismatch("Real", "Integer")

                when {
                    !tol.areEqual(w.min, g.min) || !tol.areEqual(w.max, g.max)
                        -> mb.valueMismatch(w, g)
                    // this has to be checked without tolerance
                    g.min > w.min || g.max < w.max
                        -> mb.noSafeInclusion(w, g)
                }
            }
            else -> throw IllegalStateException("unreachable")
        }
    }
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
fun assertBounds(want : Any, got : Any, unit : String = "", msg : String = "", tol : Tolerance = TOLERANCE)
{
    val mb = MessageBuilder(got, unit, msg)
    val wants = want.toRanges()

    when(got) {
        is Variable -> checkValues(wants, got.vectorQuantity.valueRanges(mb), mb, tol)
        is VectorQuantity -> checkValues(wants, got.valueRanges(mb), mb, tol)
        else -> {
            require(unit.isEmpty()) { "Cannot assert a unit on a plain value" }
            checkValues(wants, got.toRanges(), mb, tol)
        }
    }
}

/** Asserts that every component of the variable has an empty range */
fun assertEmpty(v : Variable, msg : String = "")
{
    val mb = MessageBuilder(v, "", msg)
    v.vectorQuantity.valueRanges(mb).forEach {
        if(it.isNotEmpty())
            mb.valueMismatch("∅", it)
    }
}

/** The variable with the given path, or a test failure if it does not exist. */
fun Solver.variable(path : String, index : Int = 0) : Variable =
    getVariable(path, index) ?: throw AssertionFailedError("Variable $path not found")
