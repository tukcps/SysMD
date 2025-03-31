package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.DataTypeImplementation
import com.github.tukcps.sysmd.model.sysml.AttributeDefinition
import java.util.*

class AttributeDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "AttributeDefinition"
): AttributeDefinition, DataTypeImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType
) {
    override fun clone(): AttributeDefinition {
        return AttributeDefinitionImplementation(
            declaredName=declaredName,
            declaredShortName=declaredShortName,
        ).also {
            it.model = model
            it.updated = updated
        }
    }
}