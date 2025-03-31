package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.implementation.AssociationImplementation
import com.github.tukcps.sysmd.model.sysml.ConnectionDefinition
import java.util.UUID

open class ConnectionDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "ConnectionDefinition"
) : ConnectionDefinition, AssociationImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    override fun clone(): ConnectionDefinition = ConnectionDefinitionImplementation(
        declaredName = this.declaredName,
        declaredShortName = this.declaredShortName,
    ).also {
        source = Resolved.copyOfIdentityList(source)
        target = Resolved.copyOfIdentityList(target)
    }
}