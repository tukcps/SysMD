package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.Bool
import io.github.tukcps.aadd.dd.AADD


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
            throw SemanticError("Parameter of toReal function must be of type Bool")
        upQuantity = VectorQuantity(mutableListOf(model.builder.Reals.All))
        evalUp()
        downQuantity = upQuantity.clone()
    }

    /**
     * up = if (true) 1.0 else 0.0
     */
    override fun evalUp() {
        val results = mutableListOf<AADD>()
        getParam(0).bdds.forEach {
            results.add(it.asBdd().ite(model.builder.real(1.0..1.0), model.builder.real(0.0..0.0)))
        }
        upQuantity = VectorQuantity.fromCanonical(results, Unit("1"), "1")
    }

    /**
     * The values must permit some tolerance as Doubles might not be accurately 0 or 1.
     * To be perfectly robust, we just check if 0 or 1 is in the range.
     */
    override fun evalDown() {
        val results = mutableListOf<Bool>()
        downQuantity.aadds().forEach {
            when {
                0.0 in it && 1.0 !in it -> results.add(model.builder.Bool.False)
                1.0 in it && 0.0 !in it -> results.add(model.builder.Bool.True)
                0.0 in it && 1.0 in it -> results.add(model.builder.Bool.All)
                0.0 !in it && 1.0 !in it -> results.add(model.builder.Bool.Empty)
            }
        }
        getParam(0).downQuantity = getParam(0).downQuantity.intersect(VectorQuantity(results))
    }

    override fun clone() = AstToReal(model, cloneParameters())
}
