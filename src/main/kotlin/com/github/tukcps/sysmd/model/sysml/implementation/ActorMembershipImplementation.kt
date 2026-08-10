package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureMembershipImplementation
import com.github.tukcps.sysmd.model.sysml.ActorMembership
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ActorMembershipImplementation(
    model : Session,
    elementId : Uuid = Uuid.random()
) : ActorMembership, FeatureMembershipImplementation(model,elementId = elementId) {
    override val ownedActorParameter: PartUsage = TODO()

    /**
     * (non-standard; reordering ownedElement is non-trivial due to path-based UUIDs)
     * The parameter index for positional arguments only.
     * -1 used as placeholder for named arguments.
     * Set to correct values after typing pass.
     */
    override var parameterIndex: Int
        get() = TODO("Not yet implemented")
        set(value) {}

    override val parameterDirection: com.github.tukcps.sysmd.model.kerml.Feature.FeatureDirectionKind
        get() = TODO("Not yet implemented")

    override fun clone(): ActorMembership = ActorMembershipImplementation(model)
}
