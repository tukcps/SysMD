package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.LiteralBoolean
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class LiteralBooleanImplementation(
	model : Session,
	elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    expression: String? = null,
) : LiteralBoolean, LiteralExpressionImplementation(
	model,
	elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression,
)  {
	override var value: Boolean? = null

	override val literalValue : AstLeaf?
		get() {
			val v = value ?: return null
			return AstLeaf(model, VectorQuantity(model.builder.boolean(v)))
		}

	override val typeName = "ScalarValues::Boolean"
	override val cachedType : Type? get() = model.repo.booleanType

	override fun clone() = LiteralBooleanImplementation(
		model,
		declaredName = declaredName,
		declaredShortName = declaredShortName,
		expression = expression
	).also {
		it.updateFrom(this)
	}

    override fun updateFrom(template: Element) {
	    super.updateFrom(template)

		if(template is LiteralBooleanImplementation)
            value = template.value
    }
}