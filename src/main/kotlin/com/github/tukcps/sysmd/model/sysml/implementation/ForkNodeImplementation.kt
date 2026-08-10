package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.ForkNode
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ForkNodeImplementation(model : Session,elementId : Uuid = Uuid.random())
    : ForkNode, ControlNodeImplementation(model,elementId = elementId)
