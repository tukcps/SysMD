package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.ite
import com.github.tukcps.sysmd.services.session.Session
import kotlin.math.max
import kotlin.math.min

/**
 * Predefined functions: step
 */
internal class AstStepInterpolation(model: Session, args: ArrayList<AstNode>) :
    AstFunction("stepInterpolation", model, 5, args) {

    private val numberOfParameters = parameters.size
    private val points = mutableListOf<Pair<Quantity, Quantity>>()

    init {
        if (numberOfParameters % 2 != 1 && numberOfParameters >= 3)
            throw SemanticError("Step function needs an odd number of Real or Integer parameters")

        for (i in 1..args.indices.last step 2)
            points.add(Pair(getParam(i).upQuantity.asQuantity(), getParam(i + 1).upQuantity.asQuantity()))
    }

    override fun initialize() {
        if (parameters.any { it.upQuantity.values.size != 1 })
            throw VectorDimensionError("StepInterpolation is not possible with Vectors of size > 1")
        for (i in points.indices) {
            if (i < points.size - 1) {
                if(points[i].first.value is AADD && points[i + 1].first.value is AADD)
                    if (points[i].first.getMaxAsDouble() >= points[i + 1].first.getMinAsDouble())
                        throw SemanticError("StepInterpolation function needs all x-values to be in increasing order")
                else if(points[i].first.value is IDD && points[i + 1].first.value is IDD)
                    if (points[i].first.idd().max >= points[i + 1].first.idd().min)
                        throw SemanticError("StepInterpolation function needs all x-values to be in increasing order")
                else
                    throw SemanticError("StepInterpolation function needs all x-values to be of the same type")
                if(points[i].second.value is AADD && points[i + 1].second.value !is AADD || points[i].second.value is IDD && points[i + 1].second.value !is IDD)
                    throw SemanticError("StepInterpolation function needs all y-values to be of the same type (IDD or AADD)")
            }
        }
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> Quantity(model.builder.Reals, "?")
            is IDD -> Quantity(model.builder.Integers)
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
        for (element in points)
            upQuantity = x.ge(element.first).bdd().ite(element.second, upQuantity).clone()
    }

    override fun evalDown() {
        if (getParam(1).isReal) {
            var startingPoint = Double.POSITIVE_INFINITY
            var endingPosition = Double.NEGATIVE_INFINITY
            for (i in 0 until points.size) {
                if (downQuantity.contains(points[i].second)) { // only discrete solutions possible
                    startingPoint = min(points[i].first.getMinAsDouble(), startingPoint)
                    endingPosition = max(if (i != points.size - 1) points[i + 1].first.aadd().max else getParam(0).upQuantity.getMaxAsDouble(), endingPosition)
                }
            }
            if(startingPoint <= endingPosition) //resulting values found
                getParam(0).downQuantity = VectorQuantity(model.builder.real(startingPoint..endingPosition), getParam(0).downQuantity.unit, getParam(0).downQuantity.unitSpec)
            else
                getParam(0).downQuantity = VectorQuantity(model.builder.Empty, getParam(0).downQuantity.unit, getParam(0).downQuantity.unitSpec)
        } else if (getParam(1).isInt) {
            var startingPoint = Long.MAX_VALUE
            var endingPosition = Long.MIN_VALUE
            for (i in 0 until points.size) {
                if (downQuantity.contains(points[i].second)) { // only discrete solutions possible
                    startingPoint = min(points[i].first.idd().min, startingPoint)
                    endingPosition = max(if (i != points.size - 1) points[i + 1].first.idd().max else getParam(0).upQuantity.idd().max, endingPosition)
                }
            }
            if(startingPoint <= endingPosition) //resulting values found
                getParam(0).downQuantity = VectorQuantity(model.builder.integer(startingPoint..endingPosition))
            else
                getParam(0).downQuantity = VectorQuantity(model.builder.EmptyIntegerRange)
        }
    }

    override fun clone(): AstStepInterpolation {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstStepInterpolation(model, parClone)
    }
}
