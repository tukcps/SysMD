package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.sysml.*
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class CaseUsageImplementation(model : Session,elementId : Uuid = Uuid.random())
    : CaseUsage, CalculationUsageImplementation(model,elementId = elementId)
{
    override val actorParameter: MutableList<PartUsage>
        get() = TODO("Not yet implemented")
    override val caseDefinition: CaseDefinition?
        get() = TODO("Not yet implemented")
    override val objectiveRequirement: RequirementUsage?
        get() = TODO("Not yet implemented")
    override val subjectParameter: Usage
        get() = TODO("Not yet implemented")
    override val calculationDefinition: Function?
        get() = TODO("Not yet implemented")

    override fun modelLevelEvaluable(visited: Set<Feature>): Boolean {
        TODO("Not yet implemented")
    }

}