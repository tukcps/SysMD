package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.EnumerationDefinition
import com.github.tukcps.sysmd.model.sysml.EnumerationUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class EnumerationUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    EnumerationUsage,
    AttributeUsageImplementation(model,elementId = elementId)
{
    override val enumerationDefinition: EnumerationDefinition = TODO()
}
