package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.*
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.bounds.LongBound
import io.github.tukcps.aadd.values.bounds.LongMath.max
import io.github.tukcps.aadd.values.bounds.LongMath.min
import kotlin.math.max
import kotlin.math.min

/**
 * Predefined functions: step
 */
internal class AstStepInterpolation(model: Session, args: ArrayList<AstNode>) :
    AstFunction("stepInterpolation", model, 5, args) {

    private val numberOfParameters = parameters.size

    /** The (x, y) pairs, read from the current upQuantities of the parameters so they never go stale. */
    private val points: List<Pair<VectorQuantity, VectorQuantity>>
        get() = (1 until numberOfParameters step 2).map {
            Pair(getParam(it).upQuantity.asQuantity(), getParam(it + 1).upQuantity.asQuantity())
        }

    init {
        if (numberOfParameters % 2 != 1 || numberOfParameters < 3)
            throw SemanticError("Step function needs an odd number of Real or Integer parameters (at least 3)")
    }

    override fun initialize() {
        val points = this.points
        if (parameters.any { it.upQuantity.values.size != 1 })
            throw VectorDimensionError("StepInterpolation is not possible with Vectors of size > 1")
        for (i in 0 until points.size - 1) {
            val currX = points[i].first
            val nextX = points[i + 1].first
            if (currX.value is AADD && nextX.value is AADD) {
                if (currX.getMaxAsDouble() >= nextX.getMinAsDouble()) {
                    throw SemanticError("StepInterpolation function needs all x-values to be in increasing order")
                }
            } else if (currX.value is IDD && nextX.value is IDD) {
                if (currX.idd().max >= nextX.idd().min) {
                    throw SemanticError("StepInterpolation function needs all x-values to be in increasing order")
                }
            } else {
                throw SemanticError("StepInterpolation function needs all x-values to be of the same type")
            }
            if (points[i].second.value is AADD && points[i + 1].second.value !is AADD || points[i].second.value is IDD && points[i + 1].second.value !is IDD)
                throw SemanticError("StepInterpolation function needs all y-values to be of the same type (IDD or AADD)")
        }
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(model.builder.Reals.All, "?")
            is IDD -> VectorQuantity(model.builder.Integers.All)
            else -> throw SemanticError("Step only takes Real and Integer parameters")
        }
        evalUp()
        downQuantity = upQuantity.clone()

    }

    /**
     * Parameters are:
     * - 0 = x-value of result
     * - 1,2 = x0, y0 of input
     * - 3,4 = x1, y1 of input
     * - ...
     */
    override fun evalUp() {
        val x = getParam(0).upQuantity
        upQuantity = points[0].second
        for ((index, value) in points)
            upQuantity = (x ge index).bdd().ite(value, upQuantity).clone()
    }

    /**
     * The x-region in which the result is y_i reaches from x_i to x_(i+1); for the first point it also
     * includes everything below x_0 (evalUp returns y_0 there), for the last point everything above x_n.
     * A region is kept if its y-value overlaps the range of the result.
     */
    override fun evalDown() {
        val points = this.points
        val x = getParam(0)
        if (getParam(1).isReal) {
            val lo = downQuantity.getMinAsDouble()
            val hi = downQuantity.getMaxAsDouble()
            var startingPoint = Double.POSITIVE_INFINITY
            var endingPosition = Double.NEGATIVE_INFINITY
            for ((i, element) in points.withIndex()) {
                if (element.second.getMaxAsDouble() < lo || element.second.getMinAsDouble() > hi)
                    continue

                startingPoint = min(if (i == 0) Double.NEGATIVE_INFINITY else element.first.getMinAsDouble(), startingPoint)
                endingPosition = max(
                    if (i + 1 < points.size) points[i + 1].first.getMaxAsDouble() else Double.POSITIVE_INFINITY,
                    endingPosition
                )
            }
            val range = if (startingPoint <= endingPosition) model.builder.real(startingPoint..endingPosition)
                        else model.builder.Reals.Empty
            x.downQuantity = x.downQuantity.constrain(VectorQuantity(range, x.downQuantity.unit, x.downQuantity.unitSpec))
        } else if (getParam(1).isInt) {
            val lo = downQuantity.idd().getRange().min
            val hi = downQuantity.idd().getRange().max
            var startingPoint: LongBound = LongBound.PositiveInfinity
            var endingPosition: LongBound = LongBound.NegativeInfinity
            for ((i, element) in points.withIndex()) {
                val y = element.second.idd().getRange()
                if (y.max < lo || y.min > hi)
                    continue
                // only discrete solutions possible
                startingPoint = min(if (i == 0) LongBound.NegativeInfinity else element.first.idd().min, startingPoint)
                endingPosition = max(
                    if (i + 1 < points.size) points[i + 1].first.idd().max else LongBound.PositiveInfinity,
                    endingPosition
                )
            }
            val range = if (startingPoint <= endingPosition) model.builder.integer(startingPoint..endingPosition)
                        else model.builder.Integers.Empty
            x.downQuantity = x.downQuantity.constrain(VectorQuantity(range))
        }
    }

    override fun clone() = AstStepInterpolation(model, cloneParameters())
}
