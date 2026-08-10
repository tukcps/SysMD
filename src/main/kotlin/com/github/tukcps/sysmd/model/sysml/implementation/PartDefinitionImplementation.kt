package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.StructureImplementation
import com.github.tukcps.sysmd.model.sysml.PartDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class PartDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random())
    : PartDefinition, StructureImplementation(model,elementId = elementId)
{
    override fun clone() = PartDefinitionImplementation(model)
        .also { klon -> klon.updateFrom(this) }
}