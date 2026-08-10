package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ResultExpressionMembership
import com.github.tukcps.sysmd.model.util.UnresolvedExpression
import com.github.tukcps.sysmd.model.util.UnresolvedFeature
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class ResultExpressionMembershipImplementation(
	model : Session,
	elementId : Uuid = Uuid.random(),
	ownedResultExpression: Expression = UnresolvedExpression(model),
	owningFeature: Feature = UnresolvedFeature(model),
	elementType: String = "ResultExpressionMembership"
) : ResultExpressionMembership, FeatureMembershipImplementation(
	model,
	elementId = elementId,
	ownedMemberFeature = ownedResultExpression,
	owningType = owningFeature,
	elementType = elementType,
) {
	override fun clone(): ResultExpressionMembership = ResultExpressionMembershipImplementation(
		model,
		ownedResultExpression = ownedResultExpression,
		owningFeature = owningRelatedElement as Feature,
	).also {
		it.updateFrom(this)
	}
}
