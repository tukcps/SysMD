package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session

/**
 * Predefined functions: cosine
 */
internal class AstCos(model: Session, args: ArrayList<AstNode>) :
    AstFunction("cos", model, 1, args) {

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals), "?")
            else -> throw SemanticError("Cos function must have a parameter of type Real")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.cos()
    }

    override fun evalDown() {
        if( getParam(0).upQuantity.getMinAsDouble()>0.0 &&  getParam(0).upQuantity.getMaxAsDouble()<2.0*Math.PI)
            getParam(0).downQuantity = downQuantity.arccos()
    }

    override fun clone(): AstCos {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstCos(model, parClone)
    }
}
