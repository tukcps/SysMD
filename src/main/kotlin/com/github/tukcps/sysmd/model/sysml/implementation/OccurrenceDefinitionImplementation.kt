package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.sysml.OccurrenceDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class OccurrenceDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random())
    : OccurrenceDefinition, ClassImplementation(model,elementId = elementId)
{
    override fun clone(): OccurrenceDefinition = OccurrenceDefinitionImplementation(model).also {
        it.updateFrom(this)
    }
}