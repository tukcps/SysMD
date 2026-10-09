package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD

/**
 * Predefined functions: inverseSqr (multi-valued inverse of square, returns both branches)
 */
internal class AstInverseSqr(model: Session, args: ArrayList<AstNode>) : AstFunction("inverseSqr", model, 1, args) {
    init {
        if (args.size !in 1..1)
            throw SemanticError("InverseSqr function expects one parameter of type Real or Integer")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers.All))
            else -> throw SemanticError("InverseSqr function must have a Real or Integer parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = (getParam(0).upQuantity.inverseSqr())
    }

    override fun evalDown() {
        getParam(0).downQuantity = getParam(0).downQuantity.constrain(downQuantity.sqr())
    }

    override fun <T> runDepthFirst(block: AstNode.() -> T): T {
        for (p in parameters) p.runDepthFirst(block)
        return block()
    }

    override fun clone() = AstInverseSqr(model, cloneParameters())
}
