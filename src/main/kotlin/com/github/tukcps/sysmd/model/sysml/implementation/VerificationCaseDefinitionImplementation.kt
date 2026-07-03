package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.VerificationCaseDefinition

class VerificationCaseDefinitionImplementation(
    declaredName: String? = null,
    declaredSimpleName: String? = null,
    elementType: String = "VerificationCaseDefinition",
): VerificationCaseDefinition, CalculationDefinitionImplementation(elementType = elementType)