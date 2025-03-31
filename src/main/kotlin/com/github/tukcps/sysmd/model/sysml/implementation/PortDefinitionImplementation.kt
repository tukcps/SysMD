package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.StructureImplementation
import com.github.tukcps.sysmd.model.sysml.PortDefinition

class PortDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "PortDefinition"
): PortDefinition, StructureImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {

    override fun clone(): PortDefinitionImplementation {
        val klon = PortDefinitionImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { klon ->
            klon.model = model
            klon.updated = updated
        }
        return klon
    }
}