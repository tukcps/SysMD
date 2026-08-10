package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.FeatureMembership

interface RequirementConstraintMembership : FeatureMembership {
    enum class RequirementConstraintKind { ASSUME, REQUIRE }
    var kind: RequirementConstraintKind?
    val ownedConstraint: ConstraintUsage
    val referencedConstraint: ConstraintUsage
}
