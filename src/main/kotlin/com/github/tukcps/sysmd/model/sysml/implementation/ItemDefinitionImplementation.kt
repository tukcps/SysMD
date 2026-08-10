package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.sysml.ItemDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class ItemDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random())
    : ItemDefinition, ClassImplementation(model,elementId = elementId)
{
    override fun clone(): ItemDefinition = ItemDefinitionImplementation(model).also {
        it.updateFrom(this)
    }
}