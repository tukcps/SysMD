package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.AssociationActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.ConnectorActions
import com.github.tukcps.sysmd.model.sysml.ConnectionDefinition
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.model.util.SimpleName


open class ConnectionUsageActions<T: ConnectionUsage>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?)->T,
    defaultType: MutableList<QualifiedName> = mutableListOf("Connections::Connection"),
): ConnectorActions<T>(
    context = context,
    creator = creator,
    defaultType = defaultType,
)


open class ConnectionDefinitionActions<T: ConnectionDefinition>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: MutableList<QualifiedName> = mutableListOf("Connections::Connection"),
): AssociationActions<ConnectionDefinition>(
    context = context,
    creator = creator,
    specializes
)