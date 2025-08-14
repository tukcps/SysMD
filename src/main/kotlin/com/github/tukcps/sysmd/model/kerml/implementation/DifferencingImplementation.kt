package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Differencing
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedNamespace

class DifferencingImplementation(
    typeDifferenced: Type,
    differencingType: Type,
    owningRelatedElement: Element = UnresolvedNamespace(),
    elementType: String = "Differencing"
): Differencing, RelationshipImplementation(
    source = mutableListOf(typeDifferenced),
    target = mutableListOf(differencingType),
    owningRelatedElement = owningRelatedElement,
    elementType = elementType
)