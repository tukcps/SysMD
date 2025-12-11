@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.RequirementDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.RequirementUsageImplementation


class RequirementUsageActions(
    context: ActionsContext
): FeatureActions<RequirementUsageImplementation>(
    context,
    creator = ::RequirementUsageImplementation,
    defaultType = "Requirements::RequirementUsage",
)

class RequirementDefinitionActions(
    context: ActionsContext,
): TypeActions<RequirementDefinitionImplementation>(
    context,
    creator = ::RequirementDefinitionImplementation,
    isImplicit = "Requirements::RequirementDefinition",
)

class RequirementConstraintUsageActions(
    context: ActionsContext,
): FeatureActions<Feature>(
    context,
    creator = ::FeatureImplementation,
    defaultType = "Requirements::RequirementUsage",
) {
    override fun finish() {
        context.addTyping("ScalarValues::Boolean")
        super.finish()
    }
}

class RequirementAssumeUsageActions(
    context: ActionsContext,
): FeatureActions<Feature>(
    context,
    creator = ::FeatureImplementation,
    defaultType =  "Requirements::SatisfyRequirementUsage",
) {
    override fun finish() {
        context.addTyping("ScalarValues::Boolean")
        super.finish()
    }
}