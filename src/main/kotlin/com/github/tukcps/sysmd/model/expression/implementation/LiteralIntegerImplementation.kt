package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.LiteralInteger
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class LiteralIntegerImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    expression: String? = null
) : LiteralInteger, LiteralExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression
) {
    override var value: Long? = null

    override val literalValue : AstLeaf?
        get() {
            val v = value ?: return null
            return AstLeaf(model, VectorQuantity(model.builder.integer(v)))
        }

    override val cachedType get() = model.repo.integerType
    override val typeName = "ScalarValues::Integer"

    override fun clone() = LiteralIntegerImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        expression = expression
    ).also {
        it.updateFrom(this)
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)

        if (template is LiteralIntegerImplementation)
            value = template.value
    }
}