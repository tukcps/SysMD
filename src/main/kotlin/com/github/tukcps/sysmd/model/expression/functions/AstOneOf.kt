package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session


/**
 * The function oneOf maps a range to an integer or real.
 * TODO: Add KerML with respective constraints.
 */
class AstOneOf(model: Session, args: ArrayList<AstNode>) :
    AstFunction("oneOf", model, 1, args) {

    init {
        if (parameters.size != 1)
            throw SemanticError("Real function expects one parameter of type Range or Number")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is IDD  -> VectorQuantity(model.builder.Integers, Unit(), "")
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals), "")
            else -> throw SemanticError("oneOf function parameter must be of type Real, Integer, or Subtype thereof")
        }
        downQuantity = upQuantity.clone()
        require(downQuantity.values[0] is AADD)
    }

    override fun evalUp() {}

    override fun evalDown() {}

    override fun clone(): AstOneOf {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstOneOf(model, parClone)
    }
}
