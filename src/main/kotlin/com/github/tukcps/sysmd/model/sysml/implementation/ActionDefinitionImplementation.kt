package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.ActionDefinition
import com.github.tukcps.sysmd.model.util.SimpleName

open class ActionDefinitionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "ActionDefinition",
): ActionDefinition, DefinitionImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType = elementType,
){
    override fun clone(): ActionDefinition =
        ActionDefinitionImplementation().also { klon -> klon.updateFrom(this) }
}
