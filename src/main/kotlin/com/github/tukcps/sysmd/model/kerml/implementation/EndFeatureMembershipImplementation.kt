package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*

class EndFeatureMembershipImplementation(
    owningType: Type = UnresolvedType(),
    ownedMemberFeature: Feature = UnresolvedFeature(),
    elementType: String = "EndFeatureMembership",
): EndFeatureMembership, FeatureMembershipImplementation(
    owningType = owningType,
    ownedMemberFeature = ownedMemberFeature,
    elementType = elementType,
) {
    override fun clone(): EndFeatureMembership = EndFeatureMembershipImplementation(
        owningType = owningType,
        ownedMemberFeature = ownedMemberFeature,
    ).also {
        it.updateFrom(this)
    }
}