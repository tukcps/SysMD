package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.BDD
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session


/**
 * Predefined functions: toReal x: Bool -> Real
 * Casts an integer value to a real value.
 */
internal class AstToReal(model: Session, args: ArrayList<AstNode>) :
    AstFunction("toReal", model, 1, args) {

    init {
        if (parameters.size != 1)
            throw SemanticError("toReal function (Boolean -> Real) expects exactly one parameter of type Bool")
    }

    override fun initialize() {
        if (!parameters[0].isBool)
            throw SemanticError("Paramete of toReal function must be of type Bool")
        upQuantity = VectorQuantity(mutableListOf(model.builder.Reals), "")
        evalUp()
        downQuantity = upQuantity.clone()
    }

    /**
     * up = if (true) 1.0 else 0.0
     */
    override fun evalUp() {
        val results = mutableListOf<AADD>()
        getParam(0).bdds.forEach {
            results.add(it.asBdd().ite(model.builder.range(1.0, 1.0), model.builder.range(0.0, 0.0)))
        }
        upQuantity = VectorQuantity(results, "1")
    }

    /**
     * The values must permit some tolerance as Doubles might not be accurately 0 or 1.
     * To be perfectly robust, we just check if 0 or 1 is in the range.
     */
    override fun evalDown() {
        val results = mutableListOf<BDD>()
        downQuantity.aadds().forEach {
            when {
                0.0 in it && 1.0 !in it -> results.add(model.builder.False)
                1.0 in it && 0.0 !in it -> results.add(model.builder.True)
                0.0 in it && 1.0 in it -> results.add(model.builder.Bool)
                0.0 !in it && 1.0 !in it -> results.add(model.builder.NaB)
            }
        }
        getParam(0).downQuantity = VectorQuantity(results)
    }


    override fun toExpressionString(): String {
        return "toReal(${getParam(0).toExpressionString()})"
    }

    override fun clone(): AstToReal {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstToReal(model, parClone)
    }
}

