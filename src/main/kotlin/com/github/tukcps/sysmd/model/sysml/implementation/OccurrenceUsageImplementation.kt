package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.OccurrenceUsage
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.*

open class OccurrenceUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
    elementType: String = "OccurrenceUsage",
    isEnd : Boolean = false
): OccurrenceUsage, FeatureImplementation(
    declaredName =declaredName,
    declaredShortName =declaredShortName,
    direction =direction,
    elementType =elementType,
    isEnd = isEnd)
{
    override fun clone(): OccurrenceUsage {
        val klon = OccurrenceUsageImplementation(
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