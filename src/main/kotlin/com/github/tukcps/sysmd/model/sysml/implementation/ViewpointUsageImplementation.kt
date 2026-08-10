package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.sysml.ViewpointDefinition
import com.github.tukcps.sysmd.model.sysml.ViewpointUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: RequirementUsageImplementation. */
class ViewpointUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    ViewpointUsage,
    RequirementUsageImplementation(model,elementId = elementId)
{
    
    override val viewpointDefinition: ViewpointDefinition? = TODO()
    override val viewpointStakeholder: MutableList<PartUsage> = TODO()
    
}
