package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Specialization
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedType

@Suppress("UNCHECKED_CAST")
open class SpecializationImplementation(
    specific: Type = UnresolvedType("self"),
    general: Type = UnresolvedType("Base::Anything"),
    elementType: String = "Specialization"
): Specialization, RelationshipImplementation(
    owningRelatedElement = specific,
    source = mutableListOf(specific),
    target = mutableListOf(general),
    elementType = elementType
) {

    override var general: Type
        get() = ( (target.firstOrNull() as Type?) ?: model?.anything as Type)
        set(value) { target = mutableListOf(value) }

    override var specific: Type
        get() = (
                if (source.firstOrNull() !is Type)
                    TODO()
                else (source.firstOrNull() as Type?) ?: this.owner as Type)
        set(value) { source= mutableListOf(value) }

    override fun clone(): Specialization {
        return SpecializationImplementation(
            specific = specific,
            general = general
        ).also { klon ->
            klon.isTransient = isTransient
            klon.model = model
        }
    }

    override fun updateFrom(template: Element) {
        require(template is Specialization)
        super.updateFrom(template)
    }

}