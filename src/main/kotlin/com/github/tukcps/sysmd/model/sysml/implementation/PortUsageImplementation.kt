package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.PortUsage
import com.github.tukcps.sysmd.model.util.SimpleName

class PortUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
    isEnd: Boolean = false,
    elementType: String = "PortUsage"
): PortUsage, FeatureImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    elementType =elementType
) {
    override fun clone(): PortUsage {
        val klon = PortUsageImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            isEnd = isEnd,
            direction = direction
        ).also { klon ->
            klon.model = model
            klon.updated = updated
            klon.isComposite = isComposite
        }
        return klon
    }
}