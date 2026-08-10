package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Feature.FeatureDirectionKind.OUT
import com.github.tukcps.sysmd.model.kerml.ReturnParameterMembership
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.UnresolvedFeature
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ReturnParameterMembershipImplementation(
	model: Session,
	elementId : Uuid = Uuid.random(),
	ownedMemberParameter: Feature = UnresolvedFeature(model),
	owningType: Type = UnresolvedFeature(model),
	elementType: String = "ReturnParameterMembership",
	parameterIndex : Int = -1
) : ReturnParameterMembership, ParameterMembershipImplementation(
	model,
	elementId = elementId,
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
		model,
		ownedMemberParameter = ownedMemberParameter,
		owningType = owningType,
		parameterIndex = parameterIndex
	).also {
		it.updateFrom(this)
	}
}