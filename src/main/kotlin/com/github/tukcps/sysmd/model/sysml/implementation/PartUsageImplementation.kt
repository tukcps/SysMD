package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.*

class PartUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "PartUsage",
):
    PartUsage, FeatureImplementation(
    declaredName =declaredName,
    declaredShortName =declaredShortName,
    elementType =elementType) {
    override fun clone(): PartUsage {
        val klon = PartUsageImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { klon ->
            klon.model = model
            klon.updated = updated
            klon.isComposite = isComposite
        }
        return klon
    }
}