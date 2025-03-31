package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.sysml.InterfaceDefinition

class InterfaceDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "InterfaceDefinition"
) : InterfaceDefinition, ConnectionDefinitionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType =elementType
){
    override fun clone(): InterfaceDefinition {
        return InterfaceDefinitionImplementation(
            declaredName = this.declaredName,
            declaredShortName = this.declaredShortName,
        ).also {
            it.model = model
            source = Resolved.copyOfIdentityList(source)
            target = Resolved.copyOfIdentityList(target)
        }
    }
}