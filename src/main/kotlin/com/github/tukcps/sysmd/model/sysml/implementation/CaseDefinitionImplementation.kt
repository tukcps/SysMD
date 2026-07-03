package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.CaseDefinition

class CaseDefinitionImplementation(
    elementType: String = "CaseDefinition",
): CaseDefinition, CalculationDefinitionImplementation(
    elementType = elementType,
)