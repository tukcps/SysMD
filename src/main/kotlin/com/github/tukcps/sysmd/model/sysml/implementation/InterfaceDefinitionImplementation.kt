package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.InterfaceDefinition
import com.github.tukcps.sysmd.model.sysml.Usage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class InterfaceDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random())
    : InterfaceDefinition, ConnectionDefinitionImplementation(model,elementId = elementId)
{
    override fun clone(): InterfaceDefinition = InterfaceDefinitionImplementation(model).also {
        it.updateFrom(this)
    }

    override val connectionEnd: MutableList<Usage>
        get() = TODO("Not yet implemented")
}