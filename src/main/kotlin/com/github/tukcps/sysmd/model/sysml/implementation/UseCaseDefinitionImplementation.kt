package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.UseCaseDefinition
import com.github.tukcps.sysmd.model.sysml.UseCaseUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: CaseDefinitionImplementation. */
class UseCaseDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    UseCaseDefinition,
    CaseDefinitionImplementation(model,elementId = elementId)
{
    
    override val includedUseCase: MutableList<UseCaseUsage> = TODO()
    
}
