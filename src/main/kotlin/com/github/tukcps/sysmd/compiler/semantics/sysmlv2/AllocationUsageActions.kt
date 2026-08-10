@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeAction
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.util.QualifiedName

class AllocationDefinitionAction(
    context: ActionsContext,
): TypeAction(
    context = context,
    type = ElementType.AllocationDefinition,
    isImplicit = "Allocations::Allocation",
)


class AllocationUsageAction(
    context: ActionsContext,
    type: ElementType = ElementType.AllocationUsage,
    isImplicit: QualifiedName = "Connections::Connection",
): ConnectionUsageAction(
    context = context,
    type = type,
    isImplicit = isImplicit,
)