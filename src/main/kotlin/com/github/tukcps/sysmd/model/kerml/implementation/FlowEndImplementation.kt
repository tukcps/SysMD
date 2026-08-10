package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.FlowEnd
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: FeatureImplementation. */
class FlowEndImplementation(model : Session,elementId : Uuid = Uuid.random())
    : FlowEnd, FeatureImplementation(model,elementId = elementId)
