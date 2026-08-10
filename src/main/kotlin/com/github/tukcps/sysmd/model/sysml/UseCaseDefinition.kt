package com.github.tukcps.sysmd.model.sysml

interface UseCaseDefinition : CaseDefinition {
    val includedUseCase: MutableList<UseCaseUsage>
}
