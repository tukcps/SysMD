package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.LiteralRational
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.Real

class LiteralRationalImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "LiteralRational"
) : LiteralRational, LiteralExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
) {
    override var value: Real? = null //Real? Or Float/double?

    override val literalValue : AstLeaf?
        get() {
            val v = value ?: return null
            val m = model ?: return null
            // FIXME: Unit
            return AstLeaf(m, VectorQuantity( listOf(m.builder.real(v)), "?" ))
        }

    override val cachedType get() = model?.repo?.realType
    override val typeName = "ScalarValues::Real"
}