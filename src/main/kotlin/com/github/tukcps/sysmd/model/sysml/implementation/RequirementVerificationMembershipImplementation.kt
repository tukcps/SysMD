package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.RequirementConstraintMembership
import com.github.tukcps.sysmd.model.sysml.RequirementUsage
import com.github.tukcps.sysmd.model.sysml.RequirementVerificationMembership
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: RequirementConstraintMembershipImplementation. */
class RequirementVerificationMembershipImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    RequirementVerificationMembership,
    RequirementConstraintMembershipImplementation(model,elementId = elementId)
{
    override var kind: RequirementConstraintMembership.RequirementConstraintKind? = TODO()
    override val ownedRequirement: RequirementUsage = TODO()
    override val verifiedRequirement: RequirementUsage = TODO()
}
