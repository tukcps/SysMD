package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.expression.implementation.BuiltinFunction
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class FunctionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    override val isModelLevelEvaluable : Boolean = false,
): Function, BehaviorImplementation(
    model,
    elementId = elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName
) {
	override var builtin : BuiltinFunction? = null

    override fun clone() = FunctionImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        isModelLevelEvaluable = isModelLevelEvaluable,
    ).also { klon -> klon.updateFrom(this) }
}