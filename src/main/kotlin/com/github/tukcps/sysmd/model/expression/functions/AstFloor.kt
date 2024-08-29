package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session

/**
 * Predefined functions: floor
 * Computes floor function
 */
internal class AstFloor(model: Session, args: ArrayList<AstNode>) :
    AstFunction("floor", model, 1, args) {

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers))
            else -> throw SemanticError("Floor function only takes a Real or Integer parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> getParam(0).upQuantity.floor()
            is IDD -> {
                val results = mutableListOf<IDD>()
                getParam(0).upQuantity.values.forEach {
                    val resultMin = it.asIdd().min
                    var resultMax = it.asIdd().max
                    if (resultMin < resultMax)
                        resultMax = IntegerRange().minusOverflowDetection(
                            resultMax,
                            1
                        ) //only reduce maximum border by one if not infinity
                    results.add(model.builder.range(resultMin, resultMax))
                }
                VectorQuantity(results)
            }

            else -> throw SemanticError("Floor function only takes a Real or Integer parameter")
        }
    }

    override fun evalDown() {
        getParam(0).downQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> {
                val results = mutableListOf<AADD>()
                downQuantity.values.forEach {
                    results.add((it as AADD).invFloor())
                }
                VectorQuantity(results, downQuantity.unit)
            }

            is IDD -> {
                val results = mutableListOf<IDD>()
                // avoid overflow during -1 calculation
                downQuantity.values.forEach {
                    results.add(
                        model.builder.range(
                            it.asIdd().min,
                            IntegerRange().plusOverflowDetection(it.asIdd().max, 1)
                        )
                    )
                }
                VectorQuantity(results)
            }

            else -> throw SemanticError("Floor function only takes a Real or Integer parameter")
        }
    }

    override fun toExpressionString() = "floor(${getParam(0).toExpressionString()})"

    override fun clone(): AstFloor {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstFloor(model, parClone)
    }
}
