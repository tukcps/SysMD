package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.sysml.RequirementConstraintMember
import com.github.tukcps.sysmd.model.sysml.implementation.RequirementConstraintMemberImplementation

class RequirementConstraintMemberActions(
    context: ActionsContext,
) : FeatureActions<RequirementConstraintMemberImplementation> (
    context = context,
    creator = ::RequirementConstraintMemberImplementation,
    defaultType = "Constraints::ConstraintUsage",
){
    override fun finish() {
        if (created.type.isEmpty()) {
            context.addTyping("ScalarValues::Boolean")
            if (isImplicit != null) context.addTyping(isImplicit!!)
        }
        if (created.kind == RequirementConstraintMember.Kind.ASSUME)
            context.addTypeConstraint(mutableListOf("true"))
        super.finish()
    }
}