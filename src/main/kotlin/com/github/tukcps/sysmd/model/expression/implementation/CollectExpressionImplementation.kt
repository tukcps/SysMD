package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.CollectExpression
import com.github.tukcps.sysmd.model.util.SimpleName

class CollectExpressionImplementation(
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	typeConstraint: MutableList<String> = mutableListOf(),
	expression: String? = null,
	elementType: String = "CollectExpression"
) : CollectExpression, OperatorExpressionImplementation(
	declaredName = declaredName,
	declaredShortName = declaredShortName,
	typeConstraint = typeConstraint,
	expression = expression,
	elementType = elementType)
{
	init {
		operator = "collect"
	}

	override fun clone() = CollectExpressionImplementation(
		declaredName= declaredName,
		declaredShortName = declaredShortName,
		typeConstraint = typeConstraint,
		expression = expression,
		elementType = elementType,
	).also {
		it.updateFrom(this)
	}
}