package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.AllocationUsage

open class AllocationUsageImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "AllocationUsage"
) : AllocationUsage, ConnectionUsageImplementation(
    elementType = elementType,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    override fun clone(): AllocationUsageImplementation =
        AllocationUsageImplementation(
            declaredName = this.declaredName,
            declaredShortName = this.declaredShortName,
        ).also { it.updateFrom(this) }
}