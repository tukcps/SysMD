package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.util.convexHull
import io.github.tukcps.aadd.util.roundInwards


/**
 * The function Real : Int -> Real converts an integer to a real.
 */
class AstReal(model: Session, args: ArrayList<AstNode>) :
    AstFunction("Real", model, 1, args) {

    init {
        if (parameters.size != 1)
            throw SemanticError("Real function expects one parameter of type Integer")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is IDD -> VectorQuantity(mutableListOf(model.builder.Reals.All))
            else -> throw SemanticError("Real function parameter must be of type Integer")
        }
        evalUp()
        downQuantity = upQuantity.clone()
        require(downQuantity.values[0] is AADD)
    }

    override fun evalUp() {
        val results = getParam(0).idds.map {
            model.builder.real(convexHull(it))
        }
        upQuantity = VectorQuantity.fromCanonical(results, unitsFrom = getParam(0).upQuantity)
    }

    override fun evalDown() {
        val results = downQuantity.aadds().map {
            model.builder.integer(roundInwards(it))
        }

        getParam(0).downQuantity = getParam(0).downQuantity.constrain(VectorQuantity(results))
    }

    override fun clone() = AstReal(model, cloneParameters())
}
