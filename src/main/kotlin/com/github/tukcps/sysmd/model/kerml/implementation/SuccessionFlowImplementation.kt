package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.SuccessionFlow
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: SuccessionImplementation. */
class SuccessionFlowImplementation(model : Session,elementId : Uuid = Uuid.random())
    : SuccessionFlow, SuccessionImplementation(model,elementId = elementId)
