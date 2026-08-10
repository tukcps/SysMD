package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.sysml.AcceptActionUsage
import com.github.tukcps.sysmd.model.sysml.ReferenceUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class AcceptActionUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random()
) : AcceptActionUsage, ActionUsageImplementation(model,elementId = elementId)
{
    override val payloadParameter : ReferenceUsage?
        get() = getOwnedElementOfType<ReferenceUsage>()

    override fun clone(): AcceptActionUsage = AcceptActionUsageImplementation(model).also { klon ->
        klon.isComposite = isComposite
    }
}