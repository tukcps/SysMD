package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Intersecting
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.UnresolvedNamespace
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class IntersectingImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    typeIntersected: Type = UnresolvedType(model),
    intersectingType: Type = UnresolvedType(model),
    owningRelatedElement: Element = UnresolvedNamespace(model),
): Intersecting, RelationshipImplementation(
    model,
    elementId = elementId,
    owningRelatedElement = owningRelatedElement,
    source = mutableListOf(typeIntersected),
    target = mutableListOf(intersectingType),
) {
    override fun clone(): Intersecting = IntersectingImplementation(model)
        .also { it.updateFrom(this) }
}