package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.util.SimpleName

class PartUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "PartUsage",
):
    PartUsage, FeatureImplementation(
    declaredName =declaredName,
    declaredShortName =declaredShortName,
    elementType =elementType) {
    override fun clone(): PartUsage = PartUsageImplementation(
            declaredName = declaredName, declaredShortName = declaredShortName,
        ).also { klon -> klon.updateFrom(this) }
}