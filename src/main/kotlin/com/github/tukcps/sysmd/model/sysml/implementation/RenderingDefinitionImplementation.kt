package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.RenderingDefinition
import com.github.tukcps.sysmd.model.sysml.RenderingUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: PartDefinitionImplementation. */
class RenderingDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    RenderingDefinition, PartDefinitionImplementation(model,elementId = elementId)
{
    
    override val rendering: MutableList<RenderingUsage> = TODO()
    
}
