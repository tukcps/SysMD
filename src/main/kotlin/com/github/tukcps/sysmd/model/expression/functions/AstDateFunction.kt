package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.*
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.StrDD
import io.github.tukcps.aadd.dd.map
import io.github.tukcps.aadd.values.real.ia.RealRange

/** The class of unary functions that map ISO-formatted dates onto unix epoch timestamps */
sealed class AstDateFunction(
    name : String, model : Session, args : ArrayList<AstNode>,
) : AstFunction(name, model, 1, args)
{
    init {
        if (args.size != 1)
            throw SemanticError("$name expects 1 parameter of type String")
    }

    abstract fun format(x : RealRange) : String
    abstract fun parse(date : String) : Double

    private val argument : StrDD get() = (getParam(0).upQuantity.values
            .singleOrNull() ?: throw VectorDimensionError("$name is not possible with Vectors"))
            as? StrDD ?: throw SemanticError("Argument to $name must be a string")

    final override fun initialize() {
        upQuantity = VectorQuantity.fromCanonical(model.builder.Reals.All, Unit("s"), name)
        evalUp()
        downQuantity = upQuantity.clone()
    }

    final override fun evalUp() {
        val times = argument.map(model.builder.Reals) { s ->
            model.builder.real(parse(s.str).let { it .. it })
        }
        upQuantity = VectorQuantity.fromCanonical(times, Unit("s"), name)
    }

    final override fun evalDown() {
        val r = Representer.default
        val badPatterns = listOf("..", r.illegalValue, r.infinityString, r.negativeInfinityString)
        val str = downQuantity.aadd().map(model.builder.Strings) { r ->
            val s = format(r)

            if(badPatterns.any { it in s })
                model.builder.Strings.Infeasible // or empty?
            else
                model.builder.string(s) as StrDD.Leaf
        }

        getParam(0).downQuantity = VectorQuantity(listOf(str))
    }

    abstract override fun clone(): AstDateFunction
}