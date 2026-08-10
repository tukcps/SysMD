package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ParameterMembershipImplementation
import com.github.tukcps.sysmd.model.sysml.SubjectMembership
import com.github.tukcps.sysmd.model.sysml.Usage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: ParameterMembershipImplementation. */
class SubjectMembershipImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    SubjectMembership,
    ParameterMembershipImplementation(model,elementId = elementId)
{
    
    override val ownedSubjectParameter: Usage = TODO()
    
}
