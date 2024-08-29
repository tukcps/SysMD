package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.session.Session

/**
 * Predefined functions: 2^x (two to the power of x)
 */
internal class AstPower2(model: Session, args: ArrayList<AstNode>) :
    AstFunction("power2", model, 1, args) {

    init {
        if (args.size !in 1..1)
            throw SemanticError("pow2 function expects one parameter of type Real")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> Quantity(model.builder.Reals, "")
            is IDD -> Quantity(model.builder.Integers)
            else -> throw SemanticError("pow2 function must have a Real or Integer parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.pow2()
    }

    override fun evalDown() {
        when (getParam(0).dd) {
            is AADD -> getParam(0).downQuantity = downQuantity.log(model.builder.scalar(2.0))
            is IDD -> getParam(0).downQuantity = downQuantity.log(model.builder.scalar(2))
            else -> {}
        }
    }

    override fun clone(): AstPower2 {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstPower2(model, parClone)
    }
}
