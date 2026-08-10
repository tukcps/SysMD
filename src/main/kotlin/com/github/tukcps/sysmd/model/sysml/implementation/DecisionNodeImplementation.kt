package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.DecisionNode
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class DecisionNodeImplementation(model : Session,elementId : Uuid = Uuid.random())
    : DecisionNode, ControlNodeImplementation(model,elementId = elementId)
