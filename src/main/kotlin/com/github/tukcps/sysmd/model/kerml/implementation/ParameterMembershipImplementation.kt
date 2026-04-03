package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*

open class ParameterMembershipImplementation(
	ownedMemberParameter: Feature = UnresolvedFeature(),
	owningType: Type = UnresolvedFeature(),
	elementType: String = "ParameterMembership",
	override val parameterDirection : Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
	override var parameterIndex : Int = -1,
) : ParameterMembership, FeatureMembershipImplementation(
	ownedMemberFeature = ownedMemberParameter,
	owningType = owningType,
	elementType = elementType,
) {

	override fun clone() = ParameterMembershipImplementation(
		ownedMemberParameter = ownedMemberParameter,
		owningType = membershipOwningNamespace as Feature,
		elementType = elementType,
		parameterDirection = parameterDirection
	).also {
		it.updateFrom(this)
	}

	override fun updateFrom(template: Element) {
		super.updateFrom(template)

		if(template is ParameterMembership)
			this.parameterIndex = template.parameterIndex
	}

	override fun toString() = "[ParameterMembership] ${membershipOwningNamespace.escapedName()} owns ${memberElement.escapedName()}"
}
