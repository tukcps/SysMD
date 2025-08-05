package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Disjoining
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedNamespace

class DisjoiningImplementation(
    owningRelatedElement: Element = UnresolvedNamespace(),
    typeDisjoined: Type,
    disjoiningType: Type,
    elementType: String = "Disjoining"

) : Disjoining, RelationshipImplementation(
    owningRelatedElement = owningRelatedElement,
    source = mutableListOf(typeDisjoined),
    target = mutableListOf(disjoiningType),
    elementType = elementType
) {
    override var typeDisjoined: Type
        get() = source.first() as Type
        set(value) { source = mutableListOf(value) }

    override var disjoiningType: Type
        get() = target.first() as Type
        set(value) { target = mutableListOf(value) }

    override fun clone(): Disjoining {
        return DisjoiningImplementation(
            owningRelatedElement = UnresolvedNamespace(),
            typeDisjoined = typeDisjoined,
            disjoiningType = disjoiningType
        ).also {  updateFrom(this) }
    }
}