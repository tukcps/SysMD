package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.ConcernDefinition
import com.github.tukcps.sysmd.model.sysml.ConcernUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ConcernUsageImplementation(model : Session,elementId : Uuid = Uuid.random())
     : ConcernUsage, RequirementUsageImplementation(model,elementId = elementId)
{
     override val concernDefinition: ConcernDefinition? = TODO()
}
