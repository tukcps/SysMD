package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.DDError
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.DDBuilder.IntMath.inverseSqr
import io.github.tukcps.aadd.DDBuilder.RealMath.inverseSqr
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.values.bounds.rangeTo

/**
 * Predefined functions: sqr
 */
internal class AstSqr(model: Session, args: ArrayList<AstNode>) : AstFunction("sqr", model, 1, args) {
    init {
        if (args.size != 1)
            throw SemanticError("Sqr function expects one parameter of type Real or Integer")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers.All))
            else -> throw SemanticError("Sqr function must have Real or Integer parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.sqr()
    }

    override fun evalDown() {
        val resUnit = downQuantity.unit.sqrt()

        when (downQuantity.values[0]) {
            is IDD -> {
                val results = mutableListOf<IDD>()
                downQuantity.values.indices.forEach {
                    val valForSqrt = downQuantity.values[it].asIdd()
                    if (valForSqrt.max < 0L) {
                        results.add(model.builder.Integers.Empty)
                    } else {
                        val clamped = if (valForSqrt.min < 0L)
                            model.builder.integer(0L..valForSqrt.max)
                        else
                            valForSqrt
                        results.add(inverseSqr(clamped))
                    }
                }
                val newParam = VectorQuantity(results)
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(newParam)
            }

            is AADD  -> {
                val results = mutableListOf<AADD>()
                downQuantity.values.indices.forEach {
                    val valueForSqrt = downQuantity.values[it].asAadd()
                    if (valueForSqrt.max < 0.0) {
                        results.add(model.builder.Reals.Empty)
                    } else {
                        val clamped = if (valueForSqrt.min < 0.0)
                            model.builder.real(0.0 .. valueForSqrt.max)
                        else
                            valueForSqrt
                        results.add(inverseSqr(clamped))
                    }
                }
                val newParam = VectorQuantity.fromCanonical(results, resUnit, getParam(0).downQuantity.unitSpec, getParam(0).downQuantity.userWantedUnitSpec)
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(newParam)
            }

            else -> throw DDError("sqr is only possible for Integer and Real values")
        }

    }

    override fun clone() = AstSqr(model, cloneParameters())
}
