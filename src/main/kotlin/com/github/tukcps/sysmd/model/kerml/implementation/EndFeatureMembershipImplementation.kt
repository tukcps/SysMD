package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.UnresolvedFeature
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class EndFeatureMembershipImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    owningType: Type = UnresolvedType(model),
    ownedMemberFeature: Feature = UnresolvedFeature(model),
    elementType: String = "EndFeatureMembership",
): EndFeatureMembership, FeatureMembershipImplementation(
    model,
    elementId = elementId,
    owningType = owningType,
    ownedMemberFeature = ownedMemberFeature,
    elementType = elementType,
) {
    override fun clone(): EndFeatureMembership = EndFeatureMembershipImplementation(
        model,
        owningType = owningType,
        ownedMemberFeature = ownedMemberFeature,
    ).also {
        it.updateFrom(this)
    }
}