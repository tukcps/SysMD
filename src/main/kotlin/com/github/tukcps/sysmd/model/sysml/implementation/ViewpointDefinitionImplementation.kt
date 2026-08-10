package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.sysml.ViewpointDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: RequirementDefinitionImplementation. */
class ViewpointDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    ViewpointDefinition,
    RequirementDefinitionImplementation(model,elementId = elementId)
{
    override val viewpointStakeholder: MutableList<PartUsage> = TODO()
}
