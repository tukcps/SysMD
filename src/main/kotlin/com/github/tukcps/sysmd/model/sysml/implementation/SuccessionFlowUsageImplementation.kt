package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.SuccessionFlowUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: FlowUsageImplementation. */
class SuccessionFlowUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    SuccessionFlowUsage,
    FlowUsageImplementation(model,elementId = elementId)
