@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeAction
import com.github.tukcps.sysmd.model.generated.ElementType

class RequirementUsageActions(
    context: ActionsContext
): FeatureAction(
    context,
    type = ElementType.RequirementUsage,
    isImplicit = "Requirements::RequirementUsage",
)

class RequirementDefinitionAction(
    context: ActionsContext,
): TypeAction(
    context,
    type = ElementType.RequirementDefinition,
    isImplicit = "Requirements::RequirementDefinition",
)