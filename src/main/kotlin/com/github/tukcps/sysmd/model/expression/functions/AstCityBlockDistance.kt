package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session

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
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers))
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

    override fun clone(): AstCityBlockDistance {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstCityBlockDistance(model, parClone)
    }
}
