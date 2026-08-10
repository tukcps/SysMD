package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureMembershipImplementation
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.StateSubactionMembership
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: FeatureMembershipImplementation. */
class StateSubactionMembershipImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    StateSubactionMembership,
    FeatureMembershipImplementation(model,elementId = elementId)
{
    override val action: ActionUsage = TODO()
    override var kind: StateSubactionMembership.StateSubactionKind? = TODO()
    
}
