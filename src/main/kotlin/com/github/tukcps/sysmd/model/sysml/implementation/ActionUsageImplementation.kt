package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.util.SimpleName

open class ActionUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "ActionUsage"
):
    ActionUsage, OccurrenceUsageImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType){
        override fun clone(): ActionUsage {
            val klon = ActionUsageImplementation(
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