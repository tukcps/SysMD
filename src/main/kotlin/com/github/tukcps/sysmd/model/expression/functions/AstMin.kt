package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.min
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.values.bounds.DoubleBound
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.bounds.LongBound

/**
 * Predefined functions: min
 * 1 parameter: returns upper bound of AADD or IDD value
 * 2 or more parameters: returns minimum of all parameters (no vectors allowed)
 */
internal class AstMin(model: Session, args: ArrayList<AstNode>) :
    AstFunction("min", model, 1, args) {

    /**
     * Checks the type of both parameters.
     */
    override fun initialize() {
        when(parameters.size){
            1 -> { // returns upper bound of AADD or IDD value
                if ((getParam(0).upQuantity.values[0] !is AADD) && (getParam(0).upQuantity.values[0] !is IDD))
                    throw SemanticError("Min function only takes Real or Integer parameter")
            }
            else -> { // returns maximum of all parameters
                if ((getParam(0).upQuantity.values[0] !is AADD) && (getParam(0).upQuantity.values[0] !is IDD))
                    throw SemanticError("Min function only takes Real or Integer parameters")
                if (getParam(0).upQuantity.values[0] is AADD) {
                    upQuantity = VectorQuantity(mutableListOf(model.builder.Reals.All), "?")
                    for(parameter in parameters){
                        if (parameter.upQuantity.value !is AADD)
                            throw SemanticError("Min function requires all parameters of same type (AADD)")
                        if(parameter.upQuantity.values.size != 1)
                            throw SemanticError("Min function does not allow vectors")
                    }
                }
                if (getParam(0).upQuantity.values[0] is IDD) {
                    upQuantity = VectorQuantity(mutableListOf(model.builder.Integers.All))
                    for(parameter in parameters){
                        if (parameter.upQuantity.value !is IDD)
                            throw SemanticError("Min function requires all parameters of same type (IDD)")
                        if(parameter.upQuantity.values.size != 1)
                            throw SemanticError("Min function does not allow vectors")
                    }
                }
            }
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
                val resultingValues = mutableListOf<DD<*>>()
                for (value in getParam(0).upQuantity.values) {
                    when (getParam(0).upQuantity.values[0]) {
                        is AADD -> resultingValues.add(model.builder.real((value as AADD).getRange().min..(value as AADD).getRange().min))
                        is IDD -> resultingValues.add(model.builder.integer((value as IDD).getRange().min))
                        else -> {}//not possible
                    }
                }
                upQuantity = if (getParam(0).upQuantity.values[0] is AADD) {
                    VectorQuantity.fromCanonical(resultingValues, getParam(0).upQuantity.unit, getParam(0).upQuantity.unitSpec, getParam(0).upQuantity.userWantedUnitSpec)
                } else {
                    VectorQuantity(resultingValues)
                }
            }
            else -> {
                var result =  min(getParam(0).upQuantity, getParam(1).upQuantity)
                if(parameters.size > 2){
                    val remainingValues = parameters.subList(2, parameters.size)
                    remainingValues.forEach {
                        result = min(result, it.upQuantity)
                    }
                }
                upQuantity = result
            }
        }
    }

    override fun evalDown() {
        when (parameters.size) {
            1 -> {
                val param1Down = getParam(0).downQuantity
                val results = mutableListOf<DD<*>>()
                downQuantity.values.indices.forEach {
                    when (downQuantity.values[0]) {
                        is AADD -> results.add(model.builder.real((downQuantity.values[it] as AADD).getRange().min..DoubleBound.PositiveInfinity))
                        is IDD -> results.add(model.builder.integer((downQuantity.values[it] as IDD).getRange().min..LongBound.PositiveInfinity))
                        else -> {
                            throw SemanticError("Min only possible for IDD and AADD.")
                        }
                    }
                }
                val newParam = if (downQuantity.values[0] is AADD) {
                    VectorQuantity.fromCanonical(results, param1Down.unit, param1Down.unitSpec, param1Down.userWantedUnitSpec)
                } else {
                    VectorQuantity(results)
                }
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(newParam)
            }

            else -> {
                // Every parameter is >= min. If only one parameter can still reach the upper bound of the result,
                // that parameter must lie within the result's range. Existing narrowing is kept via constrain.
                when (downQuantity.values[0]) {
                    is AADD -> {
                        val lo = downQuantity.aadd().getRange().min.toDouble()
                        val hi = downQuantity.aadd().getRange().max.toDouble()
                        val candidates = parameters.filter { it.downQuantity.getMinAsDouble() <= hi }
                        for (p in parameters) {
                            val high = if (candidates.size == 1 && p === candidates[0]) hi else Double.POSITIVE_INFINITY
                            p.downQuantity = p.downQuantity.constrain(
                                VectorQuantity.fromCanonical(model.builder.real(lo..high), p.downQuantity.unit, p.downQuantity.unitSpec, p.downQuantity.userWantedUnitSpec)
                            )
                        }
                    }
                    is IDD -> {
                        val lo = downQuantity.idd().getRange().min
                        val hi = downQuantity.idd().getRange().max
                        val candidates = parameters.filter { it.downQuantity.idd().getRange().min <= hi }
                        for (p in parameters) {
                            val high = if (candidates.size == 1 && p === candidates[0]) hi else LongBound.PositiveInfinity
                            p.downQuantity = p.downQuantity.constrain(VectorQuantity(model.builder.integer(lo..high)))
                        }
                    }
                    else -> {
                        throw SemanticError("Min only possible for IDD and AADD")
                    }
                }
            }
        }
    }

    override fun clone() = AstMin(model, cloneParameters())
}
