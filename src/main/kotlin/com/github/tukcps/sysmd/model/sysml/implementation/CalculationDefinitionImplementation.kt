package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.CalculationDefinition

class CalculationDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "CalculationDefinition"
): CalculationDefinition, ActionDefinitionImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType
) {

    override fun clone() = CalculationDefinitionImplementation()
        .also { it.updateFrom(this) }
}