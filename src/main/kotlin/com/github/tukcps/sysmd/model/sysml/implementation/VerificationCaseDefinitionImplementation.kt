package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.sysml.RequirementUsage
import com.github.tukcps.sysmd.model.sysml.Usage
import com.github.tukcps.sysmd.model.sysml.VerificationCaseDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class VerificationCaseDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    VerificationCaseDefinition,
    CalculationDefinitionImplementation(model,elementId = elementId)
{
    override val verifiedRequirement: MutableList<RequirementUsage>
        get() = TODO("Not yet implemented")
    override val actorParameter: MutableList<PartUsage>
        get() = TODO("Not yet implemented")
    override val objectiveRequirement: RequirementUsage?
        get() = TODO("Not yet implemented")
    override val subjectParameter: Usage
        get() = TODO("Not yet implemented")
}