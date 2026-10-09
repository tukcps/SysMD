package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.values.bounds.LongBound

/**
 * Predefined functions: exp
 * Computes exponential function e to the power of parameter
 */
internal class AstExp(model: Session, args: ArrayList<AstNode>) :
    AstFunction("exp", model, 1, args) {

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity.fromCanonical(MutableList(getParam(0).upQuantity.values.size) { model.builder.Reals.All }, Unit("?"), "?")
            is IDD -> VectorQuantity(MutableList(getParam(0).upQuantity.values.size) { model.builder.Integers.All })
            else -> throw SemanticError("Exp function must have a Real or Integer parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.exp()
    }

    override fun evalDown() {
        val y = downQuantity
        val p0 = getParam(0).upQuantity
        val lnY = y.ln()
        val newValues = lnY.values.mapIndexed { index, lnVal ->
            val p0Val = p0.values.getOrNull(index) ?: p0.values.first()
            val yVal = y.values[index]
            if (p0Val is IDD && yVal is IDD && yVal.min <= 0L) {
                if (lnVal.asIdd().isEmpty()) {
                    yVal.builder.Integers.Empty
                } else {
                    yVal.builder.integer(LongBound.NegativeInfinity..lnVal.asIdd().max)
                }
            } else if (p0Val is AADD && yVal is AADD && yVal.max <= 0.0) {
                yVal.builder.Reals.Empty
            } else {
                lnVal
            }
        }
        val resultingQuantity = if (newValues[0] is AADD) {
            VectorQuantity.fromCanonical(newValues, lnY.unit, lnY.unitSpec, lnY.userWantedUnitSpec)
        } else {
            VectorQuantity(newValues)
        }
        getParam(0).downQuantity = getParam(0).downQuantity.constrain(resultingQuantity)
    }

    override fun clone() = AstExp(model, cloneParameters())

}
