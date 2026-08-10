package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.sysml.RequirementConstraintMembership
import com.github.tukcps.sysmd.model.util.TypeConstraint

class RequirementConstraintAction(
    context: ActionsContext,
) : FeatureAction(
    context = context,
    type = ElementType.ConstraintUsage,
    isImplicit = "Constraints::ConstraintUsage",
   // owningMembershipType = ElementType.RequirementConstraintMembership
){
    override fun afterProduction() {

        context.addTyping("ScalarValues::Boolean")

        if (element.requirementConstraintMembershipKind == RequirementConstraintMembership.RequirementConstraintKind.ASSUME)
            context.addTypeConstraint(TypeConstraint(mutableListOf("true"), ""))
        super.afterProduction()
    }
}