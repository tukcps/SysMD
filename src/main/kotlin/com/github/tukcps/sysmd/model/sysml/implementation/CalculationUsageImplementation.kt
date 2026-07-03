package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.CalculationUsage

open class CalculationUsageImplementation(
    elementType: String = "CalculationUsage",
): CalculationUsage, ActionUsageImplementation(
    elementType = elementType,
)