package com.github.tukcps.sysmd.model.sysml

interface UseCaseUsage : CaseUsage {

    val includedUseCase: MutableList<UseCaseUsage>
    val useCaseDefinition: UseCaseDefinition?

}
