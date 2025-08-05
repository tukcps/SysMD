package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.BindingConnector
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Relationship

class BindingConnectorImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "BindingConnector",
): BindingConnector, ConnectorImplementation(
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        elementType = elementType
    ) {

    override fun clone(): BindingConnector =
        BindingConnectorImplementation().also { klon -> klon.updateFrom(this) }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is Relationship) {
            target = template.target.toMutableList()
            source = template.source.toMutableList()
        }
    }
}