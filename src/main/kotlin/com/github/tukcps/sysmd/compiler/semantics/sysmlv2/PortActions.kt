@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeAction
import com.github.tukcps.sysmd.model.generated.ElementType

class PortDefinitionAction(
    context: ActionsContext,
): TypeAction(
    context,
    type = ElementType.PortDefinition,
    isImplicit = "Ports::Port",
)

class PortUsageAction(
    context: ActionsContext,
): FeatureAction(
    context,
    type = ElementType.PortUsage,
    isImplicit = "Ports::Port",
)