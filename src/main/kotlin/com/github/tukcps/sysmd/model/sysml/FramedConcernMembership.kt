package com.github.tukcps.sysmd.model.sysml

interface FramedConcernMembership : RequirementConstraintMembership {
    override var kind: RequirementConstraintMembership.RequirementConstraintKind?
    val ownedConcern: ConcernUsage
    val referencedConcern: ConcernUsage
}
