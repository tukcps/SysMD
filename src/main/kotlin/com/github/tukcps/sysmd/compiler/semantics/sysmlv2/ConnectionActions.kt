@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.AssociationActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.ConnectorActions
import com.github.tukcps.sysmd.model.sysml.ConnectionDefinition
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.model.sysml.SuccessionAsUsage
import com.github.tukcps.sysmd.model.util.SimpleName


open class ConnectionUsageActions<T: ConnectionUsage>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?)->T,
    defaultType: QualifiedName = "Connections::Connection",
): ConnectorActions<T>(
    context = context,
    creator = creator,
    defaultType = defaultType,
) {
    override fun finish() {
        if (created.type.isEmpty()) {
            context.addTyping("Connections::Connection")
        }
        super.finish()
    }
}


open class ConnectionDefinitionActions<T: ConnectionDefinition>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: QualifiedName = "Connections::Connection",
): AssociationActions<ConnectionDefinition>(
    context = context,
    creator = creator,
    specializes,
)



class SuccessionAsUsageSemantics<T: SuccessionAsUsage>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: QualifiedName = "Occurrences::HappensBefore",
): ConnectorActions<SuccessionAsUsage>(
    context = context,
    creator = creator,
    specializes,
)

