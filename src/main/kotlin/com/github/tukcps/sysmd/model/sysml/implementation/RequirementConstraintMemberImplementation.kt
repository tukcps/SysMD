package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.RequirementConstraintMember
import com.github.tukcps.sysmd.model.util.SimpleName

class RequirementConstraintMemberImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: String? = null,
): RequirementConstraintMember, FeatureImplementation(
    declaredName,
    declaredShortName
) {
    override var kind: RequirementConstraintMember.Kind = RequirementConstraintMember.Kind.ASSUME
}