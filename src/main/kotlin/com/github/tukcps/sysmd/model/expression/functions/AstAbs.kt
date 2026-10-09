package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.values.bounds.unaryMinus

/**
 * Predefined functions: abs, absolute value for IDD and AADD
 */
internal class AstAbs(model: Session, args: ArrayList<AstNode>) :
    AstFunction("abs", model, 1, args) {
    private val arg: AstNode = getParam(0)

    override fun initialize() {
        upQuantity = when (arg.upQuantity.values[0]) {
            is AADD -> VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers.All))
            else -> throw SemanticError("Abs function must have a Real or Integer parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.abs()
    }

    override fun evalDown() {
        when (arg.upQuantity.values[0]) {
            is IDD -> {
                val results = mutableListOf<IDD>()
                getParam(0).downQuantity.values.indices.forEach { idx ->
                    val yVal = downQuantity.values.getOrNull(idx)?.asIdd()
                        ?: downQuantity.values.first().asIdd()
                    val p0Val = getParam(0).downQuantity.values[idx].asIdd()
                    if (yVal.max < 0L) {
                        results.add(model.builder.Integers.Empty)
                    } else {
                        val max = yVal.getRange().max
                        results.add(p0Val.constrainTo(model.builder.integer(-max..max)))
                    }
                }
                val newParam = VectorQuantity(results)
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(newParam)
            }

            is AADD -> {
                val results = mutableListOf<AADD>()
                getParam(0).downQuantity.values.indices.forEach { idx ->
                    val yVal = downQuantity.values.getOrNull(idx)?.asAadd()
                        ?: downQuantity.values.first().asAadd()
                    val p0Val = getParam(0).downQuantity.values[idx].asAadd()
                    if (yVal.max < 0.0) {
                        results.add(model.builder.Reals.Empty)
                    } else {
                        val max = yVal.getRange().max
                        results.add(p0Val.constrainTo(model.builder.real(-max..max)))
                    }
                }
                val newParam = VectorQuantity.fromCanonical(results, getParam(0).downQuantity.unit, getParam(0).downQuantity.unitSpec, getParam(0).downQuantity.userWantedUnitSpec)
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(newParam)
            }

            else -> {}
        }
    }

    override fun clone() = AstAbs(model, cloneParameters())
}
