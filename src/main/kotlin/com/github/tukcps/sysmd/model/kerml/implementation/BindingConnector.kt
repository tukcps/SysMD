package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.BindingConnector
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class BindingConnectorImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
): BindingConnector, ConnectorImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    override fun clone(): BindingConnector = BindingConnectorImplementation(model).also { klon ->
        klon.updateFrom(this)
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is Relationship) {
            target = template.target.toMutableList()
            source = template.source.toMutableList()
        }
    }
}