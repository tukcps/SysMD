package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.model.util.SimpleName

open class StateUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "StateUsage"
):
    StateUsage, ActionUsageImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType){
        override fun clone(): StateUsage {
            val klon = StateUsageImplementation(
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
