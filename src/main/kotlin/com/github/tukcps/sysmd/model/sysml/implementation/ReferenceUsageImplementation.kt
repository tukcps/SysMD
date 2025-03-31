package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.ReferenceUsage
import com.github.tukcps.sysmd.model.util.SimpleName

class ReferenceUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
    isEnd: Boolean = false,
    elementType: String = "ReferenceUsage"
): ReferenceUsage, FeatureImplementation(
    declaredName =declaredName,
    declaredShortName =declaredShortName,
    direction =direction,
    isEnd =isEnd,
    isComposite =false,
    elementType =elementType
){
    override fun clone(): ReferenceUsage {
        val klon = ReferenceUsageImplementation(
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
