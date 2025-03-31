package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.BindingConnector
import java.util.*

class BindingConnectorImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "BindingConnector",
): BindingConnector, ConnectorImplementation(
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        elementType = elementType
    ) {

    @Suppress("UNCHECKED_CAST")
    override fun clone(): BindingConnector {
        return BindingConnectorImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { klon ->
            klon.model = model
            klon.direction = direction
            klon.updated = updated
            klon.from = Resolved.copyOfIdentityList(from as MutableCollection<Resolved<Element>>) as MutableList<Resolved<Feature>>
            klon.to = Resolved.copyOfIdentityList(to as MutableCollection<Resolved<Element>>) as MutableList<Resolved<Feature>>
        }
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is Relationship) {
            target = Resolved.copyOfIdentityList(template.target)
            source = Resolved.copyOfIdentityList(template.source)
        }
    }

    override fun toString(): String {
        return "BindingConnector { " +
                "name='$declaredName', " +
                ((if(declaredShortName != null)"shortName='$declaredShortName', " else "")) +
                "supertypes='${allSupertypes()}', "+
                "#sources=${source.size}, " +
                "#targets=${target.size}, " +
                "id='${elementId}' }"
    }
}