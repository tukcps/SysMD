package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD

/**
 * Predefined functions: ln, natural logarithm.
 */
internal class AstLn(model: Session, args: ArrayList<AstNode>) :
    AstFunction("ln", model, 1, args) {

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers.All))
            else -> throw SemanticError("ln function must have one parameter of type Real")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.ln()
    }

    override fun evalDown() {
        getParam(0).downQuantity = getParam(0).downQuantity.constrain(downQuantity.exp())
    }

    override fun clone() = AstLn(model, cloneParameters())
}
