package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.values.bounds.LongBound
import io.github.tukcps.aadd.values.integer.IntegerRange

/**
 * Normalize a vector to length 1
 */
internal class AstQuantityOfVectorAtPosition(model: Session, args: ArrayList<AstNode>) : AstFunction("quantityOfVectorAtPosition", model, 2, args) {
    init {
        if (args.size !in 2..2)
            throw SemanticError("AstQuantityOfVectorAtPosition expects 2 parameters of type Real or Integer Vector and IntegerRange")
    }

    override fun initialize() {
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers.All))
            else -> throw SemanticError("AstQuantityOfVectorAtPosition must have a Real or Integer argument")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        val vec = getParam(0).upQuantity
        val ixQ = getParam(1).upQuantity

        if(ixQ.values.size != 1)
            throw SemanticError("Index in `quantityOfVectorAtPosition` must not be a vector")

        val ixV = ixQ.value

        if(ixV !is IDD)
            throw SemanticError("Index in `quantityOfVectorAtPosition` must be an integer")

        val validIx = ixV.constrainTo(IntegerRange(0L, vec.values.size - 1L))

        val properEmpty = with(ixV.builder) {
            when(vec.values.firstOrNull() ?: Reals.Empty) {
                is AADD -> Reals.Empty
                is IDD -> Integers.Empty
                is BDD -> Bool.Empty
                is StrDD -> Strings.Empty
            }
        }

        val upValues = when {
            validIx.getRange().isEmpty() ->  mutableListOf(properEmpty)
            vec.values.isEmpty() -> mutableListOf(properEmpty)
            else -> vec.values.filterIndexed{ ix, _ ->
                ix in validIx.getRange()
            }
        }

        upQuantity = VectorQuantity.fromCanonical( upValues , vec.unit, vec.unitSpec, vec.userWantedUnitSpec)
    }

    operator fun IntegerRange.contains(v : Int) = this.contains(LongBound.Finite(v.toLong()))

    override fun evalDown()
    {
        // TODO
    }

    override fun <T> runDepthFirst(block: AstNode.() -> T): T {
        for (p in parameters) p.runDepthFirst(block)
        return block()
    }

    override fun clone() = AstQuantityOfVectorAtPosition(model, cloneParameters())
}
