package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Disjoining
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.UnresolvedNamespace
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class DisjoiningImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    owningRelatedElement: Element = UnresolvedNamespace(model),
    typeDisjoined: Type = UnresolvedType(model, "that"),
    disjoiningType: Type = UnresolvedType(model),
) : Disjoining, RelationshipImplementation(
    model,
    elementId = elementId,
    owningRelatedElement = owningRelatedElement,
    source = mutableListOf(typeDisjoined),
    target = mutableListOf(disjoiningType),
) {
    override var typeDisjoined: Type
        get() = source.first() as Type
        set(value) { source = mutableListOf(value) }

    override var disjoiningType: Type
        get() = target.first() as Type
        set(value) { target = mutableListOf(value) }

    override fun clone(): Disjoining = DisjoiningImplementation(
        model,
        owningRelatedElement = UnresolvedNamespace(model),
        typeDisjoined = typeDisjoined,
        disjoiningType = disjoiningType
    ).also {  updateFrom(this) }
}