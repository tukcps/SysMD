package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.PortUsage
import com.github.tukcps.sysmd.model.util.SimpleName

class PortUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "PortUsage"
): PortUsage, FeatureImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType =elementType
) {
    override fun clone(): PortUsage {
        val klon = PortUsageImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { klon ->
            super.updateFrom(this)
            klon.model = model
            klon.updated = updated
            klon.isComposite = isComposite
        }
        return klon
    }
}