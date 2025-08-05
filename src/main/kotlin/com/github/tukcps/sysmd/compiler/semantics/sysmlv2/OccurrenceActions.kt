package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.model.sysml.implementation.OccurrenceDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.OccurrenceUsageImplementation


class OccurrenceDefinitionActions(
    context: ActionsContext,
): TypeActions<OccurrenceDefinitionImplementation>(
    context,
    creator = ::OccurrenceDefinitionImplementation,
    defaultType = "Occurrences::Occurrence",
)


class OccurrenceUsageActions(
    context: ActionsContext,
) : FeatureActions<OccurrenceUsageImplementation>(
    context,
    creator = ::OccurrenceUsageImplementation,
    defaultType = "Occurrences::Occurrence",
)