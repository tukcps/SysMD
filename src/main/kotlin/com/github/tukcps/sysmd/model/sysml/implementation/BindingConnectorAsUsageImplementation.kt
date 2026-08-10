package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ConnectorImplementation
import com.github.tukcps.sysmd.model.sysml.BindingConnectorAsUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class BindingConnectorAsUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random()
) :
    BindingConnectorAsUsage,
    ConnectorImplementation(model,elementId = elementId) {
}
