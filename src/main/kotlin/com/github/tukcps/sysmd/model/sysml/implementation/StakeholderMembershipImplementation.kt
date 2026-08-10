package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ParameterMembershipImplementation
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.sysml.StakeholderMembership
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: ParameterMembershipImplementation. */
class StakeholderMembershipImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    StakeholderMembership ,
    ParameterMembershipImplementation(model,elementId = elementId)
{
    
    override val ownedStakeholderParameter: PartUsage = TODO()
    
}
