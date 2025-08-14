package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Intersecting
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedNamespace

class IntersectingImplementation(
    typeIntercected: Type,
    intersectingType: Type,
    owningRelatedElement: Element = UnresolvedNamespace(),
    elementType: String = "Intersecting",
): Intersecting, RelationshipImplementation(
    source = mutableListOf(typeIntercected),
    target = mutableListOf(intersectingType),
    owningRelatedElement = owningRelatedElement,
    elementType = elementType
)