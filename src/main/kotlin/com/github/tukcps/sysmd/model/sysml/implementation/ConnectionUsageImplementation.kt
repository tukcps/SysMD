package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ConnectorImplementation
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage

open class ConnectionUsageImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "ConnectionUsage"
) : ConnectionUsage, ConnectorImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    override fun clone(): ConnectionUsage = ConnectionUsageImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName)
        .also { it.updateFrom(this) }

}