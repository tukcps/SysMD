package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.LiteralInfinity
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity

class LiteralInfinityImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "LiteralInfinity"
) : LiteralInfinity, LiteralExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
) {
    override var value = "Infinity" //String as workaround. Replace later.

    // FIXME: Is this the proper unit
    private val quantity get() = VectorQuantity(model!!.builder.real(Double.POSITIVE_INFINITY), "?")

    override val literalValue : AstLeaf?
        get() = model?.let {
            AstLeaf(it, quantity)
        }

    override val cachedType get() = model?.repo?.realType
    override val typeName = "ScalarValues::Real"

    override fun clone() = LiteralInfinityImplementation(
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        typeConstraint = typeConstraint,
        expression = expression
    ).also {
        it.updateFrom(this)
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is LiteralInfinityImplementation)
            value = template.value
    }
}