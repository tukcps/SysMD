package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.DDError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session

/**
 * Predefined functions: sqr
 */
internal class AstSqr(model: Session, args: ArrayList<AstNode>) : AstFunction("sqr", model, 1, args) {
    init {
        if (args.size !in 1..1)
            throw SemanticError("Sqr function expects one parameter of type Real or Integer")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers))
            else -> throw SemanticError("Sqr function must have Real or Integer parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.sqr()
    }

    override fun evalDown() {
        when (downQuantity.values[0]) {
            is IDD -> {
                val results = mutableListOf<IDD>()
                downQuantity.values.indices.forEach {
                    val max = downQuantity.values[it].asIdd().sqrt().getRange().max
                    results.add(model.builder.integer(-max..max))
                }
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(VectorQuantity(results))

            }

            is AADD  -> {
                val results = mutableListOf<AADD>()
                downQuantity.values.indices.forEach {
                    var valueForSqrt = downQuantity.values[it].asAadd()
                    if (valueForSqrt.min in -0.00001..0.00001)
                        valueForSqrt = downQuantity.values[it].builder.real(0.0..valueForSqrt.max)
                    val max = valueForSqrt.sqrt().getRange().max
                    results.add(model.builder.real(-max .. max))
                }
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(
                    VectorQuantity(
                        results,
                        downQuantity.unit,
                        downQuantity.unitSpec
                    )
                )
            }

            else -> throw DDError("sqr is only possible for Integer and Real values")
        }

    }

    override fun clone(): AstSqr {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstSqr(model, parClone)
    }
}
