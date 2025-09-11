package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ReturnParameterMembership
import com.github.tukcps.sysmd.model.kerml.UnresolvedFeature

class ReturnParameterMembershipImplementation(
	ownedMemberParameter: Feature = UnresolvedFeature(),
	owningType: Feature = UnresolvedFeature(),
	elementType: String = "ParameterMembership",
) : ReturnParameterMembership, ParameterMembershipImplementation(
	ownedMemberParameter = ownedMemberParameter,
	owningType = owningType,
	elementType = elementType,
	parameterDirection = Feature.FeatureDirectionKind.OUT
)