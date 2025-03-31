package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.AssociationActions
import com.github.tukcps.sysmd.model.sysml.AllocationUsage
import com.github.tukcps.sysmd.model.sysml.implementation.AllocationDefinitionImplementation
import com.github.tukcps.sysmd.model.util.SimpleName

class AllocationDefinitionActions(
    context: ActionsContext,
    specializes: MutableList<QualifiedName> = mutableListOf("Allocations::Allocation"),
): AssociationActions<AllocationDefinitionImplementation>(
    context = context,
    creator = ::AllocationDefinitionImplementation,
    specializes = specializes,
)


class AllocationUsageActions<T: AllocationUsage>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: MutableList<QualifiedName> = mutableListOf("Connections::Connection"),
): ConnectionUsageActions<AllocationUsage>(
    context = context,
    creator = creator,
    defaultType = defaultType,
)