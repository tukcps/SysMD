package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.DDBuilder.RealMath.invCeil
import io.github.tukcps.aadd.Integer
import io.github.tukcps.aadd.Real
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.values.bounds.*


/**
 * Predefined functions: ceil
 * Computes ceiling function
 */
internal class AstCeil(model: Session, args: ArrayList<AstNode>) :
    AstFunction("ceil", model, 1, args) {

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is Real -> VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
            is Integer -> VectorQuantity(mutableListOf(model.builder.Integers.All))
            else -> throw SemanticError("Ceil function only takes a Real or Integer parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> getParam(0).upQuantity.ceil()
            is IDD -> getParam(0).upQuantity.clone()
            else -> throw SemanticError("Ceil function only takes a Real or Integer parameter")
        }
    }

    override fun evalDown() {
        val propagated = when (getParam(0).upQuantity.values[0]) {
            is AADD -> {
                // invert in the unit in which evalUp rounds: the displayed unit of the parameter
                val param = getParam(0).upQuantity
                VectorQuantity.fromCanonical(downQuantity.values, param.unit, param.unitSpec, param.userWantedUnitSpec)
                    .invertRoundingInDisplayedUnit("Ceil") { invCeil(it) }
            }

            is IDD -> downQuantity.clone()

            else -> throw SemanticError("Ceil function only takes a Real or Integer parameter")
        }
        getParam(0).downQuantity = getParam(0).downQuantity.constrain(propagated)
    }

    override fun clone() = AstCeil(model, cloneParameters())
}
