package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.LiteralInteger
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity

class LiteralIntegerImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "LiteralInteger"
) : LiteralInteger, LiteralExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
) {
    override var value: Long? = null

    override val literalValue : AstLeaf?
        get() {
            val v = value ?: return null
            val m = model ?: return null
            return AstLeaf(m, VectorQuantity(m.builder.integer(v)))
        }

    override val cachedType get() = model?.repo?.integerType
    override val typeName = "ScalarValues::Integer"

    override fun clone() = LiteralIntegerImplementation(
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        typeConstraint = typeConstraint,
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