package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.model.sysml.RequirementDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class RequirementDefinitionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
): RequirementDefinition, TypeImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    override fun clone(): RequirementDefinition  = RequirementDefinitionImplementation(model)
        .also { it.updateFrom(this) }
}