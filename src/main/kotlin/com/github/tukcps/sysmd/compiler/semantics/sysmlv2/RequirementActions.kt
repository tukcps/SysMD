package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.RequirementDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.RequirementUsageImplementation


class RequirementUsageActions(
    context: ActionsContext,
): FeatureActions<RequirementUsageImplementation>(
    context,
    creator = ::RequirementUsageImplementation,
    defaultType = mutableListOf("Requirements::RequirementUsage"),
)

class RequirementDefinitionActions(
    context: ActionsContext
    ): TypeActions<RequirementDefinitionImplementation>(
    context,
    creator = ::RequirementDefinitionImplementation,
    specializes = mutableListOf("Requirements::RequirementDefinition"),
)

class RequirementConstraintUsageActions(
    context: ActionsContext
): FeatureActions<Feature>(
    context,
    creator = ::FeatureImplementation,
    defaultType = mutableListOf("Requirements::RequirementUsage", "ScalarValues::Boolean"),
)

class RequirementAssumeUsageActions(
    context: ActionsContext
): FeatureActions<Feature>(
    context,
    creator = ::FeatureImplementation,
    defaultType =  mutableListOf("Requirements::SatisfyRequirementUsage", "ScalarValues::Boolean"),
)