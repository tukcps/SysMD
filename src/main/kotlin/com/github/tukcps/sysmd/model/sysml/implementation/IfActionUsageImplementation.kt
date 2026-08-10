package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.IfActionUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class IfActionUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    IfActionUsage, ActionUsageImplementation(model,elementId = elementId)
{
    val elseAction: ActionUsage? = TODO()
    val ifArgument: Expression = TODO()
    val thenAction: ActionUsage = TODO()
}
