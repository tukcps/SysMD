package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class ActionUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
) : ActionUsage, OccurrenceUsageImplementation(
    model,
    elementId = elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName
) {
    override fun clone(): ActionUsage = ActionUsageImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
    ).also { klon ->
        klon.updateFrom(this)
        klon.isComposite = isComposite
    }
}