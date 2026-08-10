package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureTypingImplementation
import com.github.tukcps.sysmd.model.sysml.ConjugatedPortDefinition
import com.github.tukcps.sysmd.model.sysml.ConjugatedPortTyping
import com.github.tukcps.sysmd.model.sysml.PortDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ConjugatedPortTypingImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    ConjugatedPortTyping, FeatureTypingImplementation(model,elementId = elementId)
{
    override var conjugatedPortDefinition: ConjugatedPortDefinition = TODO()
    override val portDefinition: PortDefinition = TODO()
}
