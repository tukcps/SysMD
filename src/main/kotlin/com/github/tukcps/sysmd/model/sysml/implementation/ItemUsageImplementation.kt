package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.ItemUsage
import java.util.*

class ItemUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "ItemUsage",
):
    ItemUsage, FeatureImplementation(
    declaredName =declaredName,
    declaredShortName =declaredShortName,
    elementType =elementType) {
    override fun clone(): ItemUsage {
        val klon = ItemUsageImplementation(
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