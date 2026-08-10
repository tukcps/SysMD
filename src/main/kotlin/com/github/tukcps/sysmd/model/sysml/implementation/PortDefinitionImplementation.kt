package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.StructureImplementation
import com.github.tukcps.sysmd.model.sysml.PortDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class PortDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random())
    : PortDefinition, StructureImplementation(model,elementId = elementId) {

    override fun clone(): PortDefinitionImplementation = PortDefinitionImplementation(model).also { klon ->
        klon.updateFrom(this)
    }
}