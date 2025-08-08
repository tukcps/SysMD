package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.FeatureMembership
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedFeature
import com.github.tukcps.sysmd.model.kerml.UnresolvedType

open class FeatureMembershipImplementation(
    ownedMemberFeature: Feature = UnresolvedFeature(),
    owningType: Type = UnresolvedType(),
    elementType: String = "FeatureMembership",
): FeatureMembership, OwningMembershipImplementation(
    memberElement = ownedMemberFeature,
    membershipOwningNamespace = owningType,
    elementType = elementType,
)