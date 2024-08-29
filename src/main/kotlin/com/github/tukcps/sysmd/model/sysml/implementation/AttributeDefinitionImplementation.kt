package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.implementation.DataTypeImplementation
import com.github.tukcps.sysmd.model.sysml.AttributeDefinition
import java.util.*

class AttributeDefinitionImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    elementType: String = "AttributeDefinition"
): AttributeDefinition, DataTypeImplementation(
    elementId=elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    ownedElement=ownedElement,
    owner=owner,
    elementType=elementType
) {
    override fun clone(): AttributeDefinition {
        return AttributeDefinitionImplementation(
            declaredName=declaredName,
            declaredShortName=declaredShortName,
            ownedElement=Resolved.copyOfIdentityList(ownedElement),
            owner=Resolved(owner)
        ).also {
            it.model = model
            it.updated = updated
        }
    }
}