package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureMembershipImplementation
import com.github.tukcps.sysmd.model.sysml.ConcernUsage
import com.github.tukcps.sysmd.model.sysml.ConstraintUsage
import com.github.tukcps.sysmd.model.sysml.FramedConcernMembership
import com.github.tukcps.sysmd.model.sysml.RequirementConstraintMembership
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: FeatureMembershipImplementation. */
class FramedConcernMembershipImplementation(model : Session,elementId : Uuid = Uuid.random())
    : FramedConcernMembership, FeatureMembershipImplementation(model,elementId = elementId)
{
    // override var kind: RequirementConstraintKind? = TODO()
    override val ownedConcern: ConcernUsage = TODO()
    override val referencedConcern: ConcernUsage = TODO()
    override var kind: RequirementConstraintMembership.RequirementConstraintKind?
        get() = TODO("Not yet implemented")
        set(value) {}
    override val ownedConstraint: ConstraintUsage
        get() = TODO("Not yet implemented")
    override val referencedConstraint: ConstraintUsage
        get() = TODO("Not yet implemented")
}
