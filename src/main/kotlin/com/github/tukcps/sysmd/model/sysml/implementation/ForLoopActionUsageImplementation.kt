package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.ForLoopActionUsage
import com.github.tukcps.sysmd.model.sysml.ReferenceUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: ActionUsageImplementation. */
class ForLoopActionUsageImplementation(model : Session,elementId : Uuid = Uuid.random())
    : ForLoopActionUsage, ActionUsageImplementation(model,elementId = elementId)
{
    val loopVariable: ReferenceUsage = TODO()
    val seqArgument: Expression = TODO()
    override val bodyAction: ActionUsage
        get() = TODO("Not yet implemented")
}
