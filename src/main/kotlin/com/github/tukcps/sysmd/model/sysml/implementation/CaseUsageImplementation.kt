package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.CaseUsage

class CaseUsageImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "CaseUsage",
): CaseUsage, CalculationUsageImplementation(elementType)