package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.convexHull
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.bounds.LongBound
import kotlin.math.ceil
import kotlin.math.floor


/**
 * The function Real : Int -> Real converts an integer to a real.
 */
class AstReal(model: Session, args: ArrayList<AstNode>) :
    AstFunction("Real", model, 1, args) {

    init {
        if (parameters.size != 1)
            throw SemanticError("Real function expects one parameter of type Integer")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is IDD -> VectorQuantity(mutableListOf(model.builder.Reals.All))
            else -> throw SemanticError("Real function parameter must be of type Integer")
        }
        evalUp()
        downQuantity = upQuantity.clone()
        require(downQuantity.values[0] is AADD)
    }

    override fun evalUp() {
        val results = mutableListOf<AADD>()
        getParam(0).idds.forEach {
            results.add(model.builder.real(convexHull(it)))
        }
        upQuantity = VectorQuantity.fromCanonical(results, getParam(0).upQuantity.unit, getParam(0).upQuantity.unitSpec, getParam(0).upQuantity.userWantedUnitSpec)
    }

    override fun evalDown() {
        val results = mutableListOf<IDD>()
        downQuantity.aadds().forEach {
            val minCeil = ceil(it.min.toDouble()).toLong()
            val maxFloor = floor(it.max.toDouble()).toLong()
            if (minCeil <= maxFloor) {
                results.add(model.builder.integer(minCeil..maxFloor))
            } else {
                results.add(model.builder.Integers.Empty)
            }
        }
        getParam(0).downQuantity = getParam(0).downQuantity.constrain(VectorQuantity(results))
    }

    override fun clone() = AstReal(model, cloneParameters())
}
