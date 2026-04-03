package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.IndexExpression
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.util.SimpleName

class IndexExpressionImplementation(
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	typeConstraint: MutableList<String> = mutableListOf(),
	expression: String? = null,
	elementType: String = "IndexExpression"
) : IndexExpression, OperatorExpressionImplementation(
	declaredName = declaredName,
	declaredShortName = declaredShortName,
	typeConstraint = typeConstraint,
	expression = expression,
	elementType = elementType)
{
	init {
		operator = "#"
	}

	override fun clone() = IndexExpressionImplementation(
		declaredName= declaredName,
		declaredShortName = declaredShortName,
		typeConstraint = typeConstraint,
		expression = expression,
		elementType = elementType,
	).also {
		it.updateFrom(this)
	}
}