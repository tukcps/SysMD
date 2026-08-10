package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.sysml.WhileLoopActionUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: LoopActionUsageImplementation. */
class WhileLoopActionUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    WhileLoopActionUsage,
    LoopActionUsageImplementation(model,elementId = elementId)
{
    override val untilArgument: Expression? = TODO()
    override val whileArgument: Expression = TODO()
}
