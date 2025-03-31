package com.github.tukcps.sysmd.model.expression.functions


import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session

/**
 * Predefined functions: x^y (x to the power of y)
 */
internal class AstPower(model: Session, args: ArrayList<AstNode>) : AstFunction("power", model, 2, args) {

    private val exponent: DD<*>
        get() = getParam(1).dd

    init {
        if (args.size != 2)
            throw SemanticError("Power function expects two parameters of types Real or Integer")
    }

    override fun initialize() {
        if (getParam(1).upQuantity.values.size != 1)
            throw VectorDimensionError("Power function is not possible with Vector as exponent")
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> Quantity(model.builder.Reals, "")
            is IDD -> Quantity(model.builder.Integers)
            else -> throw SemanticError("Power function must have Real or Integer parameters")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = if (upQuantity.values[0] is AADD)
            getParam(0).upQuantity.pow(getParam(1).aadd)
        else
            getParam(0).upQuantity.pow(getParam(1).idd)

    }

    override fun evalDown() { //TODO Many special cases for negative numbers are missing
        getParam(0).downQuantity = when (exponent) {
            is AADD -> downQuantity.pow(model.builder.real(1.0).div(exponent.asAadd()))
            is IDD -> {
                val resultingValues = mutableListOf<IDD>()
                downQuantity.values.forEach { resultingValues.add((it as IDD).root(exponent as IDD)) }
                VectorQuantity(resultingValues)
            }

            else -> throw SemanticError("Expected base of type Real or Integer")
        }
        getParam(1).downQuantity = when (exponent) {
            is AADD -> {
                if (getParam(0).aadd.min == 1.0 && getParam(0).aadd.max == 1.0) //Log with 1 not possible. All resulting values allowed
                    Quantity(model.builder.Reals, downQuantity.unit)
                else
                    downQuantity.log(getParam(0).downQuantity)
            }

            is IDD -> {
                if (getParam(0).idd.min == 1L && getParam(0).idd.max == 1L) //Log with 1 not possible. All resulting values allowed
                    Quantity(model.builder.Integers)
                else
                    downQuantity.log(getParam(0).downQuantity)
            }

            else -> throw SemanticError("Expected base of type Real or Integer")
        }
    }


    override fun clone(): AstPower {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstPower(model, parClone)
    }
}
