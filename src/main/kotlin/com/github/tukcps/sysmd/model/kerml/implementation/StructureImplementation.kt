package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Structure
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/**
 * A Class that is an occurrence
 */
open class StructureImplementation(model : Session,elementId : Uuid = Uuid.random())
    : Structure, ClassImplementation(model,elementId = elementId)
{
    override fun clone(): Structure = StructureImplementation(model).also {
        it.updateFrom(this)
    }
}