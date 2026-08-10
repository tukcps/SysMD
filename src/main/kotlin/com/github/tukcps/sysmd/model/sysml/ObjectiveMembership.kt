package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.FeatureMembership

interface ObjectiveMembership : FeatureMembership {
    val ownedObjectiveRequirement: RequirementUsage
}
