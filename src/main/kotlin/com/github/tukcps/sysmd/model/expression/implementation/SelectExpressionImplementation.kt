package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.SelectExpression
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class SelectExpressionImplementation(
	model : Session,
	elementId : Uuid = Uuid.random(),
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	expression: String? = null
) : SelectExpression, OperatorExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression
) {

	init {
		operator = "select"
	}

	override fun clone() = SelectExpressionImplementation(
		model,
		declaredName= declaredName,
		declaredShortName = declaredShortName,
		expression = expression,
	).also {
		it.updateFrom(this)
	}
}