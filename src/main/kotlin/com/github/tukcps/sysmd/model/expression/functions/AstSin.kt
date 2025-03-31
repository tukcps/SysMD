package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session

/**
 * Predefined functions: sine
 */
internal class AstSin(model: Session, args: ArrayList<AstNode>) :
    AstFunction("sin", model, 1, args) {

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals), "?")
            else -> throw SemanticError("Sin function must have a parameter of type Real")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.sin()
    }

    override fun evalDown() {
        getParam(0).downQuantity = downQuantity.arcsin()
    }

    override fun clone(): AstSin {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstSin(model, parClone)
    }
}
