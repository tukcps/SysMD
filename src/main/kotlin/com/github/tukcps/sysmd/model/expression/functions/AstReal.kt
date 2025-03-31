package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import kotlin.math.ceil
import kotlin.math.floor


/**
 * The function Real : Int -> Real converts an integer to a real.
 */
class AstReal(model: Session, args: ArrayList<AstNode>) :
    AstFunction("Real", model, 1, args) {

    init {
        if (parameters.size != 1)
            throw SemanticError("Real function expects one parameter of type Integer")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is IDD -> VectorQuantity(mutableListOf(model.builder.Reals), "")
            else -> throw SemanticError("Real function parameter must be of type Integer")
        }
        evalUp()
        downQuantity = upQuantity.clone()
        require(downQuantity.values[0] is AADD)
    }

    override fun evalUp() {
        val results = mutableListOf<AADD>()
        getParam(0).idds.forEach {
            val min = if (it.min == Long.MIN_VALUE) Double.NEGATIVE_INFINITY else it.min.toDouble()
            val max = if (it.max == Long.MAX_VALUE) Double.POSITIVE_INFINITY else it.max.toDouble()
            results.add(model.builder.real(min..max))
        }
        upQuantity = VectorQuantity(results, getParam(0).upQuantity.unit, getParam(0).upQuantity.unitSpec)
    }

    override fun evalDown() {
        val results = mutableListOf<IDD>()
        downQuantity.aadds().forEach {
            val min = floor(it.min).toLong()
            val max = ceil(it.max).toLong()
            results.add(model.builder.integer(min..max))
        }
        getParam(0).downQuantity = VectorQuantity(results)
    }

    override fun clone(): AstReal {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstReal(model, parClone)
    }
}
