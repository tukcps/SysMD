package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeAction
import com.github.tukcps.sysmd.model.generated.ElementType

class OccurrenceDefinitionAction(
    context: ActionsContext,
): TypeAction(
    context,
    type = ElementType.OccurrenceDefinition,
    isImplicit = "Occurrences::Occurrence",
)

class OccurrenceUsageAction(
    context: ActionsContext,
) : FeatureAction(
    context,
    type = ElementType.OccurrenceUsage,
    isImplicit = "Occurrences::Occurrence",
)