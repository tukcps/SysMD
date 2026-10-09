package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.DDBuilder.RealMath.invFloor
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.values.bounds.LongBound
import io.github.tukcps.aadd.values.bounds.minus

/**
 * Predefined functions: floor
 * Computes floor function
 */
internal class AstFloor(model: Session, args: ArrayList<AstNode>) :
    AstFunction("floor", model, 1, args) {

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals.All), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers.All))
            else -> throw SemanticError("Floor function only takes a Real or Integer parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> getParam(0).upQuantity.floor()
            is IDD -> getParam(0).upQuantity.clone()
            else -> throw SemanticError("Floor function only takes a Real or Integer parameter")
        }
    }

    override fun evalDown() {
        val propagated = when (getParam(0).upQuantity.values[0]) {
            is AADD -> {
                // invert in the unit in which evalUp rounds: the displayed unit of the parameter
                val param = getParam(0).upQuantity
                VectorQuantity.fromCanonical(downQuantity.values, param.unit, param.unitSpec, param.userWantedUnitSpec)
                    .invertRoundingInDisplayedUnit("Floor") { invFloor(it) }
            }

            is IDD -> downQuantity.clone()

            else -> throw SemanticError("Floor function only takes a Real or Integer parameter")
        }
        getParam(0).downQuantity = getParam(0).downQuantity.constrain(propagated)
    }

    override fun clone() = AstFloor(model, cloneParameters())
}
