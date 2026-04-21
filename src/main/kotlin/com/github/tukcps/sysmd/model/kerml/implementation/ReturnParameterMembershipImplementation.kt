package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Feature.FeatureDirectionKind.OUT
import com.github.tukcps.sysmd.model.kerml.ReturnParameterMembership
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedFeature

class ReturnParameterMembershipImplementation(
	ownedMemberParameter: Feature = UnresolvedFeature(),
	owningType: Type = UnresolvedFeature(),
	elementType: String = "ReturnParameterMembership",
	parameterIndex : Int = -1
) : ReturnParameterMembership, ParameterMembershipImplementation(
	ownedMemberParameter = ownedMemberParameter,
	owningType = owningType,
	elementType = elementType,
	parameterDirection = OUT,
	parameterIndex = parameterIndex
) {
	override var parameterDirection: Feature.FeatureDirectionKind
		get() = OUT
		set(value) {
			if(value != OUT)
				throw IllegalArgumentException("Direction of return parameter must be OUT")
		}

	override fun clone() =  ReturnParameterMembershipImplementation(
		ownedMemberParameter = ownedMemberParameter,
		owningType = owningType,
		parameterIndex = parameterIndex
	).also {
		it.updateFrom(this)
	}
}