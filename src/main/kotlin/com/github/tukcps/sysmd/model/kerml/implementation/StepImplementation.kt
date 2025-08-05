package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Step
import com.github.tukcps.sysmd.model.util.SimpleName

open class StepImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "Step"
): Step, FeatureImplementation(
    declaredName =declaredName,
    declaredShortName =declaredShortName,
    elementType = elementType
) {
    override fun clone() = StepImplementation(declaredName, declaredShortName)
        .also { it.updateFrom(this) }
}