@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.ConnectorAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeAction
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.util.QualifiedName

open class ConnectionUsageAction(
    context: ActionsContext,
    type: ElementType = ElementType.ConnectionUsage,
    isImplicit: QualifiedName = "Connections::Connection",
): ConnectorAction(
    context = context,
    type = type,
    isImplicit = isImplicit,
)

open class ConnectionDefinitionAction(
    context: ActionsContext,
    type: ElementType = ElementType.CalculationDefinition,
    isImplicit: QualifiedName = "Connections::Connection",
): TypeAction(
    context = context,
    type = type,
    isImplicit = isImplicit,
)

class SuccessionAsUsageAction(
    context: ActionsContext,
    type: ElementType = ElementType.SuccessionAsUsage,
    isImplicit: QualifiedName = "Occurrences::HappensBefore"
): ConnectorAction(
    context = context,
    type = type,
    isImplicit,
)

