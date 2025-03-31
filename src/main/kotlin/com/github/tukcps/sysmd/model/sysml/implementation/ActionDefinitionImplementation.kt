package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.ActionDefinition
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.UUID

class ActionDefinitionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
): ActionDefinition, DefinitionImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
){
    override fun clone(): ActionDefinition {
        val klon = ActionDefinitionImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { klon ->
            klon.model = model
            klon.updated = updated
        }
        return klon
    }
}
