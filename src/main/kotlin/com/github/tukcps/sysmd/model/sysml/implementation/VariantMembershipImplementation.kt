package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.OwningMembershipImplementation
import com.github.tukcps.sysmd.model.sysml.Usage
import com.github.tukcps.sysmd.model.sysml.VariantMembership
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: OwningMembershipImplementation. */
class VariantMembershipImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    VariantMembership, OwningMembershipImplementation(model,elementId = elementId) {
    
    override val ownedVariantUsage: Usage = TODO()
    
}
