package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.EnumerationDefinition
import com.github.tukcps.sysmd.model.sysml.EnumerationUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class EnumerationDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    EnumerationDefinition,
    AttributeDefinitionImplementation(model,elementId = elementId)
{
    val enumeratedValue: MutableList<EnumerationUsage> = TODO()
    override var isVariation: Boolean? = TODO()
}
