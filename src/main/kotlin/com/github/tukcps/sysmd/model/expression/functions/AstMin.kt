package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.min
import com.github.tukcps.sysmd.services.session.Session

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
                    upQuantity = VectorQuantity(mutableListOf(model.builder.Reals), "?")
                    for(parameter in parameters){
                        if (parameter.upQuantity.value !is AADD)
                            throw SemanticError("Min function requires all parameters of same type (AADD)")
                        if(parameter.upQuantity.values.size != 1)
                            throw SemanticError("Min function does not allow vectors")
                    }
                }
                if (getParam(0).upQuantity.values[0] is IDD) {
                    upQuantity = VectorQuantity(mutableListOf(model.builder.Integers))
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
                        is AADD -> resultingValues.add(model.builder.real((value as AADD).getRange().min))
                        is IDD -> resultingValues.add(model.builder.integer((value as IDD).getRange().min))
                        else -> {}//not possible
                    }
                }
                upQuantity = VectorQuantity(resultingValues, getParam(0).upQuantity.unit, getParam(0).upQuantity.unitSpec)
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
                        is AADD -> results.add(model.builder.real((param1Down.values[it] as AADD).getRange().min..Double.POSITIVE_INFINITY))
                        is IDD -> results.add(model.builder.integer((param1Down.values[it] as IDD).getRange().min..Long.MAX_VALUE))
                        else -> {
                            throw SemanticError("Min only possible for IDD and AADD.")
                        }
                    }
                }
                getParam(0).downQuantity =
                    getParam(0).downQuantity.constrain(VectorQuantity(results, param1Down.unit, param1Down.unitSpec))
            }

            else -> {
                when (downQuantity.values[0]) {
                    is AADD -> {
                        //if the downQuantity fits only in one of the downParams, this downParam must have the value of the downQuantity
                        //store those positions in a list
                        val includedPositions = mutableListOf<Int>()
                        parameters.forEach{
                            if(it.downQuantity.getMinAsDouble() < downQuantity.getMinAsDouble())
                                it.downQuantity = Quantity(model.builder.real(downQuantity.getMinAsDouble() .. it.downQuantity.getMaxAsDouble()),it.downQuantity.unit,it.downQuantity.unitSpec)
                            if(it.downQuantity.value.asAadd().contains(downQuantity.value.asAadd())){
                                includedPositions.add(parameters.indexOf(it))
                            }
                        }
                        if(includedPositions.size == 1){
                            //if only one downParam fits the downQuantity, this downParam must have the value of the downQuantity
                            parameters[includedPositions[0]].downQuantity = downQuantity
                        } else if (includedPositions.size==2) {
                            //bigger value than downQuantity Possible
                            includedPositions.forEach {
                                parameters[it].downQuantity =
                                    Quantity(model.builder.real(downQuantity.value.asAadd().min..parameters[it].upQuantity.getMaxAsDouble()),downQuantity.unit,downQuantity.unitSpec)
                            }
                        }
                    }

                    is IDD -> {
                        //if the downQuantity fits only in one of the downParams, this downParam must have the value of the downQuantity
                        //store those positions in a list
                        val includedPositions = mutableListOf<Int>()
                        parameters.forEach{
                            if(it.downQuantity.value.asIdd().min < downQuantity.value.asIdd().min)
                                it.downQuantity = Quantity(model.builder.integer(downQuantity.value.asIdd()))
                            if(it.downQuantity.value.asIdd().contains(downQuantity.value.asIdd())){
                                includedPositions.add(parameters.indexOf(it))
                            }
                        }
                        if(includedPositions.size == 1){
                            //if only one downParam fits the downQuantity, this downParam must have the value of the downQuantity
                            parameters[includedPositions[0]].downQuantity = downQuantity
                        } else if (includedPositions.size==2) {
                            //bigger value than downQuantity Possible
                            includedPositions.forEach {
                                parameters[it].downQuantity =
                                    Quantity(model.builder.integer(downQuantity.value.asIdd().min..parameters[it].upQuantity.idd().max))
                            }
                        }
                    }

                    else -> {
                        throw SemanticError("Max only possible for IDD and AADD")
                    }
                }
            }
        }
    }

    override fun clone(): AstMin {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstMin(model, parClone)
    }
}
