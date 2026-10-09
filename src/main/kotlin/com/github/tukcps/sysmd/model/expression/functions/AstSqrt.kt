package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.bounds.LongBound

/**
 * Predefined functions: sqrt (square root)
 */
internal class AstSqrt(model: Session, args: ArrayList<AstNode>) : AstFunction("sqrt", model, 1, args) {
    init {
        if (args.size !in 1..1)
            throw SemanticError("Sqrt function expects one parameter of type Real or Integer")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers.All))
            else -> throw SemanticError("Sqrt function must have a Real or Integer parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = (getParam(0).upQuantity.sqrt())
    }

    override fun evalDown() {
        val clampedValues = downQuantity.values.map {
            when (it) {
                is AADD -> {
                    if (it.max.toDouble() < 0.0) {
                        model.builder.Reals.Empty
                    } else if (it.min.toDouble() < 0.0) {
                        model.builder.real(0.0..it.max.toDouble())
                    } else {
                        it
                    }
                }
                is IDD -> {
                    if (it.max < LongBound.Finite(0L)) {
                        model.builder.Integers.Empty
                    } else if (it.min < LongBound.Finite(0L)) {
                        model.builder.integer(LongBound.Finite(0L)..it.max)
                    } else {
                        it
                    }
                }
                else -> throw SemanticError("Sqrt is only possible for Integer and Real values")
            }
        }
        val clampedQuantity = if (clampedValues[0] is AADD) {
            VectorQuantity.fromCanonical(clampedValues, downQuantity.unit, downQuantity.unitSpec, downQuantity.userWantedUnitSpec)
        } else {
            VectorQuantity(clampedValues)
        }
        getParam(0).downQuantity = getParam(0).downQuantity.constrain(clampedQuantity.sqr())
    }


    override fun <T> runDepthFirst(block: AstNode.() -> T): T {
        for (p in parameters) p.runDepthFirst(block)
        return block()
    }

    override fun clone() = AstSqrt(model, cloneParameters())
}
