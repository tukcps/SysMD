package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.EndFeatureMembership
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedFeature
import com.github.tukcps.sysmd.model.kerml.UnresolvedType

class EndFeatureMembershipImplementation(
    owningType: Type = UnresolvedType(),
    ownedMemberFeature: Feature = UnresolvedFeature(),
    elementType: String = "EndFeatureMembership",
): EndFeatureMembership, FeatureMembershipImplementation(
    owningType = owningType,
    ownedMemberFeature = ownedMemberFeature,
    elementType = elementType,
)