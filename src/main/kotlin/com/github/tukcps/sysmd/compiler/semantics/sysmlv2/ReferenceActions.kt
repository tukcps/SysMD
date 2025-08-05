@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.sysml.implementation.ReferenceUsageImplementation


abstract class ReferenceUsageActions(
    context: ActionsContext,
) : FeatureActions<ReferenceUsageImplementation>(
    context,
    creator = ::ReferenceUsageImplementation,
)

class PayloadParameterActions(
    context: ActionsContext,
) : ReferenceUsageActions(
    context,
)