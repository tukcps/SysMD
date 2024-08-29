package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.DD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.min
import com.github.tukcps.sysmd.services.session.Session

/**
 * Predefined functions: min
 * Calculates the minimum of two parameters or the minimum of one value
 */
internal class AstMin(model: Session, args: ArrayList<AstNode>) :
    AstFunction("min", model, 1, args) {

    /**
     * Checks the type of both parameters.
     */
    override fun initialize() {
        when(parameters.size){
            1 -> {
                if ((getParam(0).upQuantity.values[0] !is AADD) && (getParam(0).upQuantity.values[0] !is IDD))
                    throw SemanticError("Min function only takes Real or Integer parameters")
            }
            2 -> {
                if ((getParam(0).upQuantity.values[0] !is AADD) && (getParam(0).upQuantity.values[0] !is IDD))
                    throw SemanticError("Min function only takes Real or Integer parameters")
                if ((getParam(1).upQuantity.values[0] !is AADD) && (getParam(1).upQuantity.values[0] !is IDD))
                    throw SemanticError("Min function only takes Real or Integer parameters")
                if (getParam(0).upQuantity.values[0] is AADD) {
                    upQuantity = VectorQuantity(mutableListOf(model.builder.Reals), "?")
                    if (getParam(1).upQuantity.values[0] !is AADD)
                        throw SemanticError("Min function requires both parameters of same type")
                }
                if (getParam(0).upQuantity.values[0] is IDD) {
                    upQuantity = VectorQuantity(mutableListOf(model.builder.Integers))
                    if (getParam(1).upQuantity.values[0] !is IDD)
                        throw SemanticError("Min function requires both parameters of same type")
                }
            }
            else -> throw SemanticError("Min function requires one or two parameters")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    /**
     * Implementation of min(a, b)
     */
    override fun evalUp() {
        when (parameters.size) {
            1 -> {
                val resultingValues = mutableListOf<DD>()
                for (value in getParam(0).upQuantity.values) {
                    when (getParam(0).upQuantity.values[0]) {
                        is AADD -> resultingValues.add(model.builder.scalar((value as AADD).getRange().min))
                        is IDD -> resultingValues.add(model.builder.scalar((value as IDD).getRange().min))
                        else -> {}//not possible
                    }
                }
                upQuantity =
                    VectorQuantity(resultingValues, getParam(0).upQuantity.unit, getParam(0).upQuantity.unitSpec)
            }
            2 -> upQuantity = min(getParam(0).upQuantity, getParam(1).upQuantity)
        }
    }

    override fun evalDown() {
        when (parameters.size) {
            1 -> {
                val param1Down = getParam(0).downQuantity
                val results = mutableListOf<DD>()
                downQuantity.values.indices.forEach {
                    when (downQuantity.values[0]) {
                        is AADD -> results.add(
                            model.builder.range(
                                (param1Down.values[it] as AADD).getRange().min,
                                Double.POSITIVE_INFINITY
                            )
                        )

                        is IDD -> results.add(
                            model.builder.range(
                                (param1Down.values[it] as IDD).getRange().min,
                                Long.MAX_VALUE
                            )
                        )

                        else -> {
                            throw SemanticError("Min only possible for IDD and AADD.")
                        }
                    }
                }
                getParam(0).downQuantity =
                    getParam(0).downQuantity.constrain(VectorQuantity(results, param1Down.unit, param1Down.unitSpec))
            }

            2 -> {
                val param1Down = getParam(0).upQuantity
                val param2Down = getParam(1).upQuantity
                val resultsParam1 = mutableListOf<DD>()
                val resultsParam2 = mutableListOf<DD>()
                downQuantity.values.indices.forEach {
                    when (downQuantity.values[0]) {
                        is AADD -> {
                            resultsParam1.add(
                                param2Down.gt(downQuantity).bdd().ite(
                                    downQuantity.values[it] as AADD,
                                    model.builder.range(
                                        (downQuantity.values[it] as AADD).getRange().min,
                                        (param1Down.values[it] as AADD).getRange().max
                                    )
                                )
                            )
                            resultsParam2.add(
                                param1Down.gt(downQuantity).bdd().ite(
                                    downQuantity.values[it] as AADD,
                                    model.builder.range(
                                        (downQuantity.values[it] as AADD).getRange().min,
                                        (param2Down.values[it] as AADD).getRange().max
                                    )
                                )
                            )
                        }

                        is IDD -> {
                            resultsParam1.add(
                                param2Down.gt(downQuantity).bdd().ite(
                                    downQuantity.values[it] as IDD,
                                    model.builder.range(
                                        (downQuantity.values[it] as IDD).getRange().min,
                                        (param1Down.values[it] as IDD).getRange().max
                                    )
                                )
                            )
                            resultsParam2.add(
                                param1Down.gt(downQuantity).bdd().ite(
                                    downQuantity.values[it] as IDD,
                                    model.builder.range(
                                        (downQuantity.values[it] as IDD).getRange().min,
                                        (param2Down.values[it] as IDD).getRange().max
                                    )
                                )
                            )
                        }

                        else -> {
                            throw SemanticError("Min only possible for IDD and AADD")
                        }
                    }
                }
                getParam(0).downQuantity =
                    getParam(0).downQuantity.constrain(VectorQuantity(resultsParam1, param1Down.unit))
                getParam(1).downQuantity =
                    getParam(1).downQuantity.constrain(VectorQuantity(resultsParam2, param2Down.unit))
            }
        }
    }

    override fun toExpressionString() = "min(${getParam(0).toExpressionString()}, ${getParam(1).toExpressionString()})"

    override fun clone(): AstMin {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstMin(model, parClone)
    }
}
