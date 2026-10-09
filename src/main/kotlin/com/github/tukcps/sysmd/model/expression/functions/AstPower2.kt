package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.bounds.LongBound

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
            is AADD -> VectorQuantity.fromCanonical(MutableList(getParam(0).upQuantity.values.size) { model.builder.Reals.All })
            is IDD -> VectorQuantity(MutableList(getParam(0).upQuantity.values.size) { model.builder.Integers.All })
            else -> throw SemanticError("pow2 function must have a Real or Integer parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity =  getParam(0).upQuantity.pow2()
    }

    override fun evalDown() {
        val y = downQuantity
        val p0 = getParam(0).upQuantity
        val log2Y = y.log2()
        val newValues = log2Y.values.mapIndexed { index, logVal ->
            val p0Val = p0.values.getOrNull(index) ?: p0.values.first()
            val yVal = y.values[index]
            if (p0Val is IDD && yVal is IDD && yVal.min <= LongBound.Finite(0L)) {
                if (logVal.asIdd().isEmpty()) {
                    yVal.builder.Integers.Empty
                } else {
                    yVal.builder.integer(LongBound.NegativeInfinity..logVal.asIdd().max)
                }
            } else if (p0Val is AADD && yVal is AADD && yVal.max.toDouble() <= 0.0) {
                yVal.builder.Reals.Empty
            } else {
                logVal
            }
        }
        val resultingQuantity = if (newValues[0] is AADD) {
            VectorQuantity.fromCanonical(newValues, log2Y.unit, log2Y.unitSpec, log2Y.userWantedUnitSpec)
        } else {
            VectorQuantity(newValues)
        }
        getParam(0).downQuantity = getParam(0).downQuantity.constrain(resultingQuantity)
    }

    override fun clone() = AstPower2(model, cloneParameters())
}
