package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ReferenceSubsetting
import com.github.tukcps.sysmd.model.kerml.UnresolvedFeature

@Suppress("UNCHECKED_CAST")
class ReferenceSubsettingImplementation(
    referencingFeature: Feature = UnresolvedFeature(),
    referencedFeature: Feature = UnresolvedFeature(),
    elementType: String = "ReferenceSubsetting"
) : ReferenceSubsetting, SubsettingImplementation(
    subsettingFeature = referencingFeature,
    subsettedFeature = referencedFeature,
    elementType
) {
    override var referencedFeature: Feature
        get() = general as Feature
        set(value) { general = value }

    override var referencingFeature: Feature
        get() = specific as Feature
        set(value) { specific = value}

    override fun clone(): ReferenceSubsetting {
        return ReferenceSubsettingImplementation(
            referencingFeature = source.firstOrNull() as Feature,
            referencedFeature = target.firstOrNull() as Feature,
        ).also { klon ->
            klon.isTransient = isTransient
            klon.model = model
        }
    }

    override fun updateFrom(template: Element) {
        require(template is ReferenceSubsetting)
        super.updateFrom(template)
    }
}