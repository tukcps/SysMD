package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.DDBuilder.RealMath
import io.github.tukcps.aadd.Real
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble

/**
 * Predefined functions: sine
 */
internal class AstSin(model: Session, args: ArrayList<AstNode>) :
    AstFunction("sin", model, 1, args) {

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is Real -> VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
            else -> throw SemanticError("Sin function must have a parameter of type Real")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.sin()
    }

    override fun evalDown() {
        val p0 = getParam(0).downQuantity
        val p0Min = p0.getMinAsDouble()
        val p0Max = p0.getMaxAsDouble()
        if (p0Min >= -Math.PI && p0Max <= 2.0 * Math.PI) {
            val results = mutableListOf<AADD>()
            downQuantity.values.forEach {
                val aadd = it.asAadd()
                if (aadd.max < -1.0 || aadd.min > 1.0) {
                    results.add(model.builder.Reals.Empty)
                } else {
                    val clampedMin = maxOf(-1.0, aadd.min.toDouble())
                    val clampedMax = minOf(1.0, aadd.max.toDouble())
                    val clamped = model.builder.real(clampedMin..clampedMax)
                    val asinVal = RealMath.asin(clamped)
                    if (asinVal.isEmpty()) {
                        results.add(model.builder.Reals.Empty)
                    } else {
                        val res = when {
                            p0Min >= -Math.PI / 2.0 && p0Max <= Math.PI / 2.0 -> asinVal
                            p0Min >= Math.PI / 2.0 && p0Max <= 3.0 * Math.PI / 2.0 ->
                                model.builder.real(Math.PI - asinVal.max.toDouble()..Math.PI - asinVal.min.toDouble())
                            p0Max <= -Math.PI / 2.0 && p0Min >= -3.0 * Math.PI / 2.0 ->
                                model.builder.real(-Math.PI - asinVal.max.toDouble()..-Math.PI - asinVal.min.toDouble())
                            // x and PI - x both solve sin(x) = y: take the hull of both branches
                            p0Min >= -Math.PI / 2.0 && p0Max <= 3.0 * Math.PI / 2.0 -> {
                                val lo = asinVal.min.toDouble()
                                val hi = asinVal.max.toDouble()
                                model.builder.real(minOf(lo, Math.PI - hi)..maxOf(hi, Math.PI - lo))
                            }
                            else -> model.builder.Reals.All // several branches possible: no sound restriction
                        }
                        results.add(res)
                    }
                }
            }
            val newP0 = VectorQuantity.fromCanonical(results, getParam(0).downQuantity.unit, getParam(0).downQuantity.unitSpec, getParam(0).downQuantity.userWantedUnitSpec)
            getParam(0).downQuantity = getParam(0).downQuantity.constrain(newP0)
        }
    }

    override fun clone() = AstSin(model, cloneParameters())
}
