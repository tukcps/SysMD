package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.CalculationDefinition

open class CalculationDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "CalculationDefinition",
	override val isModelLevelEvaluable: Boolean = false,
): CalculationDefinition, ActionDefinitionImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType
) {
	override val builtin : Nothing? = null

    override fun clone() = CalculationDefinitionImplementation(isModelLevelEvaluable = isModelLevelEvaluable)
        .also { it.updateFrom(this) }
}