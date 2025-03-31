package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session


/**
 * Predefined functions: intersect
 * Computes the intersection of two parameters (of their ranges)
 */
internal class AstIntersect(model: Session, args: ArrayList<AstNode>) :
    AstFunction("intersect", model, 2, args) {

    /**
     * Checks the type of both parameters.
     */
    override fun initialize() {
        if ((getParam(0).upQuantity.values[0] !is AADD) && (getParam(0).upQuantity.values[0] !is IDD))
            throw SemanticError("Intersect only takes Real or Integer parameters")
        if ((getParam(1).upQuantity.values[0] !is AADD) && (getParam(1).upQuantity.values[0] !is IDD))
            throw SemanticError("Intersect only takes Real or Integer parameters")
        if (getParam(0).upQuantity.values[0] is AADD) {
            upQuantity = VectorQuantity(mutableListOf(model.builder.Reals), "?")
            if (getParam(1).upQuantity.values[0] !is AADD)
                throw SemanticError("Intersect requires both parameters of same type")
        }
        if (getParam(0).upQuantity.values[0] is IDD) {
            upQuantity = VectorQuantity(mutableListOf(model.builder.Integers))
            if (getParam(1).upQuantity.values[0] !is IDD)
                throw SemanticError("Intersect requires both parameters of same type")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    /**
     * Implementation of intersect(a, b)
     */
    override fun evalUp() {
        // compute intersection
        val results = mutableListOf<DD<*>>()
        getParam(0).upQuantity.values.indices.forEach {
            results.add(
                when (getParam(0).upQuantity.values[0]) {
                    is AADD -> (getParam(0).upQuantity.values[it] as AADD).intersect(getParam(1).upQuantity.values[it] as AADD)
                    is IDD -> (getParam(0).upQuantity.values[it] as IDD).intersect(getParam(1).upQuantity.values[it] as IDD)
                    is BDD -> (getParam(0).upQuantity.values[it] as BDD).intersect(getParam(1).upQuantity.values[it] as BDD)
                    else -> {
                        throw SemanticError("Intersect is only defined on Integer, Real, Boolean")
                    }
                }
            )
        }
        upQuantity = VectorQuantity(results, getParam(0).upQuantity.unit, getParam(0).upQuantity.unitSpec)
    }

    /**
     * Implementation of inverse function of y = intersect(a, b):
     * we maintain very optimistically the downwards result y for a and b.
     * (could be more precise).
     */
    override fun evalDown() {
        getParam(0).downQuantity = downQuantity.clone()
        getParam(1).downQuantity = downQuantity.clone()
    }

    override fun clone(): AstIntersect {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstIntersect(model, parClone)
    }
}
