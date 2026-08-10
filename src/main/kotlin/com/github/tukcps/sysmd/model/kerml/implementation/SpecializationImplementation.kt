package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Specialization
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.ErrorElement
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.model.datamodel.toElementData
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

@Suppress("UNCHECKED_CAST")
open class SpecializationImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    specific: Type = UnresolvedType(model, "self"),
    general: Type = UnresolvedType(model, "Base::Anything"),
): Specialization, RelationshipImplementation(
    model,
    elementId = elementId,
    owningRelatedElement = specific,
    source = mutableListOf(specific),
    target = mutableListOf(general),
) {

    override var general: Type
        get() = target.firstOrNull() as? Type
            ?: ErrorElement(model, target.first()).also { model.status.error("Name ${target.firstOrNull()} must resolve to a type", element = owner?.toElementData()) }
        set(value) { target = mutableListOf(value) }

    override var specific: Type
        get() = source.firstOrNull() as? Type
            ?: ErrorElement(model, target.first()).also { model.status.error("Name ${target.firstOrNull()} must resolve to a type", element = owner?.toElementData()) }
        set(value) { source= mutableListOf(value) }

    override fun clone(): Specialization = SpecializationImplementation(
        model,
        specific = specific,
        general = general
    ).also { klon ->
        klon.isTransient = isTransient
    }

    override fun updateFrom(template: Element) {
        require(template is Specialization)
        super.updateFrom(template)
    }

}