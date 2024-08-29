package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.ite
import com.github.tukcps.sysmd.services.session.Session

/**
 * Predefined functions: step
 */
internal class AstStep(model: Session, args: ArrayList<AstNode>) :
    AstFunction("step", model, 5, args) {

    private val numberOfParameters = parameters.size
    private val points = mutableListOf<Pair<Quantity, Quantity>>()

    init {
        if (numberOfParameters % 2 != 1 && numberOfParameters >= 3)
            throw SemanticError("Step function needs an even number of Real or Integer parameters")

        for (i in 1..args.indices.last step 2)
            points.add(Pair(getParam(i).upQuantity.asQuantity(), getParam(i + 1).upQuantity.asQuantity()))
    }

    override fun initialize() {
        if (parameters.any { it.upQuantity.values.size != 1 })
            throw VectorDimensionError("Step is not possible with Vectors of size > 1")
        for (i in 1 until numberOfParameters step 2) {
            if (getParam(i).upQuantity.values[0] !is AADD && getParam(i).upQuantity.values[0] !is IDD ||
                getParam(i + 1).upQuantity.values[0] !is AADD && getParam(i + 1).upQuantity.values[0] !is IDD
            )
                throw SemanticError("Step only takes Real and Integer parameters")
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
            upQuantity = x.ge(element.first).bdd().ite(element.second, upQuantity.asQuantity())
    }

    override fun evalDown() {
        if (downQuantity.values[0] is AADD) {
            getParam(0).downQuantity = Quantity(model.builder.Reals, downQuantity.unit) // if no other solution found
            for (i in 0 until points.size) {
                if (points[i].second.value.asAadd().contains(downQuantity.aadd().min) ||
                    points[i].second.value.asAadd().contains(downQuantity.aadd().max)
                ) { // only discrete solutions possible
                    val startingPoint = if (i != 0) points[i].first.getMinAsDouble() else model.builder.Reals.min
                    val endingPoint =
                        if (i != points.size - 1) points[i + 1].first.getMaxAsDouble() else model.builder.Reals.max
                    getParam(0).downQuantity =
                        Quantity(model.builder.range(startingPoint, endingPoint), downQuantity.unit)
                }
            }
        }
        if (downQuantity.values[0] is IDD) {
            getParam(0).downQuantity = Quantity(model.builder.Integers) // if no other solution found
            for (i in 0 until points.size) {
                if (points[i].second.value.asIdd().contains(downQuantity.idd().min) ||
                    points[i].second.value.asIdd().contains(downQuantity.idd().max)
                ) { // only discrete solutions possible
                    val startingPoint = if (i != 0) points[i].first.idd().min else model.builder.Integers.min
                    val endingPoint =
                        if (i != points.size - 1) points[i + 1].first.idd().max else model.builder.Integers.max
                    getParam(0).downQuantity =
                        Quantity(model.builder.range(startingPoint, endingPoint), downQuantity.unit)
                }
            }
        }
    }

    override fun toExpressionString() = "step(${getParam(0).toExpressionString()})"

    override fun clone(): AstStep {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstStep(model, parClone)
    }
}