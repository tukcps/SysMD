package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.expression.implementation.BuiltinFunction
import com.github.tukcps.sysmd.model.kerml.Function

open class FunctionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "Function",
    override val isModelLevelEvaluable : Boolean = false,
): Function, BehaviorImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType
) {
	override var builtin : BuiltinFunction? = null

    override fun clone(): FunctionImplementation =
        FunctionImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
			isModelLevelEvaluable = isModelLevelEvaluable,
        ).also { klon -> updateFrom(this) }
}