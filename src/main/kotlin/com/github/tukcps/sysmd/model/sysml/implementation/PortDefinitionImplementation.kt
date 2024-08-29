package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.sysml.PortDefinition
import java.util.*

class PortDefinitionImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    elementType: String = "PortDefinition"
): PortDefinition, ClassImplementation(
    elementId, declaredName, declaredShortName, ownedElement, owner, elementType
) {

    override fun clone(): PortDefinitionImplementation {
        val klon = PortDefinitionImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            ownedElement = Resolved.copyOfIdentityList(ownedElement),
            owner = Resolved(owner)
        ).also { klon ->
            klon.model = model
            klon.updated = updated
        }
        return klon
    }
}