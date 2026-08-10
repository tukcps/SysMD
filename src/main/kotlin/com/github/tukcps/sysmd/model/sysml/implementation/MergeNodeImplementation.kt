package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.MergeNode
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class MergeNodeImplementation(model : Session,elementId : Uuid = Uuid.random())
    : MergeNode, ControlNodeImplementation(model,elementId = elementId)

