package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.ReferenceUsage
import com.github.tukcps.sysmd.model.util.SimpleName

class ReferenceUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "ReferenceUsage"
): ReferenceUsage, FeatureImplementation(
    declaredName =declaredName,
    declaredShortName =declaredShortName,
    elementType =elementType
){
    override fun clone(): ReferenceUsage = ReferenceUsageImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { updateFrom(this) }
}
