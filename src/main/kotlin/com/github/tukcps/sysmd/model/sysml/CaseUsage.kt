package com.github.tukcps.sysmd.model.sysml

interface CaseUsage: CalculationUsage {

    val actorParameter: MutableList<PartUsage>
    val caseDefinition: CaseDefinition?
    val objectiveRequirement: RequirementUsage?
    val subjectParameter: Usage

}