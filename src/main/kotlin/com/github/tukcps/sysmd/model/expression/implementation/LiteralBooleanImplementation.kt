package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.LiteralBoolean
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity

class LiteralBooleanImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "LiteralBoolean"
) : LiteralBoolean, LiteralExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
)  {
	override var value: Boolean? = null

	override val literalValue : AstLeaf?
		get() {
			val v = value ?: return null
			val m = model ?: return null
			return AstLeaf(m, VectorQuantity(m.builder.boolean(v)))
		}

	override val typeName = "ScalarValues::Boolean"
	override val cachedType : Type? get() = model?.repo?.booleanType

	override fun clone() = LiteralBooleanImplementation(
		declaredName = declaredName,
		declaredShortName = declaredShortName,
		typeConstraint = typeConstraint,
		expression = expression
	).also {
		it.updateFrom(this)
	}

    override fun updateFrom(template: Element) {
	    super.updateFrom(template)
	    if (template is LiteralBooleanImplementation)
            value = template.value
    }
}