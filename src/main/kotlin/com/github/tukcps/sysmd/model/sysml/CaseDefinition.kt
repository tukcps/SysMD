package com.github.tukcps.sysmd.model.sysml

interface CaseDefinition: CalculationDefinition  {

    val actorParameter: MutableList<PartUsage>
    val objectiveRequirement: RequirementUsage?
    val subjectParameter: Usage

}