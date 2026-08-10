package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.sysml.TerminateActionUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: ActionUsageImplementation. */
class TerminateActionUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    TerminateActionUsage, ActionUsageImplementation(model,elementId = elementId)
{
    
    override val terminatedOccurrenceArgument: Expression? = TODO()
    
}
