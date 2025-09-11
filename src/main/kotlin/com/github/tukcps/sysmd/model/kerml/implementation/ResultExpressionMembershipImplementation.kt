package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ParameterMembership
import com.github.tukcps.sysmd.model.kerml.ResultExpressionMembership
import com.github.tukcps.sysmd.model.kerml.UnresolvedFeature

open class ResultExpressionMembershipImplementation(
	ownedResultExpression: Expression, // TODO: UnresolvedExpression()
	owningFeature: Feature = UnresolvedFeature(),
	elementType: String = "ResultExpressionMembership"
) : ResultExpressionMembership, FeatureMembershipImplementation(
	ownedMemberFeature = ownedResultExpression,
	owningType = owningFeature,
	elementType = elementType,
)
