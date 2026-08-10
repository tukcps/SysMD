@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.model.generated.ElementType


open class StateUsageAction(context: ActionsContext ): FeatureAction(
    context = context,
    type = ElementType.StateUsage,
    isImplicit = "States::StateAction"
)


class TransitionUsageAction(context: ActionsContext): FeatureAction(
    context = context,
    type = ElementType.TransitionUsage,
    isImplicit = "Occurrences::Occurrence",
)