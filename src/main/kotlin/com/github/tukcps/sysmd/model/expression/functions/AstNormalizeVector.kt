package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.Real

/**
 * Normalize a vector to length 1
 */
internal class AstNormalizeVector(model: Session, args: ArrayList<AstNode>) : AstFunction("norm", model, 1, args) {
    init {
        if (args.size !in 1..1)
            throw SemanticError("Normalize expects 1 parameter of type Real  Vector")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is Real -> VectorQuantity(mutableListOf(model.builder.Reals.All), "?")
            else -> throw SemanticError("Normalize must have a Real argument")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = (getParam(0).upQuantity.norm())
    }

    override fun evalDown() {
        //Nothing to do, all value ranges are possible
    }


    override fun <T> runDepthFirst(block: AstNode.() -> T): T {
        for (p in parameters) p.runDepthFirst(block)
        return block()
    }

    override fun clone() = AstNormalizeVector(model, cloneParameters())
}
