package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.unaryMinus
import io.github.tukcps.aadd.DDBuilder.RealMath
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.values.bounds.DoubleBound
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.bounds.minus
import io.github.tukcps.aadd.values.bounds.unaryMinus
import io.github.tukcps.aadd.values.real.ia.minus

/**
 * Predefined functions: cosine
 */
internal class AstCos(model: Session, args: ArrayList<AstNode>) :
    AstFunction("cos", model, 1, args) {

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
            else -> throw SemanticError("Cos function must have a parameter of type Real")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.cos()
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
                    val clampedMin = maxOf(DoubleBound.Finite(-1.0), aadd.min)
                    val clampedMax = minOf(DoubleBound.Finite(1.0), aadd.max)
                    val clamped = model.builder.real(clampedMin..clampedMax)
                    val acosVal = RealMath.acos(clamped)
                    if (acosVal.isEmpty()) {
                        results.add(model.builder.Reals.Empty)
                    } else {
                        val res = when {
                            p0Max <= 0.0 -> -acosVal
                            p0Min < 0.0 && p0Max <= Math.PI -> model.builder.real(-acosVal.max..acosVal.max)
                            p0Min >= Math.PI -> model.builder.real((2.0 * Math.PI) - acosVal.getRange())
                            p0Min >= 0.0 && p0Max <= Math.PI -> acosVal
                            // x and 2*PI - x both solve cos(x) = y: take the hull of both branches
                            p0Min >= 0.0 && p0Max <= 2.0 * Math.PI -> {
                                val lo = acosVal.min.toDouble()
                                val hi = acosVal.max.toDouble()
                                model.builder.real(minOf(lo, 2.0 * Math.PI - hi)..maxOf(hi, 2.0 * Math.PI - lo))
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

    override fun clone() = AstCos(model, cloneParameters())
}
