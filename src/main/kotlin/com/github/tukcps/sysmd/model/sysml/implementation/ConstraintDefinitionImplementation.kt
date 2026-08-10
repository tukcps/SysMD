package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.PredicateImplementation
import com.github.tukcps.sysmd.model.sysml.ConstraintDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ConstraintDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random())
    : ConstraintDefinition, PredicateImplementation(model,elementId = elementId)
