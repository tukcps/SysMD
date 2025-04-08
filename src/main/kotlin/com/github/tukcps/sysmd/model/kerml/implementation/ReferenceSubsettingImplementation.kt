package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ReferenceSubsetting
import com.github.tukcps.sysmd.model.kerml.Resolved

@Suppress("UNCHECKED_CAST")
class ReferenceSubsettingImplementation(
    referencingFeature: Resolved<Feature>? = null,
    referencedFeature: Resolved<Feature>? = null,
    elementType: String = "ReferenceSubsetting"
) : ReferenceSubsetting, SubsettingImplementation(
    subsettingFeature = referencingFeature,
    subsettedFeature = referencedFeature,
    elementType
) {
    override var referencedFeature: Resolved<Feature>
        get() = general as Resolved<Feature>
        set(value) { general = value }

    override var referencingFeature: Resolved<Feature>
        get() = specific as Resolved<Feature>
        set(value) { specific = value}

    override fun toString(): String {
        return "$elementType { ${source.firstOrNull()}, ${target.firstOrNull()}}"
    }

    override fun clone(): ReferenceSubsetting {
        return ReferenceSubsettingImplementation(
            referencingFeature = Resolved(source.firstOrNull() as Resolved<Feature>),
            referencedFeature = if (target.firstOrNull() == null) Resolved() else Resolved(target.firstOrNull() as Resolved<Feature>)
        ).also { klon ->
            klon.isTransient = isTransient
            klon.model = model
        }
    }

    override fun updateFrom(template: Element) {
        require(template is ReferenceSubsetting)
        super.updateFrom(template)
    }

    override fun setOwner(owningElement: Element) {
        if (owningElement is Feature) {
            super.setOwner(owningElement)
            referencingFeature = Resolved(owningElement)
        } else
            owningElement.model?.status?.fatal("Attempt to add a reference to a non-feature element", element = owningElement)
    }
}