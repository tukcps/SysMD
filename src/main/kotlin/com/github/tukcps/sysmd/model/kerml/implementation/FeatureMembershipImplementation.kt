package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*

open class FeatureMembershipImplementation(
    ownedMemberFeature: Feature = UnresolvedFeature(),
    owningType: Type = UnresolvedType(),
    elementType: String = "FeatureMembership",
): FeatureMembership, OwningMembershipImplementation(
    memberElement = ownedMemberFeature,
    membershipOwningNamespace = owningType,
    elementType = elementType,
) {
    override fun clone(): FeatureMembership = FeatureMembershipImplementation(
        ownedMemberFeature = ownedMemberFeature,
        owningType = owningType
    ).also {
        it.updateFrom(this)
    }
}