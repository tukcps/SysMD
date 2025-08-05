package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.StructureImplementation
import com.github.tukcps.sysmd.model.sysml.PartDefinition

class PartDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "PartDefinition",
) : PartDefinition, StructureImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType) {

    override fun clone(): PartDefinitionImplementation = PartDefinitionImplementation()
        .also { klon -> klon.updateFrom(this) }
}