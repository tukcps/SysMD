package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.RenderingDefinition
import com.github.tukcps.sysmd.model.sysml.RenderingUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: PartUsageImplementation. */
class RenderingUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    RenderingUsage, PartUsageImplementation(model,elementId = elementId)
{
    
    override val renderingDefinition: RenderingDefinition? = TODO()
    
}
