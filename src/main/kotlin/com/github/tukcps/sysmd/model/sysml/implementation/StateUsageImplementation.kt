package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class StateUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
): StateUsage, ActionUsageImplementation(
    model,
    elementId = elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName
){
        override fun clone(): StateUsage {
            val klon = StateUsageImplementation(
                model,
                declaredName = declaredName,
                declaredShortName = declaredShortName,
            ).also { klon ->
                klon.updated = updated
                klon.isComposite = isComposite
            }
            return klon
        }
    }
