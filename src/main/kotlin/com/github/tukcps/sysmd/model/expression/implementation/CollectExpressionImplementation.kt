package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.CollectExpression
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class CollectExpressionImplementation(
	model : Session,
	elementId : Uuid = Uuid.random(),
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	expression: String? = null,
) : CollectExpression, OperatorExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression
) {
	init {
		operator = "collect"
	}

	override fun clone() = CollectExpressionImplementation(
		model,
		expression = expression,
	).also {
		it.updateFrom(this)
	}
}