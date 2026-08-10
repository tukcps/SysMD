package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.generated.elementType
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ParameterMembership
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.UnresolvedFeature
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class ParameterMembershipImplementation(
	model : Session,
	elementId : Uuid = Uuid.random(),
    ownedMemberParameter: Feature = UnresolvedFeature(model),
    owningType: Type = UnresolvedFeature(model),
    elementType: String = "ParameterMembership",
    override val parameterDirection : Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
    override var parameterIndex : Int = -1,
) : ParameterMembership, FeatureMembershipImplementation(
	model,
	elementId = elementId,
	ownedMemberFeature = ownedMemberParameter,
	owningType = owningType,
	elementType = elementType,
) {

	override fun clone() = ParameterMembershipImplementation(
		model,
		ownedMemberParameter = ownedMemberParameter,
		owningType = owningType,
		parameterDirection = parameterDirection
	).also {
		it.updateFrom(this)
	}

	override fun updateFrom(template: Element) {
		super.updateFrom(template)

		if(template is ParameterMembership)
			this.parameterIndex = template.parameterIndex
	}
}
