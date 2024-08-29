package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.sysml.PartDefinition
import java.util.UUID

class PartDefinitionImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "PartDefinition",
) : PartDefinition, ClassImplementation(
    elementId = elementId,
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