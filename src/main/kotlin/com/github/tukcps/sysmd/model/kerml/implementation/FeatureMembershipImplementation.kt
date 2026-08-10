package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.UnresolvedFeature
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class FeatureMembershipImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    ownedMemberFeature: Feature = UnresolvedFeature(model),
    owningType: Type = UnresolvedType(model),
    elementType: String = "FeatureMembership",
): FeatureMembership, OwningMembershipImplementation(
    model,
    elementId = elementId,
    memberElement = ownedMemberFeature,
    membershipOwningNamespace = owningType,
    elementType = elementType,
) {
    override fun clone(): FeatureMembership = FeatureMembershipImplementation(
        model,
        ownedMemberFeature = ownedMemberFeature,
        owningType = owningType
    ).also {
        it.updateFrom(this)
    }
}