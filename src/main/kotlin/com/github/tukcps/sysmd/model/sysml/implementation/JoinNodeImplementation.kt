package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.JoinNode
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: ControlNodeImplementation. */
class JoinNodeImplementation(model : Session,elementId : Uuid = Uuid.random()) : JoinNode, ControlNodeImplementation(model,elementId = elementId) {
}
