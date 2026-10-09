package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD

/**
 * Normalize a vector to length 1
 */
internal class AstVectorAngle(model: Session, args: ArrayList<AstNode>) : AstFunction("angle", model, 2, args) {
    init {
        if (args.size !in 2..2)
            throw SemanticError("Angle function expects two parameters of type Real or Integer Vector")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals.All), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers.All))
            else -> throw SemanticError("Parameters of Angle function must be Reals or Integers")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = (getParam(0).upQuantity.angle(getParam(1).upQuantity))
    }

    override fun evalDown() {
        //Nothing to do, all value ranges are possible
    }


    override fun <T> runDepthFirst(block: AstNode.() -> T): T {
        for (p in parameters) p.runDepthFirst(block)
        return block()
    }

    override fun clone() = AstVectorAngle(model, cloneParameters())
}
