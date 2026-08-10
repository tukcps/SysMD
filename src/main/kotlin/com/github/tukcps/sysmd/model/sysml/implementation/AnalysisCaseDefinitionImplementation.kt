package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.sysml.AnalysisCaseDefinition
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.sysml.RequirementUsage
import com.github.tukcps.sysmd.model.sysml.Usage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class AnalysisCaseDefinitionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random()
) : AnalysisCaseDefinition, CaseDefinitionImplementation(model,elementId = elementId)
{
    override val resultExpression: Expression? = TODO()
    override val actorParameter: MutableList<PartUsage>
        get() = TODO("Not yet implemented")
    override val objectiveRequirement: RequirementUsage?
        get() = TODO("Not yet implemented")
    override val subjectParameter: Usage
        get() = TODO("Not yet implemented")
}
