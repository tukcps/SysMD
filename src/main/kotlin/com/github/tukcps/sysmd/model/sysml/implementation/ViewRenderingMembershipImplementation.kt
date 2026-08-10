package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureMembershipImplementation
import com.github.tukcps.sysmd.model.sysml.RenderingUsage
import com.github.tukcps.sysmd.model.sysml.ViewRenderingMembership
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: FeatureMembershipImplementation. */
class ViewRenderingMembershipImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    ViewRenderingMembership,
    FeatureMembershipImplementation(model,elementId = elementId)
{
    
    override val ownedRendering: RenderingUsage = TODO()
    override val referencedRendering: RenderingUsage = TODO()
    
}
