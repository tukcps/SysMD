package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.Integer
import io.github.tukcps.aadd.Real

/**
 * Predefined functions: CityBlockDistance, absolute value for IDD and AADD
 */
internal class AstCityBlockDistance(model: Session, args: ArrayList<AstNode>) :
    AstFunction("cityBlockDistance", model, 2, args) {
    private val arg: AstNode = getParam(0)

    override fun initialize() {
        if (parameters.size != 2)
            throw SysMDError("cityBlockDistance expects two parameters")
        upQuantity = when (arg.upQuantity.values[0]) {
            is Real -> VectorQuantity(mutableListOf(model.builder.Reals.All), "?")
            is Integer -> VectorQuantity(mutableListOf(model.builder.Integers.All))
            else -> throw SemanticError("cityBlockDistance must have Real or Int argument")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = getParam(0).upQuantity.cityBlockDistance(getParam(1).upQuantity)
    }

    override fun evalDown() {
        //TODO
    }

    override fun clone() = AstCityBlockDistance(model, cloneParameters())
}
