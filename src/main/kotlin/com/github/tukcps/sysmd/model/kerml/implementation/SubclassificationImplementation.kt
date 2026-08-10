package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Subclassification
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class SubclassificationImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    subclassification: Type = UnresolvedType(model),
    superclassification: Type = UnresolvedType(model),
): Subclassification, SpecializationImplementation(
    model,
    elementId = elementId,
    specific = subclassification,
    general = superclassification,
) {
    override fun clone(): Subclassification = SubclassificationImplementation(model).also {
        it.updateFrom(this)
    }
}