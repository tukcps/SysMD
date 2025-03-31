package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.sysml.ItemDefinition
import java.util.UUID

class ItemDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "ItemDefinition"
): ItemDefinition, ClassImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    override fun clone(): ItemDefinition {
        return ItemDefinitionImplementation(
            declaredName = this.declaredName,
            declaredShortName = this.declaredShortName,
        ).also {
            it.model = model
        }
    }
}