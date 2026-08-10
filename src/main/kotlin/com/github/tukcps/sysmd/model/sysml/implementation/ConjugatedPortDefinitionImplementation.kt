package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.ConjugatedPortDefinition
import com.github.tukcps.sysmd.model.sysml.PortConjugation
import com.github.tukcps.sysmd.model.sysml.PortDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ConjugatedPortDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    ConjugatedPortDefinition, PortDefinitionImplementation(model,elementId = elementId)
{
    override val originalPortDefinition: PortDefinition = TODO()
    override val ownedPortConjugator: PortConjugation = TODO()
    
    override fun effectiveName(): String? = TODO()
}
