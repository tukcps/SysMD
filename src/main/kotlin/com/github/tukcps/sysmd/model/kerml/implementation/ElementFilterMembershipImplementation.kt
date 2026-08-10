package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.kerml.ElementFilterMembership
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ElementFilterMembershipImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    ElementFilterMembership,
    OwningMembershipImplementation(model,elementId = elementId)
{
    val condition: Expression = TODO()
}
