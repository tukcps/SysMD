package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.EventOccurrenceUsage
import com.github.tukcps.sysmd.model.sysml.OccurrenceUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class EventOccurrenceUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    EventOccurrenceUsage,
    OccurrenceUsageImplementation(model,elementId = elementId)
{
    
    val eventOccurrence: OccurrenceUsage = TODO()
    override val isReference: Boolean = TODO()
    
}
