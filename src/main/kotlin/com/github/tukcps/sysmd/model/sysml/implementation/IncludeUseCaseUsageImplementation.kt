package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.sysml.*
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class IncludeUseCaseUsageImplementation(model : Session,elementId : Uuid = Uuid.random())
    : IncludeUseCaseUsage, CalculationUsageImplementation(model,elementId = elementId)
{
    
    override val useCaseIncluded: UseCaseUsage = TODO()
    override val performedAction: ActionUsage
        get() = TODO("Not yet implemented")

    override fun namingFeature(): Feature? {
        TODO("Not yet implemented")
    }

    override val includedUseCase: MutableList<UseCaseUsage>
        get() = TODO("Not yet implemented")
    override val useCaseDefinition: UseCaseDefinition?
        get() = TODO("Not yet implemented")
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
