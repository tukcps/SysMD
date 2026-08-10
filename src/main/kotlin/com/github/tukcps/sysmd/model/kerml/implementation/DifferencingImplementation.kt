package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Differencing
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.UnresolvedNamespace
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class DifferencingImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    typeDifferenced: Type = UnresolvedType(model),
    differencingType: Type = UnresolvedType(model),
    owningRelatedElement: Element = UnresolvedNamespace(model),
): Differencing, RelationshipImplementation(
    model,
    elementId = elementId,
    owningRelatedElement = owningRelatedElement,
    source = mutableListOf(typeDifferenced),
    target = mutableListOf(differencingType),
) {
    override fun clone(): Differencing = DifferencingImplementation(model).also { it.updateFrom(this) }
}