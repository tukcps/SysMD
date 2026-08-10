package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.LiteralInfinity
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class LiteralInfinityImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    expression: String? = null,
) : LiteralInfinity, LiteralExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression,
) {
    override val value = "Infinity" //String as workaround. Replace later.

    // FIXME: Is this the proper unit
    private val quantity get() = VectorQuantity(model.builder.real(Double.POSITIVE_INFINITY), "?")

    override val literalValue : AstLeaf
        get() = AstLeaf(model, quantity)

    override val cachedType get() = model.repo.realType
    override val typeName = "ScalarValues::Real"

    override fun clone() = LiteralInfinityImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        expression = expression
    ).also {
        it.updateFrom(this)
    }
}