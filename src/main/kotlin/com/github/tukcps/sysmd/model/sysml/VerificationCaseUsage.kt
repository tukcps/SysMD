package com.github.tukcps.sysmd.model.sysml

interface VerificationCaseUsage: CaseUsage {
    val verificationCaseDefinition: VerificationCaseDefinition?
    val verifiedRequirement: MutableList<RequirementUsage>
}