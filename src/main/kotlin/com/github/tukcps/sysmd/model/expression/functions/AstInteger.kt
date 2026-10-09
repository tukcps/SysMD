package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.convexHull
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import kotlin.math.ceil
import kotlin.math.floor


/**
 * The function ToInteger : Real -> Integer converts a Real to an Integer.
 */
class AstInteger(model: Session, args: ArrayList<AstNode>) :
    AstFunction("ToInteger", model, 1, args) {

    init {
        if (parameters.size != 1)
            throw SemanticError("Integer function expects exactly one parameter")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Integers.All))
            else -> throw SemanticError("Parameter of Integer function must be of type Real")
        }
        evalUp()
        downQuantity = upQuantity.clone()
        require(downQuantity.values[0] is IDD)
    }

    @Throws(SemanticError::class)
    override fun evalUp() {
        val results = mutableListOf<IDD>()
        getParam(0).aadds.forEach {
            val min = floor(it.min.toDouble()).toLong()
            val max = ceil(it.max.toDouble()).toLong()
            results.add(model.builder.integer(min..max))
        }
        upQuantity = VectorQuantity(results)

    }

    override fun evalDown() {
        val results = mutableListOf<AADD>()
        downQuantity.idds().forEach {
            if (it.isEmpty()) {
                results.add(model.builder.Reals.Empty)
            } else {
                results.add(model.builder.real(convexHull(it)))
            }
        }
        val newParam = VectorQuantity.fromCanonical(
            results,
            getParam(0).downQuantity.unit,
            getParam(0).downQuantity.unitSpec,
            getParam(0).downQuantity.userWantedUnitSpec
        )
        getParam(0).downQuantity = getParam(0).downQuantity.constrain(newParam)
    }

    override fun clone() = AstInteger(model, cloneParameters())
}
