package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.BDD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.dd.StrDD

/**
 * Normalize a vector to length 1
 */
internal class AstVectorSize(model: Session, args: ArrayList<AstNode>) : AstFunction("size", model, 1, args) {
    init {
        if (args.size !in 1..1)
            throw SemanticError("Vector length expects 1 parameter of type Vector")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals.All), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers.All))
            is StrDD -> VectorQuantity(mutableListOf(model.builder.Strings.All))
            is BDD -> VectorQuantity(mutableListOf(model.builder.Bool.All))
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.size()
    }

    override fun evalDown() {
        //Nothing to do
    }


    override fun <T> runDepthFirst(block: AstNode.() -> T): T {
        for (p in parameters) p.runDepthFirst(block)
        return block()
    }

    override fun clone() = AstVectorSize(model, cloneParameters())
}
