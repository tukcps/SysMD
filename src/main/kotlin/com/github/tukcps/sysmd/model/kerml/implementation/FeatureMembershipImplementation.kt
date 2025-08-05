package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.FeatureMembership

open class FeatureMembershipImplementation(
    elementType: String = "FeatureMembership",
): FeatureMembership, OwningMembershipImplementation(
    elementType = elementType,
)