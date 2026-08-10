package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.ActionDefinition
import com.github.tukcps.sysmd.model.sysml.Definition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class ActionDefinitionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random()
): ActionDefinition, DefinitionImplementation(model,elementId = elementId)
{
    override fun clone(): Definition =ActionDefinitionImplementation(model).also { klon ->
        klon.updateFrom(this)
    }
}
