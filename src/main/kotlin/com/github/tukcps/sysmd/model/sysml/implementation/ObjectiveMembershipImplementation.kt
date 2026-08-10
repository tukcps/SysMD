package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureMembershipImplementation
import com.github.tukcps.sysmd.model.sysml.ObjectiveMembership
import com.github.tukcps.sysmd.model.sysml.RequirementUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: FeatureMembershipImplementation. */
class ObjectiveMembershipImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    ObjectiveMembership, FeatureMembershipImplementation(model,elementId = elementId)
{
    override val ownedObjectiveRequirement: RequirementUsage = TODO()
}
