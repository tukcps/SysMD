package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.RequirementConstraintMember
import com.github.tukcps.sysmd.model.util.SimpleName

class RequirementConstraintMemberImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: String? = null,
    elementType: String = "RequirementConstraintMember",
): RequirementConstraintMember, FeatureImplementation(
    declaredName,
    declaredShortName,
    elementType = elementType
) {
    override var kind: RequirementConstraintMember.Kind = RequirementConstraintMember.Kind.ASSUME
    override fun clone() = RequirementConstraintMemberImplementation().also { it.updateFrom(this) }
}