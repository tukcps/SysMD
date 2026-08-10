package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.NullExpression
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class NullExpressionImplementation(
	model : Session,
	elementId : Uuid = Uuid.random(),
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	expression: String? = null,
) : NullExpression, ExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression
) {
	/* TODO: A NullExpression must directly or indirectly specialize the base NullExpression Performances::nullEvaluations from the Kernel Semantic Library */

	override fun learnType() : List<Type> = emptyList() // TODO: return `Base::Anything[0]`

	override fun initialize()
	{
	}

	override fun evalUp()
	{
	}

	override fun evalDown()
	{
	}

	override fun toAstString(b : StringBuilder, precedence : Int)
	{
		b.append("null")
	}

	override fun clone() = NullExpressionImplementation(
		model,
		declaredName = declaredName,
		declaredShortName = declaredShortName,
		expression = expression
	).also {
		it.updateFrom(this)
	}
}