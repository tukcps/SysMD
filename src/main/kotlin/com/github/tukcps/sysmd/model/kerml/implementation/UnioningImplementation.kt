package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.Unioning
import com.github.tukcps.sysmd.model.kerml.UnresolvedNamespace

class UnioningImplementation(
    unionedType: Type,
    unioningType: Type,
    owningRelatedElement: Element = UnresolvedNamespace(),
    elementType: String = "Unioning"
): Unioning, RelationshipImplementation(
    source = mutableListOf(unionedType),
    target = mutableListOf(unioningType),
    owningRelatedElement = owningRelatedElement,
    elementType = elementType
)