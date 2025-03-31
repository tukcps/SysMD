package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.ConnectorActions
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.UUID

class InterfaceUsageActions<T: ConnectionUsage>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?)->T,
    defaultType: MutableList<QualifiedName> = mutableListOf("Interfaces::Interface"),
): ConnectorActions<T>(
    context = context,
    creator = creator,
    defaultType = defaultType,
)