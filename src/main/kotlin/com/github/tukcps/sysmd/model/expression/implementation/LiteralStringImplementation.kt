package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.*
import com.github.tukcps.sysmd.quantities.*
import kotlinx.serialization.json.Json

class LiteralStringImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "LiteralString"
) : LiteralString, LiteralExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
) {
    override var value: String? = null

    override val literalValue : AstLeaf?
        get() {
            val v = value ?: return null
            val m = model ?: return null
            return AstLeaf(m, VectorQuantity(listOf(m.builder.string(v))))
        }

    override val cachedType get() = model?.repo?.stringType
    override val typeName = "ScalarValues::String"

    override fun toAstString(b : StringBuilder, precedence : Int)
    {
        b.append(Json.encodeToString(value))
    }

    override fun clone() = LiteralStringImplementation(
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        typeConstraint = typeConstraint,
        expression = expression
    ).also {
        it.updateFrom(this)
    }

    override fun updateFrom(template: Element) {
        if (template is LiteralStringImplementation)
            value = template.value

        super.updateFrom(template)
    }
}