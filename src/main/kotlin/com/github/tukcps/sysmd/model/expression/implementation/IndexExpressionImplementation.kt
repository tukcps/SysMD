package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.IndexExpression
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class IndexExpressionImplementation(
	model : Session,
	elementId : Uuid = Uuid.random(),
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	expression: String? = null
) : IndexExpression, OperatorExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression
) {
	init {
		operator = "#"
	}

	override fun clone() = IndexExpressionImplementation(
		model,
		declaredName= declaredName,
		declaredShortName = declaredShortName,
		expression = expression,
	).also {
		it.updateFrom(this)
	}
}