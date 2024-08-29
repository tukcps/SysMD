package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.model.sysml.InterfaceUsage
import java.util.UUID

class InterfaceUsageImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "InterfaceUsage",
): InterfaceUsage, ConnectionUsageImplementation(
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType,
){
    override fun clone(): InterfaceUsage {
        return InterfaceUsageImplementation(
            declaredName = this.declaredName,
            declaredShortName = this.declaredShortName,
        ).also {
            it.model = model
        }
    }
}