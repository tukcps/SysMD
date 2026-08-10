package com.github.tukcps.sysmd.model.sysml

interface RequirementVerificationMembership : RequirementConstraintMembership {
    override var kind: RequirementConstraintMembership.RequirementConstraintKind?
    val ownedRequirement: RequirementUsage
    val verifiedRequirement: RequirementUsage
}
