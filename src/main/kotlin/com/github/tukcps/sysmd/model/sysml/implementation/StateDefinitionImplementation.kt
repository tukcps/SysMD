package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.StateDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class StateDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    StateDefinition,
    ActionDefinitionImplementation(model,elementId = elementId)