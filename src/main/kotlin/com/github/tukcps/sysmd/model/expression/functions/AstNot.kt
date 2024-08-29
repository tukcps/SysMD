package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.BDD
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session

/**
 * Predefined functions: not
 */
class AstNot(model: Session, args: ArrayList<AstNode>) :
    AstFunction("not", model, 1, args) {

    init {
        if (parameters.size != 1)
            throw SemanticError("Not expects one parameter only")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is BDD -> Quantity(model.builder.Bool)
            else -> throw SemanticError("Parameter of not function parameter must be of type Bool")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }


    override fun evalUp() {
        val results = mutableListOf<BDD>()
        getBDDParam(0).upQuantity.values.forEach {
            results.add(it.asBdd().not())
        }
        upQuantity = VectorQuantity(results, getParam(0).upQuantity.unit, getParam(0).upQuantity.unitSpec)
    }

    /** No propagation; Boolean functions are handled by discrete solver */
    override fun evalDown() {}


    override fun toExpressionString(): String {
        return "not(${getParam(0).toExpressionString()})"
    }

    override fun clone(): AstNot {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstNot(model, parClone)
    }
}
