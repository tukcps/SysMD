package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.sysml.AllocationUsage
import java.util.UUID

open class AllocationUsageImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "AllocationUsage"
) : AllocationUsage, ConnectionUsageImplementation(
    elementType = elementType,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementId = elementId
) {
    override fun clone(): AllocationUsageImplementation {
        return AllocationUsageImplementation(
            declaredName = this.declaredName,
            declaredShortName = this.declaredShortName,
        ).also {
            it.model = model
            source = Resolved.copyOfIdentityList(source)
            target = Resolved.copyOfIdentityList(target)
        }
    }
}