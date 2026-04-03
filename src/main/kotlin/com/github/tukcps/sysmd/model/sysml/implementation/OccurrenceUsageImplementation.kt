package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.OccurrenceUsage
import com.github.tukcps.sysmd.model.util.SimpleName

open class OccurrenceUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "OccurrenceUsage",
): OccurrenceUsage, FeatureImplementation(
    declaredName =declaredName,
    declaredShortName =declaredShortName,
    elementType =elementType)
{
    override fun clone(): OccurrenceUsage {
        val klon = OccurrenceUsageImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { super.updateFrom(this) }
        return klon
    }
}