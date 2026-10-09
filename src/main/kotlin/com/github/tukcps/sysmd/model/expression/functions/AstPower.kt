package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.DDBuilder.IntMath.root
import io.github.tukcps.aadd.DDBuilder.RealMath
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.values.bounds.*
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble

/**
 * Predefined functions: x^y (x to the power of y)
 */
internal class AstPower(model: Session, args: ArrayList<AstNode>) : AstFunction("power", model, 2, args) {

    private val exponent: DD<*>
        get() = getParam(1).dd

    init {
        if (args.size != 2)
            throw SemanticError("Power function expects two parameters of types Real or Integer")
    }

    override fun initialize() {
        if (getParam(1).upQuantity.values.size != 1)
            throw VectorDimensionError("Power function is not possible with Vector as exponent")
        if (getParam(0).upQuantity.values[0] is IDD && exponent !is IDD)
            throw SemanticError("Power function with an Integer base requires an Integer exponent")
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(model.builder.Reals.All)
            is IDD -> VectorQuantity(model.builder.Integers.All)
            else -> throw SemanticError("Power function must have Real or Integer parameters")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = if (upQuantity.values[0] is AADD)
            getParam(0).upQuantity.pow(getParam(1).aadd)
        else
            getParam(0).upQuantity.pow(getParam(1).idd)

    }

    override fun evalDown() {
        val baseQuantity = when (exponent) {
            is AADD -> {
                val expAadd = exponent.asAadd()
                val e = if (expAadd.min == expAadd.max) expAadd.min.toDouble() else Double.NaN
                val isInteger = !e.isNaN() && !e.isInfinite() && e == e.toLong().toDouble()
                val isEvenInt = isInteger && e != 0.0 && e.toLong() % 2L == 0L
                val isOddInt = isInteger && e.toLong() % 2L != 0L
                val resultingValues = mutableListOf<AADD>()
                downQuantity.values.forEachIndexed { i, it ->
                    val z = it.asAadd()
                    when {
                        // Range of positive exponents with a non-negative base: the root is monotonic in the exponent.
                        e.isNaN() && expAadd.min > 0.0 && (getParam(0).downQuantity.values.getOrNull(i)?.asAadd()?.min ?: DoubleBound.Finite(-1.0)) >= 0.0 ->
                            resultingValues.add(
                                if (z.max < 0.0) model.builder.Reals.Empty
                                else RealMath.pow(if (z.min < 0.0) model.builder.real(0.0 .. z.max.toDouble()) else z, model.builder.real(1.0).div(expAadd).asAadd())
                            )
                        // Exponent 0 (result is always 1) or other ranges of exponents: no safe inverse for the base.
                        e.isNaN() || e == 0.0 || e.isInfinite() -> resultingValues.add(model.builder.Reals.All)
                        isEvenInt -> {
                        if (z.max < 0.0) {
                            resultingValues.add(model.builder.Reals.Empty)
                        } else {
                            val clampedZ = if (z.min < 0.0)
                                model.builder.real(0.0 .. z.max.toDouble())
                            else z
                            val rootVal = RealMath.pow(clampedZ, 1.0 / e)
                            val currentBase = getParam(0).downQuantity.values.getOrNull(i)?.asAadd()
                            val rootMin = rootVal.min
                            val rootMax = rootVal.max
                            resultingValues += when {
                                currentBase != null && currentBase.min >= 0.0 -> rootVal
                                currentBase != null && currentBase.max <= 0.0 ->
                                    model.builder.real(-rootMax .. if (rootMin.isZero) DoubleBound.Finite(0.0) else -rootMin)
                                else ->
                                    model.builder.real(-rootMax .. rootMax)
                            }
                        }
                        }
                        // Odd integer exponent: sign is preserved, so the negative and positive parts are inverted separately.
                        isOddInt -> {
                            var lo = Double.POSITIVE_INFINITY
                            var hi = Double.NEGATIVE_INFINITY
                            if (z.min < 0.0) {
                                val magnitude = model.builder.real(kotlin.math.max(-z.max.toDouble(), 0.0) .. -z.min.toDouble())
                                val r = RealMath.pow(magnitude, 1.0 / e)
                                lo = kotlin.math.min(lo, -r.max.toDouble())
                                hi = kotlin.math.max(hi, -r.min.toDouble())
                            }
                            if (z.max >= 0.0) {
                                val positive = model.builder.real(kotlin.math.max(z.min.toDouble(), 0.0) .. z.max.toDouble())
                                val r = RealMath.pow(positive, 1.0 / e)
                                lo = kotlin.math.min(lo, r.min.toDouble())
                                hi = kotlin.math.max(hi, r.max.toDouble())
                            }
                            resultingValues.add(if (lo <= hi) model.builder.real(lo..hi) else model.builder.Reals.Empty)
                        }
                        // Fractional exponent: only non-negative bases are defined.
                        else -> resultingValues.add(
                            if (z.max < 0.0) model.builder.Reals.Empty
                            else RealMath.pow(if (z.min < 0.0) model.builder.real(0.0 .. z.max.toDouble()) else z, 1.0 / e)
                        )
                    }
                }
                VectorQuantity.fromCanonical(resultingValues, getParam(0).downQuantity.unit, getParam(0).downQuantity.unitSpec, getParam(0).downQuantity.userWantedUnitSpec)
            }
            is IDD -> {
                val expIdd = exponent as IDD
                val minVal = expIdd.min
                val isEvenInt = minVal == expIdd.max &&
                        minVal is LongBound.Finite &&
                        minVal.value > 0L &&
                        minVal.value % 2L == 0L
                val resultingValues = mutableListOf<IDD>()
                downQuantity.values.forEachIndexed { i, it ->
                    val z = it as IDD
                    if (isEvenInt) {
                        if (z.max < 0L) {
                            resultingValues.add(model.builder.Integers.Empty)
                        } else {
                            val clampedZ = if (z.min < 0L)
                                model.builder.integer(0L .. z.max)
                            else z
                            val rootVal = root(clampedZ, expIdd)
                            val currentBase = getParam(0).downQuantity.values.getOrNull(i) as? IDD
                            val rootMin = rootVal.min
                            val rootMax = rootVal.max
                            resultingValues += when {
                                currentBase !== null && currentBase.min >= 0L -> rootVal
                                currentBase !== null && currentBase.max <= 0L
                                    -> model.builder.integer(-rootMax .. -rootMin)
                                else -> model.builder.integer(-rootMax .. rootMax)
                            }
                        }
                    } else {
                        resultingValues.add(root(z, expIdd))
                    }
                }
                VectorQuantity(resultingValues)
            }
            else -> throw SemanticError("Expected base of type Real or Integer")
        }
        val constrainedBase = getParam(0).downQuantity.constrain(baseQuantity)
        getParam(0).downQuantity = constrainedBase

        val newExponent = when(exponent) {
            is AADD -> {
                val baseVal = constrainedBase.values[0].asAadd()
                val downVal = downQuantity.aadds()[0]
                when {
                    baseVal.min <= 1.0 || downVal.min <= 0.0 -> when {
                        baseVal.min > 0.0 && downVal.max <= 0.0 -> VectorQuantity.fromCanonical(
                            mutableListOf(model.builder.Reals.Empty)
                        )
                        else -> VectorQuantity.fromCanonical(
                            model.builder.Reals.All
                        )
                    }
                    else -> {
                        val logRes = downQuantity.log(constrainedBase)
                        VectorQuantity.fromCanonical(logRes.values)
                    }
                }
            }

            is IDD -> {
                val baseVal = constrainedBase.values[0] as IDD
                val downVal = downQuantity.idds()[0]
                when {
                    baseVal.min <= 1L || downVal.min <= 0L -> when {
                        baseVal.min > 0L && downVal.max <= 0L
                            -> VectorQuantity(mutableListOf(model.builder.Integers.Empty))
                        else -> VectorQuantity(model.builder.Integers.All)
                    }
                    else -> downQuantity.log(constrainedBase)
                }
            }

            else -> throw SemanticError("Expected base of type Real or Integer")
        }
        getParam(1).downQuantity = getParam(1).downQuantity.constrain(newExponent)
    }


    override fun clone() = AstPower(model, cloneParameters())
}
