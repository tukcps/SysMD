package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.LiteralRational
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.Real

class LiteralRationalImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "LiteralRational"
) : LiteralRational, LiteralExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
) {
    override var value: Double? = null //Real? Or Float/double?

    override val literalValue : AstLeaf?
        get() {
            val v = value ?: return null
            val m = model ?: return null
            // FIXME: Unit
            // Unit should be best handled as a second feature typed by string? as in standard?
            // We map it only ot a variable where we per variable use value/unit and multiple dimensions in one variable
            // for efficiency and ease-of-debug
            return AstLeaf(m, VectorQuantity( listOf(m.builder.real(v)), "?" ))
        }

    override val cachedType get() = model?.repo?.realType
    override val typeName = "ScalarValues::Real"

    override fun clone() = LiteralRationalImplementation(
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        typeConstraint = typeConstraint,
        expression = expression
    ).also {
        it.updateFrom(this)
    }

    override fun updateFrom(template: Element) {
        if (template is LiteralRationalImplementation)
            value = template.value
        super.updateFrom(template)
    }
}