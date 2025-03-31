package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.model.sysml.RequirementDefinition
import java.util.UUID

class RequirementDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
): RequirementDefinition, TypeImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = "RequirementDefinition"
) {
    override fun toString(): String = "RequirementDefinition '$declaredName'"
    override fun clone(): RequirementDefinition  = RequirementDefinitionImplementation(
        declaredName = declaredName,
        declaredShortName = declaredShortName).also {
        it.model = model
    }

}