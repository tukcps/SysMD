package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.sysml.RequirementConstraintMember
import com.github.tukcps.sysmd.model.sysml.implementation.RequirementConstraintMemberImplementation

class RequirementConstraintMemberActions(context: ActionsContext)
    : FeatureActions<RequirementConstraintMemberImplementation> (
    context = context,
    creator = ::RequirementConstraintMemberImplementation,
    defaultType = mutableListOf("Constraints::ConstraintUsage")
){
    var kind: RequirementConstraintMember.Kind = RequirementConstraintMember.Kind.REQUIRE

    override fun finish() {
        super.finish()
        if (kind == RequirementConstraintMember.Kind.ASSUME)
            addTypeConstraint(mutableListOf("true"))
    }
}