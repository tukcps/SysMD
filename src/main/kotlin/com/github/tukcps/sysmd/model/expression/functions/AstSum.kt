package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session

/**
 * Calculates Sum of all values of a vector
 */
internal class AstSum(
    private val namespace: Namespace,
    model: Session,
    args: ArrayList<AstNode>
) : AstFunction("sum", model, 1, args) {
    private var value: AstNode = getParam(0)

    override fun initialize() {
        // Check that 1st parameter is int or real
        if (parameters.size!=1)
            throw SemanticError("Sum function expected exactly one parameter, got: ${parameters.size}")
        upQuantity = when (value.upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers))
            else -> throw SemanticError("Sum must have a Real or Integer parameter")
        }
        downQuantity = upQuantity.clone()
    }


    override fun evalUp() {
        when (value.upQuantity.value) {
            is AADD -> {
                var sum = model.builder.real(0.0)
                value.upQuantity.values.forEach { sum += it.asAadd() }
                upQuantity = Quantity(sum, value.upQuantity.unit, value.upQuantity.unitSpec)
            }

            is IDD -> {
                var sum = model.builder.integer(0)
                value.upQuantity.values.forEach { sum += it.asIdd() }
                upQuantity = Quantity(sum)
            }
            else -> {} //not possible, case already in initialize
        }
    }

    /**
     * Iterate through all values of the vector and calculate the down quantity of each element
     */
    override fun evalDown() {

        when (getParam(0).downQuantity.value) {
            is AADD -> {
                val resultingSum = downQuantity.value.asAadd()
                val resultingValues = mutableListOf<AADD>()
                getParam(0).downQuantity.values.indices.forEach {
                    var currentResult = resultingSum
                    getParam(0).downQuantity.values.indices.forEach {
                        iterator -> if(iterator != it)
                            currentResult -= getParam(0).downQuantity.values[iterator].asAadd()
                    }
                    resultingValues.add(currentResult)
                }
                getParam(0).downQuantity = VectorQuantity(resultingValues, getParam(0).downQuantity.unit, getParam(0).downQuantity.unitSpec)
            }
            is IDD -> {
                val resultingSum = downQuantity.value.asIdd()
                val resultingValues = mutableListOf<IDD>()
                getParam(0).downQuantity.values.indices.forEach {
                    var currentResult = resultingSum
                    getParam(0).downQuantity.values.indices.forEach {
                            iterator -> if(iterator != it)
                        currentResult -= getParam(0).downQuantity.values[iterator].asIdd()
                    }
                    resultingValues.add(currentResult)
                }
                getParam(0).downQuantity = VectorQuantity(resultingValues)
            }
            else -> {} //not possible, case already in initialize
        }
    }


    override fun <T> runDepthFirst(block: AstNode.() -> T): T {
        for (p in parameters) p.runDepthFirst(block)
        return block()
    }

    override fun clone(): AstSum {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstSum(namespace, model, parClone)
    }
}