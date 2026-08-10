package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ConnectorImplementation
import com.github.tukcps.sysmd.model.sysml.SuccessionAsUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class SuccessionAsUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    SuccessionAsUsage,
    ConnectorImplementation(model,elementId = elementId)
{
    override fun clone(): SuccessionAsUsage = SuccessionAsUsageImplementation(model)
        .also { klon -> klon.updateFrom(this) }
}