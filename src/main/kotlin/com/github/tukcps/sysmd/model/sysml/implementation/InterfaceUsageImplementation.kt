package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.InterfaceUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class InterfaceUsageImplementation(model : Session,elementId : Uuid = Uuid.random())
    : InterfaceUsage, ConnectionUsageImplementation(model,elementId = elementId)
{
    override fun clone(): InterfaceUsage = InterfaceUsageImplementation(model).also {
        it.updateFrom(this)
    }
}