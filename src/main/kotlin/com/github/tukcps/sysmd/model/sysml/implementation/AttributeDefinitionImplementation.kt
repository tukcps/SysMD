package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.DataTypeImplementation
import com.github.tukcps.sysmd.model.sysml.AttributeDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class AttributeDefinitionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
): AttributeDefinition, DataTypeImplementation(
    model,
    elementId = elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
) {
    override fun clone(): AttributeDefinition = AttributeDefinitionImplementation(model).also {
        it.updateFrom(this)
    }

    override var isVariation: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}
}