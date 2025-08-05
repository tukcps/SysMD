package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Conjugation
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedElement
import com.github.tukcps.sysmd.model.kerml.UnresolvedType
import com.github.tukcps.sysmd.model.util.SimpleName

class ConjugationImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    owningRelatedElement: Element = UnresolvedElement(),
    type: Type = UnresolvedType(),
    conjugated: Type = UnresolvedType(),
    elementType: String = "Conjugation"
): Conjugation, RelationshipImplementation(
    declaredName, declaredShortName,
    source = mutableListOf(type),
    target = mutableListOf(conjugated),
    owningRelatedElement = owningRelatedElement,
    elementType = elementType
) {
    override var conjugatedType: Type
        get() = source.first() as Type
        set(value) { source = mutableListOf(value) }

    override var originalType: Type
        get() = target.first() as Type
        set(value) { target = mutableListOf(value) }
}