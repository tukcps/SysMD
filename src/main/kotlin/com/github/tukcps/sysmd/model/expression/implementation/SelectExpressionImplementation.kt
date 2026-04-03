package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.SelectExpression
import com.github.tukcps.sysmd.model.util.SimpleName

class SelectExpressionImplementation(
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	typeConstraint: MutableList<String> = mutableListOf(),
	expression: String? = null,
	elementType: String = "SelectExpression"
) : SelectExpression, OperatorExpressionImplementation(
	declaredName = declaredName,
	declaredShortName = declaredShortName,
	typeConstraint = typeConstraint,
	expression = expression,
	elementType = elementType)
{

	init {
		operator = "select"
	}

	override fun clone() = SelectExpressionImplementation(
		declaredName= declaredName,
		declaredShortName = declaredShortName,
		typeConstraint = typeConstraint,
		expression = expression,
		elementType = elementType,
	).also {
		it.updateFrom(this)
	}
}