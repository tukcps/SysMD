package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.DataTypeImplementation
import com.github.tukcps.sysmd.model.sysml.AttributeDefinition

class AttributeDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "AttributeDefinition"
): AttributeDefinition, DataTypeImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType
) {
    override fun clone(): AttributeDefinition =
        AttributeDefinitionImplementation(
            declaredName=declaredName,
            declaredShortName=declaredShortName,
        ).also { it.updateFrom(this) }
}