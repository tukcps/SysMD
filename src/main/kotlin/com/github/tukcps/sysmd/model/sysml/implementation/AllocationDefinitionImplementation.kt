package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.AllocationDefinition

open class AllocationDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "AllocationDefinition"
): AllocationDefinition, ConnectionDefinitionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    override fun clone(): AllocationDefinition =
        AllocationDefinitionImplementation(
            declaredName = this.declaredName,
            declaredShortName = this.declaredShortName,
        ).also { it.updateFrom(this) }
}