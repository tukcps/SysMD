package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session


/**
 * The function oneOf maps a range to an integer or real.
 * TODO: Add KerML with respective constraints.
 */
class AstAnyOf(model: Session, args: ArrayList<AstNode>) :
    AstFunction("anyOf", model, 1, args) {

    init {
        if (parameters.size != 1)
            throw SemanticError("Real function expects one parameter of type Range or Number")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is IDD  -> VectorQuantity(model.builder.Integers, Unit(), "")
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals), "")
            else -> throw SemanticError("anyOf function parameter must be of type Real, Integer, or Subtype thereof")
        }
        downQuantity = upQuantity.clone()
        require(downQuantity.values[0] is AADD)
    }

    override fun evalUp() {}

    override fun evalDown() {}

    override fun toExpressionString(): String {
        return "oneOf(${getParam(0).toExpressionString()})"
    }

    override fun clone(): AstAnyOf {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstAnyOf(model, parClone)
    }
}
