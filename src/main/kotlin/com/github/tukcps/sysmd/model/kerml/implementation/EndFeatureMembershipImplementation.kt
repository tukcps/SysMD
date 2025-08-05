package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.EndFeatureMembership

class EndFeatureMembershipImplementation(
    elementType: String = "EndFeatureMembership",
): EndFeatureMembership, FeatureMembershipImplementation(
    elementType = elementType,
)