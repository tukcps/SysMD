package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session

/**
 * Normalize a vector to length 1
 */
internal class AstQuantityOfVectorAtPosition(model: Session, args: ArrayList<AstNode>) : AstFunction("quantityOfVectorAtPosition", model, 2, args) {
    init {
        if (args.size !in 2..2)
            throw SemanticError("AstQuantityOfVectorAtPosition expects 2 parameters of type Real or Integer Vector and IntegerRange")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers))
            else -> throw SemanticError("AstQuantityOfVectorAtPosition must have a Real or Integer argument")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.getQuantityAtPosition(getParam(1).upQuantity.value.asIdd().getRange())
    }

    override fun evalDown() {

    }


    override fun <T> runDepthFirst(block: AstNode.() -> T): T {
        for (p in parameters) p.runDepthFirst(block)
        return block()
    }

    override fun clone(): AstQuantityOfVectorAtPosition {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstQuantityOfVectorAtPosition(model, parClone)
    }
}
