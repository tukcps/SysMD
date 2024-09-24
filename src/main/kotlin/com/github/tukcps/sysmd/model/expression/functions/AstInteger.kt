package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import kotlin.math.ceil
import kotlin.math.floor


/**
 * The function ToInteger : Real -> Integer converts a Real to an Integer.
 */
class AstInteger(model: Session, args: ArrayList<AstNode>) :
    AstFunction("ToInteger", model, 1, args) {

    init {
        if (parameters.size != 1)
            throw SemanticError("Integer function expects exactly one parameter")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Integers))
            else -> throw SemanticError("Parameter of Integer function must be of type Real")
        }
        evalUp()
        downQuantity = upQuantity.clone()
        require(downQuantity.values[0] is IDD)
    }


    @Throws(SemanticError::class)
    override fun evalUp() {
        val results = mutableListOf<IDD>()
        getParam(0).aadds.forEach {
            val min = floor(it.min).toLong()
            val max = ceil(it.max).toLong()
            results.add(model.builder.range(min, max))
        }
        upQuantity = VectorQuantity(results)

    }

    override fun evalDown() {
        val results = mutableListOf<AADD>()
        downQuantity.idds().forEach {
            val min = if (it.min == Long.MIN_VALUE) Double.NEGATIVE_INFINITY else it.min.toDouble()
            val max = if (it.max == Long.MAX_VALUE) Double.POSITIVE_INFINITY else it.max.toDouble()
            results.add(model.builder.range(min, max))
        }
        getParam(0).downQuantity = VectorQuantity(results, upQuantity.unit, upQuantity.unitSpec)
    }


    override fun toExpressionString(): String {
        return "Integer(${getParam(0).toExpressionString()})"
    }

    override fun clone(): AstInteger {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstInteger(model, parClone)
    }
}
