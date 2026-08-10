package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.Unioning
import com.github.tukcps.sysmd.model.util.UnresolvedNamespace
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class UnioningImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    unionedType: Type = UnresolvedType(model),
    unioningType: Type = UnresolvedType(model),
    owningRelatedElement: Element = UnresolvedNamespace(model),
): Unioning, RelationshipImplementation(
    model,
    elementId = elementId,
    owningRelatedElement = owningRelatedElement,
    source = mutableListOf(unionedType),
    target = mutableListOf(unioningType),
)