package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.sysml.AcceptActionUsage
import com.github.tukcps.sysmd.model.sysml.ReferenceUsage
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.*

open class AcceptActionUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "AcceptActionUsage"
):
    AcceptActionUsage, ActionUsageImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType){
        override val payloadParameter : ReferenceUsage?
            get() = getOwnedElementOfType<ReferenceUsage>()
        override fun clone(): AcceptActionUsage {
            val klon = AcceptActionUsageImplementation(
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