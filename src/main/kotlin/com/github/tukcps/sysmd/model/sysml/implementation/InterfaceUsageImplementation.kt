package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.InterfaceUsage

class InterfaceUsageImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "InterfaceUsage",
): InterfaceUsage, ConnectionUsageImplementation(
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