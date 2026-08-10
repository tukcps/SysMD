package com.github.tukcps.sysmd.model.sysml

interface VerificationCaseDefinition: CaseDefinition {
    val verifiedRequirement: MutableList<RequirementUsage>
}