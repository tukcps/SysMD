package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.LoopActionUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: ActionUsageImplementation. */
open class LoopActionUsageImplementation(model : Session,elementId : Uuid = Uuid.random())
    : LoopActionUsage, ActionUsageImplementation(model,elementId = elementId)
{
    
    override val bodyAction: ActionUsage = TODO()
    
}
