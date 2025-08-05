package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Function

open class FunctionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "Function"
): Function, BehaviorImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType
) {
    override fun clone(): FunctionImplementation =
        FunctionImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { klon -> updateFrom(this) }
}