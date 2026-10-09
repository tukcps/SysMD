package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.*
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.bounds.LongBound

/**
 * Predefined functions: max
 * 1 parameter: returns upper bound of AADD or IDD value
 * 2 or more parameters: returns maximum of all parameters (no vectors allowed)
 */
internal class AstMax(model: Session, args: ArrayList<AstNode>) :
    AstFunction("max", model, 1, args) {

    /**
     * Checks the type of both parameters.
     */
    override fun initialize() {
        when {
            // max(vector) -- 1 parameter that must be a Vector
            parameters.size == 1 -> {
                if ((getParam(0).upQuantity.values[0] !is AADD) && (getParam(0).upQuantity.values[0] !is IDD))
                    throw SemanticError("Max is only defined for Integer and Real")
                upQuantity = VectorQuantity.fromCanonical(getParam(0).upQuantity.values[0].builder.Reals.All, Unit("?"), "?")
            }
            // max(s1, s2, ...) -- 2 or more scalar parameters
            parameters.size >= 2 -> {
                if ((getParam(0).upQuantity.values[0] !is AADD) && (getParam(0).upQuantity.values[0] !is IDD))
                    throw SemanticError("Max is only defined for Integer and Real")
                if (getParam(0).upQuantity.values[0] is AADD) {
                    upQuantity = VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
                    for(parameter in parameters){
                        if (parameter.upQuantity.values.any { it !is AADD })
                            throw SemanticError("Mix of Real and Integer in max function is not supported")
                        if(parameter.upQuantity.values.size != 1)
                            throw VectorDimensionError("max(s1, s2, ...) expects only scalar parameters, but found vector parameter with size ${parameter.upQuantity.values.size}")
                    }
                }
                if (getParam(0).upQuantity.values[0] is IDD) {
                    upQuantity = VectorQuantity(mutableListOf(model.builder.Integers.All))
                    for(parameter in parameters){
                        if (parameter.upQuantity.values.any { it !is IDD })
                            throw SemanticError("Mix of Real and Integer in max function is not supported")
                        if(parameter.upQuantity.values.size != 1)
                            throw VectorDimensionError("max(s1, s2, ...) expects only scalar parameters, but found vector parameter with size ${parameter.upQuantity.values.size}")
                    }
                }
            }

            else -> throw SemanticError("Function max expects at least 1 parameter.")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    /**
     * Implementation of max(a) and max(a,b,...) functions
     */
    override fun evalUp() {
        when (parameters.size) {
            1 -> {
                val resultingValues = mutableListOf<DD<*>>()
                for (value in getParam(0).upQuantity.values) {
                    when (getParam(0).upQuantity.values[0]) {
                        is AADD -> resultingValues.add(model.builder.real((value as AADD).getRange().max.toDouble()))
                        is IDD -> resultingValues.add(model.builder.integer((value as IDD).getRange().max))
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
                var result =  max(getParam(0).upQuantity, getParam(1).upQuantity)
                if (parameters.size > 2) {
                    val remainingValues = parameters.subList(2, parameters.size)
                    remainingValues.forEach {
                        result = max(result, it.upQuantity)
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
                        is AADD -> results.add(model.builder.real(Double.NEGATIVE_INFINITY..(downQuantity.values[it] as AADD).getRange().max.toDouble()))
                        is IDD -> results.add(model.builder.integer(LongBound.NegativeInfinity..(downQuantity.values[it] as IDD).getRange().max))
                        else -> { throw SemanticError("Max only possible for IDD and AADD") }
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
                // Every parameter is <= max. If only one parameter can still reach the lower bound of the result,
                // that parameter must lie within the result's range. Existing narrowing is kept via constrain.
                when (downQuantity.values[0]) {
                    is AADD -> {
                        val lo = downQuantity.aadd().getRange().min.toDouble()
                        val hi = downQuantity.aadd().getRange().max.toDouble()
                        val candidates = parameters.filter { it.downQuantity.getMaxAsDouble() >= lo }
                        for (p in parameters) {
                            val low = if (candidates.size == 1 && p === candidates[0]) lo else Double.NEGATIVE_INFINITY
                            p.downQuantity = p.downQuantity.constrain(
                                VectorQuantity.fromCanonical(model.builder.real(low..hi), p.downQuantity.unit, p.downQuantity.unitSpec, p.downQuantity.userWantedUnitSpec)
                            )
                        }
                    }
                    is IDD -> {
                        val lo = downQuantity.idd().getRange().min
                        val hi = downQuantity.idd().getRange().max
                        val candidates = parameters.filter { it.downQuantity.idd().getRange().max >= lo }
                        for (p in parameters) {
                            val low = if (candidates.size == 1 && p === candidates[0]) lo else LongBound.NegativeInfinity
                            p.downQuantity = p.downQuantity.constrain(VectorQuantity(model.builder.integer(low..hi)))
                        }
                    }
                    else -> {
                        throw SemanticError("Max only possible for IDD and AADD")
                    }
                }
            }
        }
    }

    override fun clone() = AstMax(model, cloneParameters())
}
