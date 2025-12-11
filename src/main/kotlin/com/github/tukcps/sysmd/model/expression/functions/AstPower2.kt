package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.values.IntegerRange
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

/**
 * Predefined functions: 2^x (two to the power of x)
 */
internal class AstPower2(model: Session, args: ArrayList<AstNode>) :
    AstFunction("power2", model, 1, args) {

    init {
        if (args.size !in 1..1)
            throw SemanticError("pow2 function expects one parameter of type Real")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> Quantity(model.builder.Reals, "")
            is IDD -> Quantity(model.builder.Integers)
            else -> throw SemanticError("pow2 function must have a Real or Integer parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> getParam(0).upQuantity.pow2()
            is IDD -> {
                val results = mutableListOf<IDD>()
                getParam(0).upQuantity.idds().forEach {
                    val min= floor(2.0.pow(it.min.toDouble())).toLong()
                    val max = ceil(2.0.pow(it.max.toDouble())).toLong()
                    results.add(model.builder.integer(min..max))

                }
                 VectorQuantity(results)
            }
            else -> throw SemanticError("pow2 function must have a Real or Integer parameter")
        }
    }

    override fun evalDown() {
        when (getParam(0).dd) {
            is AADD -> getParam(0).downQuantity = downQuantity.log(model.builder.real(2.0))
            is IDD -> getParam(0).downQuantity = downQuantity.log(model.builder.integer(2))
            else -> {}
        }
    }

    override fun clone(): AstPower2 {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstPower2(model, parClone)
    }
}
