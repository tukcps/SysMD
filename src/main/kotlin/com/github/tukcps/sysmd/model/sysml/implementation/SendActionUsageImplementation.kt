package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.sysml.SendActionUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: ActionUsageImplementation. */
class SendActionUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    SendActionUsage,
    ActionUsageImplementation(model,elementId = elementId)
{
    
    override val payloadArgument: Expression = TODO()
    override val receiverArgument: Expression? = TODO()
    override val senderArgument: Expression? = TODO()
    
}
