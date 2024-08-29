package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.DD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.max
import com.github.tukcps.sysmd.services.session.Session

/**
 * Predefined functions: max
 * Calculates the maximum of two parameters or the maximum of one value
 */
internal class AstMax(model: Session, args: ArrayList<AstNode>) :
    AstFunction("max", model, 1, args) {

    /**
     * Checks the type of both parameters.
     */
    override fun initialize() {
        when(parameters.size){
            1 -> {
                if ((getParam(0).upQuantity.values[0] !is AADD) && (getParam(0).upQuantity.values[0] !is IDD))
                    throw SemanticError("Max function only takes Real or Integer parameter")
            }
            2 -> {
                if ((getParam(0).upQuantity.values[0] !is AADD) && (getParam(0).upQuantity.values[0] !is IDD))
                    throw SemanticError("Max function only takes Real or Integer parameters")
                if ((getParam(1).upQuantity.values[0] !is AADD) && (getParam(1).upQuantity.values[0] !is IDD))
                    throw SemanticError("Max function only takes Real or Integer parameters")
                if (getParam(0).upQuantity.values[0] is AADD) {
                    upQuantity = VectorQuantity(mutableListOf(model.builder.Reals), "?")
                    if (getParam(1).upQuantity.values[0] !is AADD)
                        throw SemanticError("Max function requires both parameters of same type")
                }
                if (getParam(0).upQuantity.values[0] is IDD) {
                    upQuantity = VectorQuantity(mutableListOf(model.builder.Integers))
                    if (getParam(1).upQuantity.values[0] !is IDD)
                        throw SemanticError("Max function requires both parameters of same type")
                }
            }
            else -> throw SemanticError("Max function requires one or two parameters")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    /**
     * Implementation of max(a, b)
     */
    override fun evalUp() {
        when (parameters.size) {
            1 -> {
                val resultingValues = mutableListOf<DD>()
                for (value in getParam(0).upQuantity.values) {
                    when (getParam(0).upQuantity.values[0]) {
                        is AADD -> resultingValues.add(model.builder.scalar((value as AADD).getRange().max))
                        is IDD -> resultingValues.add(model.builder.scalar((value as IDD).getRange().max))
                        else -> {}//not possible
                    }
                }
                upQuantity = VectorQuantity(resultingValues, getParam(0).upQuantity.unit, getParam(0).upQuantity.unitSpec)
            }
            2 -> upQuantity = max(getParam(0).upQuantity, getParam(1).upQuantity)
        }
    }

    override fun evalDown() {
        when (parameters.size) {
            1 -> {
                val param1Down = getParam(0).downQuantity
                val results = mutableListOf<DD>()
                downQuantity.values.indices.forEach {
                    when (downQuantity.values[0]) {
                        is AADD -> results.add(model.builder.range(Double.NEGATIVE_INFINITY,(param1Down.values[it] as AADD).getRange().max))
                        is IDD -> results.add(model.builder.range(Long.MIN_VALUE,(param1Down.values[it] as IDD).getRange().max))
                        else -> {
                            throw SemanticError("Max only possible for IDD and AADD")
                        }
                    }
                }
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(VectorQuantity(results, param1Down.unit, param1Down.unitSpec))
            }
            2 -> {
                val param1Down = getParam(0).downQuantity
                val param2Down = getParam(1).downQuantity
                val resultsParam1 = mutableListOf<DD>()
                val resultsParam2 = mutableListOf<DD>()
                downQuantity.values.indices.forEach {
                    when (downQuantity.values[0]) {
                        is AADD -> {
                            resultsParam1.add(
                                param2Down.lt(downQuantity).bdd().ite(
                                    downQuantity.values[it] as AADD,
                                    model.builder.range(
                                        (param1Down.values[it] as AADD).getRange().min,
                                        (downQuantity.values[it] as AADD).getRange().max
                                    )
                                )
                            )
                            resultsParam2.add(
                                param1Down.lt(downQuantity).bdd().ite(
                                    downQuantity.values[it] as AADD,
                                    model.builder.range(
                                        (param2Down.values[it] as AADD).getRange().min,
                                        (downQuantity.values[it] as AADD).getRange().max
                                    )
                                )
                            )
                        }

                        is IDD -> {
                            resultsParam1.add(
                                param2Down.lt(downQuantity).bdd().ite(
                                    downQuantity.values[it] as IDD,
                                    model.builder.range(
                                        (param1Down.values[it] as IDD).getRange().min,
                                        (downQuantity.values[it] as IDD).getRange().max
                                    )
                                )
                            )
                            resultsParam2.add(
                                param1Down.lt(downQuantity).bdd().ite(
                                    downQuantity.values[it] as IDD,
                                    model.builder.range(
                                        (param2Down.values[it] as IDD).getRange().min,
                                        (downQuantity.values[it] as IDD).getRange().max
                                    )
                                )
                            )
                        }

                        else -> {
                            throw SemanticError("Max only possible for IDD and AADD")
                        }
                    }
                }
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(VectorQuantity(resultsParam1, param1Down.unit,param1Down.unitSpec))
                getParam(1).downQuantity = getParam(1).downQuantity.constrain(VectorQuantity(resultsParam2, param1Down.unit,param1Down.unitSpec))
            }
        }
    }

    override fun toExpressionString() = "max(${getParam(0).toExpressionString()})"

    override fun clone(): AstMax {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstMax(model, parClone)
    }
}
