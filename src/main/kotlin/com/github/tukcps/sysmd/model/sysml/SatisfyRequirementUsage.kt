package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Feature

interface SatisfyRequirementUsage : AssertConstraintUsage, RequirementUsage {
    val satisfiedRequirement: RequirementUsage
    val satisfyingFeature: Feature
}
