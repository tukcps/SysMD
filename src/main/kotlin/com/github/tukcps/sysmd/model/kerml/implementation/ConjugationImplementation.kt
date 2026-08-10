package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Conjugation
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.model.util.UnresolvedElement
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class ConjugationImplementation(
    model: Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    owningRelatedElement: Element = UnresolvedElement(model),
    type: Type = UnresolvedType(model),
    conjugated: Type = UnresolvedType(model),
): Conjugation, RelationshipImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    owningRelatedElement = owningRelatedElement,
    source = mutableListOf(type),
    target = mutableListOf(conjugated),
) {
    override var conjugatedType: Type
        get() = source.first() as Type
        set(value) { source = mutableListOf(value) }

    override var originalType: Type
        get() = target.first() as Type
        set(value) { target = mutableListOf(value) }

    override fun clone(): Conjugation = ConjugationImplementation(model).also {
        it.updateFrom(this)
    }
}