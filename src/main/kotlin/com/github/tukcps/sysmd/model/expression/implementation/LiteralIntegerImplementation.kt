package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.LiteralInteger
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity

class LiteralIntegerImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "LiteralInteger"
) : LiteralInteger, LiteralExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
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
        direction = direction,
        isEnd = isEnd,
        typeConstraint = typeConstraint,
        expression = expression,
        elementType = elementType,
    )
}