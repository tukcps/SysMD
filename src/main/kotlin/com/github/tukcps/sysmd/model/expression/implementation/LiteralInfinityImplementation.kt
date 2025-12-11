package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.LiteralInfinity
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity

class LiteralInfinityImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "LiteralInfinity"
) : LiteralInfinity, LiteralExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
) {
    override var value = "Infinity" //String as workaround. Replace later.

    // FIXME: Is this the proper unit
    private val quantity = VectorQuantity(model!!.builder.real(Double.POSITIVE_INFINITY), "?")

    override val literalValue : AstLeaf?
        get() = model?.let {
            AstLeaf(it, quantity)
        }

    override val cachedType get() = model?.repo?.realType
    override val typeName = "ScalarValues::Real"
}