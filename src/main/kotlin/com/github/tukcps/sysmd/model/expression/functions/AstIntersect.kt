package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.BDD
import io.github.tukcps.aadd.dd.DD
import io.github.tukcps.aadd.dd.IDD


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
            upQuantity = VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
            if (getParam(1).upQuantity.values[0] !is AADD)
                throw SemanticError("Intersect requires both parameters of same type")
        }
        if (getParam(0).upQuantity.values[0] is IDD) {
            upQuantity = VectorQuantity(mutableListOf(model.builder.Integers.All))
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
        upQuantity = VectorQuantity.fromCanonical(results, getParam(0).upQuantity.unit, getParam(0).upQuantity.unitSpec, getParam(0).upQuantity.userWantedUnitSpec)
    }

    /**
     * Implementation of inverse function of y = intersect(a, b):
     * The result only tells that its range is contained in the ranges of a and b. The parts of a outside the result
     * may still be valid when b does not cover them, so neither parameter can be narrowed soundly.
     */
    override fun evalDown() {
        // nothing to propagate
    }

    override fun clone() = AstIntersect(model, cloneParameters())
}
