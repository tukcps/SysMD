package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureMembershipImplementation
import com.github.tukcps.sysmd.model.sysml.ConstraintUsage
import com.github.tukcps.sysmd.model.sysml.RequirementConstraintMembership
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: FeatureMembershipImplementation. */
open class RequirementConstraintMembershipImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    RequirementConstraintMembership,
    FeatureMembershipImplementation(model,elementId = elementId)
{
    override var kind: RequirementConstraintMembership.RequirementConstraintKind? = null
    override val ownedConstraint: ConstraintUsage = target.first() as ConstraintUsage
    override val referencedConstraint: ConstraintUsage = target.first() as ConstraintUsage
}
