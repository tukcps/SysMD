@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.model.sysml.implementation.StateUsageImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.TransitionUsageImplementation


open class StateUsageActions(context: ActionsContext ): FeatureActions<StateUsage>(
    context = context,
    creator = ::StateUsageImplementation,
    defaultType = "States::StateAction"
)


class TransitionUsageActions(context: ActionsContext): FeatureActions<TransitionUsageImplementation>(
    context = context,
    creator = ::TransitionUsageImplementation,
    defaultType = "Occurrences::Occurrence",
)