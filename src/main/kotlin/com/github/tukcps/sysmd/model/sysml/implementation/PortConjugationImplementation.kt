package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ConjugationImplementation
import com.github.tukcps.sysmd.model.sysml.ConjugatedPortDefinition
import com.github.tukcps.sysmd.model.sysml.PortConjugation
import com.github.tukcps.sysmd.model.sysml.PortDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: ConjugationImplementation. */
class PortConjugationImplementation(model : Session,elementId : Uuid = Uuid.random())
    : PortConjugation, ConjugationImplementation(model,elementId = elementId)
{
    
    override val conjugatedPortDefinition: ConjugatedPortDefinition = TODO()
    override var originalPortDefinition: PortDefinition = TODO()
    
}
