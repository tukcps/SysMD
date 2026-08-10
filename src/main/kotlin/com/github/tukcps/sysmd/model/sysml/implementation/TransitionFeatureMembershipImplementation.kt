package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Step
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureMembershipImplementation
import com.github.tukcps.sysmd.model.sysml.TransitionFeatureMembership
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: FeatureMembershipImplementation. */
class TransitionFeatureMembershipImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    TransitionFeatureMembership,
    FeatureMembershipImplementation(model,elementId = elementId)
{
    override var kind: TransitionFeatureMembership.TransitionFeatureKind? = TODO()
    override val transitionFeature: Step = TODO()
}
