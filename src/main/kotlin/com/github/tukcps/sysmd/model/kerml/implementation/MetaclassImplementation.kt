package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Metaclass
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class MetaclassImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    Metaclass,
    StructureImplementation(model,elementId = elementId)
{
    override fun clone(): MetaclassImplementation = MetaclassImplementation(model).also {
        it.updateFrom(this)
    }
}