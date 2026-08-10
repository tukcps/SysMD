package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.PerformActionUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: EventOccurrenceUsageImplementation. */
class PerformActionUsageImplementation(model : Session,elementId : Uuid = Uuid.random())
    : PerformActionUsage, EventOccurrenceUsageImplementation(model,elementId = elementId)
{
    override val performedAction: ActionUsage = TODO()
    override fun namingFeature(): Feature? = TODO()
}
