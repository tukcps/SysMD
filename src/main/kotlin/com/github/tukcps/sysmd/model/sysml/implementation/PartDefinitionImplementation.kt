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

    override fun clone(): PartDefinitionImplementation {
        val klon = PartDefinitionImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { klon ->
            klon.model = model
            klon.updated = updated
        }
        return klon
    }
}