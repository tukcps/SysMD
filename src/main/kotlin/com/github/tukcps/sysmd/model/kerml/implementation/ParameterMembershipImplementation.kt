package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ParameterMembership
import com.github.tukcps.sysmd.model.kerml.UnresolvedFeature

open class ParameterMembershipImplementation(
	ownedMemberParameter: Feature = UnresolvedFeature(),
	owningType: Feature = UnresolvedFeature(),
	elementType: String = "ParameterMembership",
	override val parameterDirection : Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
) : ParameterMembership, FeatureMembershipImplementation(
	ownedMemberFeature = ownedMemberParameter,
	owningType = owningType,
	elementType = elementType,
)
